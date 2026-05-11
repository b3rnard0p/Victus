package com.example.sistemanutricao.service.ingrediente;

import com.example.sistemanutricao.exception.IngredienteNotFoundException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sistemanutricao.mapper.IngredienteMapper;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;

@Service
public class IngredienteService {

    @Value("${app.taco.username}")
    private String tacoUsername;



    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final IngredienteMapper ingredienteMapper;

    public IngredienteService(IngredienteRepository ingredienteRepository,
                              UsuarioRepository usuarioRepository,
                              IngredienteMapper ingredienteMapper) {
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.ingredienteMapper = ingredienteMapper;
    }

    private void validarDuplicidadeIngredienteCreate(String nome, Long usuarioId) {
        if (ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatus(nome, usuarioId, Status.ATIVA)) {
            throw new IllegalArgumentException("Já existe um ingrediente com este nome em seus ingredientes.");
        }
        Long tacoId = buscarUsuarioTacoId();
        if (ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatus(nome, tacoId, Status.ATIVA)) {
            throw new IllegalArgumentException("Já existe um ingrediente com este nome na tabela TACO.");
        }
    }

    private void validarDuplicidadeIngredienteUpdate(String nome, Long ingredienteId, Long usuarioId) {
        if (ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatusAndIdNot(nome, usuarioId, Status.ATIVA, ingredienteId)) {
            throw new IllegalArgumentException("Já existe um ingrediente com este nome em seus ingredientes.");
        }
        Long tacoId = buscarUsuarioTacoId();
        if (ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatus(nome, tacoId, Status.ATIVA)) {
            throw new IllegalArgumentException("Já existe um ingrediente com este nome na tabela TACO.");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredienteGetDTO create(IngredienteDTO dto, Long usuarioId) {
        validarCamposObrigatorios(dto);
        validarDuplicidadeIngredienteCreate(dto.nome(), usuarioId);

        Ingrediente ingrediente = ingredienteMapper.toEntity(dto);
        ingrediente.setUsuario(buscarUsuarioOuFalhar(usuarioId));

        return convertToDto(ingredienteRepository.save(ingrediente));
    }

    @Transactional(rollbackFor = Exception.class)
    public IngredienteGetDTO update(Long id, IngredienteDTO dto) {
        Ingrediente ingrediente = buscarIngredienteOuFalhar(id);

        if (!ingrediente.getNome().equalsIgnoreCase(dto.nome())) {
            validarDuplicidadeIngredienteUpdate(dto.nome(), id, ingrediente.getUsuario().getId());
        }

        validarCamposObrigatorios(dto);
        ingredienteMapper.updateEntity(ingrediente, dto);

        return convertToDto(ingredienteRepository.save(ingrediente));
    }

    public IngredienteGetDTO getIngredienteById(Long id) {
        return convertToDto(buscarIngredienteOuFalhar(id));
    }

    public Usuario buscarUsuarioTaco() {
        return usuarioRepository.findByUsernameIgnoreCase(tacoUsername)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário TACO não encontrado"));
    }


    public Long buscarUsuarioTacoId() {
        return buscarUsuarioTaco().getId();
    }

    public void atualizaStatus(Long id) {
        Ingrediente ingrediente = buscarIngredienteOuFalhar(id);

        ingrediente.setStatus(
                ingrediente.getStatus() == Status.ATIVA ?
                        Status.INATIVA :
                        Status.ATIVA
        );

        ingredienteRepository.save(ingrediente);
    }

    private IngredienteGetDTO convertToDto(Ingrediente ingre) {
        return ingredienteMapper.toGetDto(ingre);
    }

    @SuppressWarnings("null")
    private Optional<Usuario> buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId);
    }

    private Usuario buscarUsuarioOuFalhar(Long usuarioId) {
        return buscarUsuario(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));
    }

    @SuppressWarnings("null")
    private Ingrediente buscarIngredienteOuFalhar(Long ingredienteId) {
        return ingredienteRepository.findById(ingredienteId)
                .orElseThrow(() -> new IngredienteNotFoundException("Ingrediente não encontrado"));
    }

    private void validarCamposObrigatorios(IngredienteDTO dto) {
        if (dto.nome() == null || dto.nome().trim().isEmpty() || dto.ptn() == null || dto.cho() == null || dto.lip() == null
                || dto.sodio() == null || dto.gorduraSaturada() == null) {
            throw new IllegalArgumentException("Todos os campos nutricionais e o nome são obrigatórios.");
        }
    }
}
