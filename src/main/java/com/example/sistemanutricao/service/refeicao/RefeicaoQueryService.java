package com.example.sistemanutricao.service.refeicao;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.repository.RefeicaoRepository;
import com.example.sistemanutricao.repository.specification.RefeicaoSpecification;
import com.example.sistemanutricao.mapper.RefeicaoMapper;
import org.springframework.data.jpa.domain.Specification;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class RefeicaoQueryService {

    private static final int MAX_SEARCH_RESULTS = 500;

    private final RefeicaoRepository refeicaoRepository;
    private final RefeicaoMapper refeicaoMapper;

    public RefeicaoQueryService(RefeicaoRepository refeicaoRepository,
                                RefeicaoMapper refeicaoMapper) {
        this.refeicaoRepository = refeicaoRepository;
        this.refeicaoMapper = refeicaoMapper;
    }

    public List<RefeicaoResponseDTO> buscarPorStatus(Status status, Usuario usuario) {
        return buscarPorStatus(status, usuario, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
    }

    public Page<RefeicaoResponseDTO> buscarPorStatus(Status status, Usuario usuario, Pageable pageable) {
        Long nutricionistaId = isProducao(usuario) ? null : usuario.getId();
        Long estabelecimentoId = isProducao(usuario) ? usuario.getEstabelecimento().getId() : null;

        Specification<Refeicao> spec = RefeicaoSpecification.filter(status, nutricionistaId, estabelecimentoId, null, null);
        return refeicaoRepository.findAll(spec, pageable).map(refeicaoMapper::toResponseDTO);
    }

    public List<RefeicaoResponseDTO> buscarPorNome(String nome, Usuario usuario) {
        return buscarPorNome(nome, usuario, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
    }

    public Page<RefeicaoResponseDTO> buscarPorNome(String nome, Usuario usuario, Pageable pageable) {
        Long nutricionistaId = isProducao(usuario) ? null : usuario.getId();
        Long estabelecimentoId = isProducao(usuario) ? usuario.getEstabelecimento().getId() : null;

        Specification<Refeicao> spec = RefeicaoSpecification.filter(Status.ATIVA, nutricionistaId, estabelecimentoId, "nome", nome);
        return refeicaoRepository.findAll(spec, pageable).map(refeicaoMapper::toResponseDTO);
    }

    private boolean isProducao(Usuario u) {
        return u != null && u.getCargo() != null && "PRODUCAO".equals(u.getCargo().name());
    }
}
