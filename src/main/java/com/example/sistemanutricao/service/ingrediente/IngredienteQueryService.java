package com.example.sistemanutricao.service.ingrediente;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import com.example.sistemanutricao.mapper.IngredienteMapper;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.repository.specification.IngredienteSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class IngredienteQueryService {

    private static final String TACO_USERNAME = "TACO";
    private static final int MAX_SEARCH_RESULTS = 500;

    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final IngredienteMapper ingredienteMapper;
    private final com.example.sistemanutricao.service.ficha.IngredienteTagClassifier ingredienteTagClassifier;

    public IngredienteQueryService(IngredienteRepository ingredienteRepository,
                                   UsuarioRepository usuarioRepository,
                                   IngredienteMapper ingredienteMapper,
                                   com.example.sistemanutricao.service.ficha.IngredienteTagClassifier ingredienteTagClassifier) {
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.ingredienteMapper = ingredienteMapper;
        this.ingredienteTagClassifier = ingredienteTagClassifier;
    }

    public List<IngredienteGetDTO> buscarPorStatusEUsuario(Status status, Long usuarioId) {
        Specification<Ingrediente> spec = IngredienteSpecification.filter(status, usuarioId, null, null);
        List<Ingrediente> ingredientes = ingredienteRepository.findAll(spec, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
        return ingredientes.stream().map(ingredienteMapper::toGetDto).toList();
    }

    public Page<IngredienteGetDTO> buscarPorStatusEUsuario(Status status, Long usuarioId, Pageable pageable) {
        Specification<Ingrediente> spec = IngredienteSpecification.filter(status, usuarioId, null, null);
        return ingredienteRepository.findAll(spec, pageable).map(ingredienteMapper::toGetDto);
    }

    public List<IngredienteGetDTO> buscarPorStatusEUsuarios(Status status, Long... usuariosIds) {
        List<Long> ids = Arrays.asList(usuariosIds);
        List<Ingrediente> ingredientes = ingredienteRepository.findByStatusAndUsuarioIdIn(status, ids);
        return ingredientes.stream().map(ingredienteMapper::toGetDto).toList();
    }

    public Page<IngredienteGetDTO> buscarIngredientesDoUsuarioTaco(Pageable pageable) {
        Long tacoId = buscarUsuarioTacoId();
        Specification<Ingrediente> spec = IngredienteSpecification.filter(null, tacoId, null, null);
        return ingredienteRepository.findAll(spec, pageable).map(ingredienteMapper::toGetDto);
    }

    public Page<?> pesquisar(String campo, String valor, String tipoPesquisa, Long usuarioId, Pageable pageable) {
        if ("tags".equals(tipoPesquisa)) {
            return buscarPorTag(campo, valor, usuarioId, pageable);
        }

        Object valorObjeto = valor;
        if (!"nome".equalsIgnoreCase(campo)) {
            try { valorObjeto = new BigDecimal(valor); } catch (NumberFormatException e) { return Page.empty(pageable); }
        }

        Specification<Ingrediente> spec = IngredienteSpecification.filter(Status.ATIVA, usuarioId, campo, valorObjeto);
        return ingredienteRepository.findAll(spec, pageable).map(ingredienteMapper::toGetDto);
    }

    public Page<?> pesquisarTaco(String campo, String valor, String tipoPesquisa, Pageable pageable) {
        if ("tags".equals(tipoPesquisa)) {
            return buscarPorTagTaco(campo, valor, pageable);
        }

        Long tacoId = buscarUsuarioTacoId();
        Object valorObjeto = valor;
        if (!"nome".equalsIgnoreCase(campo)) {
            try { valorObjeto = new BigDecimal(valor); } catch (NumberFormatException e) { return Page.empty(pageable); }
        }

        Specification<Ingrediente> spec = IngredienteSpecification.filter(Status.ATIVA, tacoId, campo, valorObjeto);
        return ingredienteRepository.findAll(spec, pageable).map(ingredienteMapper::toGetDto);
    }

    public List<IngredienteComTagDTO> buscarPorTag(String campo, String tag, Long usuarioId) {
        Specification<Ingrediente> spec = IngredienteSpecification.filter(Status.ATIVA, usuarioId, null, null);
        List<Ingrediente> ingredientes = ingredienteRepository.findAll(spec, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
        return ingredientes.stream()
                .map(ing -> ingredienteMapper.toTagDto(ing, ingredienteTagClassifier.determinarTag(ing, campo)))
                .filter(ing -> ing.tag().equalsIgnoreCase(tag))
                .toList();
    }

    public Page<IngredienteComTagDTO> buscarPorTag(String campo, String tag, Long usuarioId, Pageable pageable) {
        return paginarLista(buscarPorTag(campo, tag, usuarioId), pageable);
    }

    public List<IngredienteComTagDTO> buscarPorTagTaco(String campo, String tag) {
        Long tacoId = buscarUsuarioTacoId();
        Specification<Ingrediente> spec = IngredienteSpecification.filter(Status.ATIVA, tacoId, null, null);
        List<Ingrediente> ingredientes = ingredienteRepository.findAll(spec, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
        return ingredientes.stream()
                .map(ing -> ingredienteMapper.toTagDto(ing, ingredienteTagClassifier.determinarTag(ing, campo)))
                .filter(ing -> ing.tag().equalsIgnoreCase(tag))
                .toList();
    }

    public Page<IngredienteComTagDTO> buscarPorTagTaco(String campo, String tag, Pageable pageable) {
        return paginarLista(buscarPorTagTaco(campo, tag), pageable);
    }

    private <T> Page<T> paginarLista(List<T> itens, Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(itens);
        }

        int start = Math.toIntExact(pageable.getOffset());
        if (start >= itens.size()) {
            return new PageImpl<>(List.of(), pageable, itens.size());
        }

        int end = Math.min(start + pageable.getPageSize(), itens.size());
        return new PageImpl<>(itens.subList(start, end), pageable, itens.size());
    }

    private Usuario buscarUsuarioTaco() {
        return usuarioRepository.findByUsernameIgnoreCase(TACO_USERNAME)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário TACO não encontrado"));
    }

    private Long buscarUsuarioTacoId() {
        return buscarUsuarioTaco().getId();
    }
}
