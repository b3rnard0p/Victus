package com.example.sistemanutricao.service;

import com.example.sistemanutricao.record.EstabelecimentoDTO.EstabelecimentoDTO;
import com.example.sistemanutricao.record.EstabelecimentoDTO.GetEstabelecimentoDTO;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import com.example.sistemanutricao.exception.EstabelecimentoNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;

    public EstabelecimentoService(EstabelecimentoRepository estabelecimentoRepository) {
        this.estabelecimentoRepository = estabelecimentoRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public GetEstabelecimentoDTO create(EstabelecimentoDTO dto) {
        if (estabelecimentoRepository.existsByNome(dto.nome())) {
            throw new IllegalArgumentException("Já existe um estabelecimento com este nome.");
        }
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setNome(dto.nome());

        Estabelecimento saved = estabelecimentoRepository.save(estabelecimento);
        return toGetDTO(saved);
    }

    public List<GetEstabelecimentoDTO> listAll() {
        return listPage(Pageable.unpaged()).getContent();
    }

    public Page<GetEstabelecimentoDTO> listPage(Pageable pageable) {
        return estabelecimentoRepository.findAll(pageable)
                .map(this::toGetDTO);
    }

    public GetEstabelecimentoDTO findById(Long id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new EstabelecimentoNotFoundException("Estabelecimento não encontrado"));
        return toGetDTO(estabelecimento);
    }

    @Transactional(rollbackFor = Exception.class)
    public GetEstabelecimentoDTO update(Long id, EstabelecimentoDTO dto) {
        if (estabelecimentoRepository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new IllegalArgumentException("Já existe outro estabelecimento com este nome.");
        }
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new EstabelecimentoNotFoundException("Estabelecimento não encontrado"));

        estabelecimento.setNome(dto.nome());

        Estabelecimento updated = estabelecimentoRepository.save(estabelecimento);
        return toGetDTO(updated);
    }

    private GetEstabelecimentoDTO toGetDTO(Estabelecimento estabelecimento) {
        List<GetUsuarioDTO> usuariosDTO = List.of();

        if (estabelecimento.getUsuario() != null && !estabelecimento.getUsuario().isEmpty()) {
            usuariosDTO = estabelecimento.getUsuario().stream()
                    .map(usuario -> {
                        Long estabelecimentoId = null;
                        String estabelecimentoNome = null;

                        if (usuario.getEstabelecimento() != null) {
                            estabelecimentoId = usuario.getEstabelecimento().getId();
                            estabelecimentoNome = usuario.getEstabelecimento().getNome();
                        }

                        return new GetUsuarioDTO(
                                usuario.getId(),
                                usuario.getUsername(),
                                usuario.getEmail(),
                                usuario.getCargo(),
                                estabelecimentoId,
                                estabelecimentoNome,
                                usuario.isAtivo(),
                                usuario.getCaminhoImagem()
                        );
                    })
                    .collect(Collectors.toList());
        }

        return new GetEstabelecimentoDTO(
                estabelecimento.getId(),
                estabelecimento.getNome(),
                usuariosDTO
        );
    }
}
