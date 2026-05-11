package com.example.sistemanutricao.service.ficha;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Categoria;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaComTagDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.specification.FichaTecnicaSpecification;
import com.example.sistemanutricao.mapper.FichaTecnicaMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class FichaQueryService {

    private static final int MAX_SEARCH_RESULTS = 500;
    
    private final FichaTecnicaRepository fichaRepository;
    private final FichaTagClassifier tagClassifier;
    private final FichaTecnicaMapper fichaTecnicaMapper;

    public FichaQueryService(FichaTecnicaRepository fichaRepository,
                             FichaTagClassifier tagClassifier,
                             FichaTecnicaMapper fichaTecnicaMapper) {
        this.fichaRepository = fichaRepository;
        this.tagClassifier = tagClassifier;
        this.fichaTecnicaMapper = fichaTecnicaMapper;
    }

    // BUSCA POR STATUS

    public List<FichaTecnicaGetDTO> buscarPorStatus(Status status, StatusCriacao statusCriacao, Usuario usuario) {
        return processarFichas(fichasStatus(status, statusCriacao, usuario, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent());
    }

    public Page<FichaTecnicaGetDTO> buscarPorStatus(Status status, StatusCriacao statusCriacao, Usuario usuario, Pageable pageable) {
        return fichasStatus(status, statusCriacao, usuario, pageable)
                .map(fichaTecnicaMapper::toGetDTO);
    }

    public List<FichaTecnicaGetDTO> buscarPorStatusSimples(Status status, Usuario usuario) {
        return processarFichas(fichasStatusSimples(status, usuario, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent());
    }

    public Page<FichaTecnicaGetDTO> buscarPorStatusSimples(Status status, Usuario usuario, Pageable pageable) {
        return fichasStatusSimples(status, usuario, pageable)
                .map(fichaTecnicaMapper::toGetDTO);
    }

    // PESQUISA DINÂMICA

    public Page<?> pesquisar(String campo, String valor, String tipoPesquisa, Usuario usuario, Pageable pageable) {
        if ("tags".equals(tipoPesquisa)) {
            return buscarPorTag(campo, valor, usuario, pageable);
        }

        Long nutricionistaId = isProducao(usuario) ? null : usuario.getId();
        Long estabelecimentoId = (isProducao(usuario) && usuario.getEstabelecimento() != null) ? usuario.getEstabelecimento().getId() : null;

        Object valorObjeto = valor;
        if ("por-numero".equalsIgnoreCase(campo)) {
            try { valorObjeto = Integer.valueOf(valor); } catch (NumberFormatException e) { return Page.empty(); }
        } else if ("por-categoria".equalsIgnoreCase(campo)) {
            valorObjeto = Arrays.stream(Categoria.values())
                    .filter(c -> c.getNome().equalsIgnoreCase(valor))
                    .findFirst()
                    .orElse(null);
            if (valorObjeto == null) return Page.empty();
        } else if (!"por-nome".equalsIgnoreCase(campo)) {
            try { valorObjeto = new BigDecimal(valor); } catch (NumberFormatException e) { return Page.empty(); }
        }

        Specification<FichaTecnica> spec = FichaTecnicaSpecification.filter(
                Status.ATIVA, StatusCriacao.COMPLETA, nutricionistaId, estabelecimentoId, campo, valorObjeto);

        return fichaRepository.findAll(spec, pageable).map(fichaTecnicaMapper::toGetDTO);
    }

    // BUSCA POR TAG

    public List<FichaTecnicaComTagDTO> buscarPorTag(String campo, String tag, Usuario usuario) {
        if (campo == null || tag == null || usuario == null) return new ArrayList<>();

        Long nutricionistaId = isProducao(usuario) ? null : usuario.getId();
        Long estabelecimentoId = (isProducao(usuario) && usuario.getEstabelecimento() != null) ? usuario.getEstabelecimento().getId() : null;

        Specification<FichaTecnica> spec = FichaTecnicaSpecification.filter(
                Status.ATIVA, StatusCriacao.COMPLETA, nutricionistaId, estabelecimentoId, null, null)
                .and(FichaTecnicaSpecification.byTag(campo, tag));

        List<FichaTecnica> fichas = fichaRepository.findAll(spec, PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();

        return fichas.stream()
                .map(f -> toComTagDTO(f, campo))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public Page<FichaTecnicaComTagDTO> buscarPorTag(String campo, String tag, Usuario usuario, Pageable pageable) {
        Long nutricionistaId = isProducao(usuario) ? null : usuario.getId();
        Long estabelecimentoId = (isProducao(usuario) && usuario.getEstabelecimento() != null) ? usuario.getEstabelecimento().getId() : null;

        Specification<FichaTecnica> spec = FichaTecnicaSpecification.filter(
                Status.ATIVA, StatusCriacao.COMPLETA, nutricionistaId, estabelecimentoId, null, null)
                .and(FichaTecnicaSpecification.byTag(campo, tag));

        return fichaRepository.findAll(spec, pageable).map(f -> toComTagDTO(f, campo));
    }

    // HELPERS PRIVADOS

    private List<FichaTecnicaGetDTO> processarFichas(List<FichaTecnica> fichas) {
        return fichas.stream().map(fichaTecnicaMapper::toGetDTO).toList();
    }

    private <T> Page<T> paginarLista(List<T> listaCompleta, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), listaCompleta.size());
        if (start >= listaCompleta.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, listaCompleta.size());
        }
        return new PageImpl<>(new ArrayList<>(listaCompleta.subList(start, end)), pageable, listaCompleta.size());
    }

    private boolean isProducao(Usuario u) {
        return u != null && u.getCargo() != null && "PRODUCAO".equals(u.getCargo().name());
    }

    private Page<FichaTecnica> fichasStatus(Status status, StatusCriacao sc, Usuario u, Pageable p) {
        Long nutricionistaId = isProducao(u) ? null : u.getId();
        Long estabelecimentoId = (isProducao(u) && u.getEstabelecimento() != null) ? u.getEstabelecimento().getId() : null;
        
        Specification<FichaTecnica> spec = FichaTecnicaSpecification.filter(status, sc, nutricionistaId, estabelecimentoId, null, null);
        return fichaRepository.findAll(spec, p);
    }

    private Page<FichaTecnica> fichasStatusSimples(Status status, Usuario u, Pageable p) {
        Long nutricionistaId = isProducao(u) ? null : u.getId();
        Long estabelecimentoId = (isProducao(u) && u.getEstabelecimento() != null) ? u.getEstabelecimento().getId() : null;

        Specification<FichaTecnica> spec = FichaTecnicaSpecification.filter(status, StatusCriacao.COMPLETA, nutricionistaId, estabelecimentoId, null, null);
        return fichaRepository.findAll(spec, p);
    }

    private FichaTecnicaComTagDTO toComTagDTO(FichaTecnica ficha, String campo) {
        try {
            return new FichaTecnicaComTagDTO(
                    ficha.getId(),
                    ficha.getPreparacao().getNome() != null ? ficha.getPreparacao().getNome() : "",
                    ficha.getPreparacao().getCategoria() != null ? ficha.getPreparacao().getCategoria().getNome() : "",
                    ficha.getPreparacao().getNumero(),
                    ficha.getCustoPerCapita(),
                    ficha.getCustoTotal(),
                    ficha.getPreparacao().getRendimento(),
                    ficha.getPerfilNutricional().getVtc(),
                    ficha.getPerfilNutricional().getGramasPTN(),
                    ficha.getPerfilNutricional().getGramasCHO(),
                    ficha.getPerfilNutricional().getGramasLIP(),
                    ficha.getPerfilNutricional().getGramasSodio(),
                    ficha.getPerfilNutricional().getGramasSaturada(),
                    ficha.getStatus(),
                    ficha.getStatusCriacao(),
                    ficha.getNutricionista().getId(),
                    tagClassifier.determinarTag(ficha, campo)
            );
        } catch (Exception e) {
            return null;
        }
    }
}
