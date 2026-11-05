package com.example.sistemanutricao.service.refeicao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.lang.NonNull;
import com.example.sistemanutricao.exception.RefeicaoNotFoundException;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoNutrientesResponseDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.RefeicaoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.mapper.RefeicaoMapper;
import com.example.sistemanutricao.aop.RegistrarAcao;
import com.example.sistemanutricao.model.enuns.TipoAcao;

@Service
public class RefeicaoService {

    private final RefeicaoRepository refeicaoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RefeicaoMapper refeicaoMapper;

    public RefeicaoService(RefeicaoRepository refeicaoRepository,
                           FichaTecnicaRepository fichaTecnicaRepository,
                           UsuarioRepository usuarioRepository,
                           RefeicaoMapper refeicaoMapper) {
        this.refeicaoRepository = refeicaoRepository;
        this.fichaTecnicaRepository = fichaTecnicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.refeicaoMapper = refeicaoMapper;
    }

    private Refeicao findRefeicaoById(@NonNull Long id) {
        return refeicaoRepository.findById(id)
                .orElseThrow(() -> new RefeicaoNotFoundException("Refeição não encontrada"));
    }

    public RefeicaoNutrientesResponseDTO buscarTotaisNutrientesPorId(@NonNull Long id, Usuario usuario) {
        var refeicao = findRefeicaoById(id);
        validarAcessoLeituraRefeicao(refeicao, usuario);
        return montarTotaisNutrientes(id, refeicao);
    }

    public RefeicaoNutrientesResponseDTO buscarTotaisNutrientesPorId(@NonNull Long id) {
        return montarTotaisNutrientes(id, findRefeicaoById(id));
    }

    private RefeicaoNutrientesResponseDTO montarTotaisNutrientes(Long id, Refeicao refeicaoAlvo) {
        Refeicao refeicao = refeicaoAlvo != null ? refeicaoAlvo : findRefeicaoById(id);
        if (refeicao.getTotalGramasPTN() == null) {
            atualizarNutrientes(refeicao);
            refeicao = refeicaoRepository.save(refeicao);
        }

        return new RefeicaoNutrientesResponseDTO(
            refeicao.getId(),
            refeicao.getNome(),
            refeicao.getKcalTotal(),
            refeicao.getStatus(),
            refeicaoMapper.toFichasDTOList(refeicao.getFichasPorRefeicao()),
            refeicao.getTotalGramasPTN(),
            refeicao.getTotalGramasCHO(),
            refeicao.getTotalGramasLIP(),
            refeicao.getTotalGramasSodio(),
            refeicao.getTotalGramasSaturada()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.CRIOU_REFEICAO)
    public RefeicaoResponseDTO create(RefeicaoDTO dto, @NonNull Long nutricionistaId) {
        List<Long> fichasIds = normalizarFichasIds(dto.fichasTecnicasIds());
        validarFichasObrigatorias(fichasIds);

        Usuario nutricionista = usuarioRepository.findById(nutricionistaId)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        validarDuplicidadeNomeCreate(dto.nome(), nutricionistaId);

        Refeicao refeicao = new Refeicao();
        refeicao.setNome(normalizarTexto(dto.nome()));
        refeicao.setKcalTotal(dto.kcalTotal());
        refeicao.setStatus(dto.status());
        refeicao.setNutricionista(nutricionista);

        syncFichas(refeicao, fichasIds);
        atualizarNutrientes(refeicao);

        Refeicao savedRefeicao = refeicaoRepository.save(refeicao);
        return refeicaoMapper.toResponseDTO(savedRefeicao);
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.EDITOU_REFEICAO)
    public RefeicaoResponseDTO update(@NonNull Long id, RefeicaoDTO dto, Long nutricionistaId) {
        var refeicao = findRefeicaoById(id);
        validarPropriedadeRefeicao(refeicao, nutricionistaId);
        return executarUpdate(id, dto, refeicao);
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.EDITOU_REFEICAO)
    public RefeicaoResponseDTO update(@NonNull Long id, RefeicaoDTO dto) {
        return executarUpdate(id, dto, null);
    }

    private RefeicaoResponseDTO executarUpdate(@NonNull Long id, RefeicaoDTO dto, Refeicao refeicaoAlvo) {
        List<Long> fichasIds = normalizarFichasIds(dto.fichasTecnicasIds());
        validarFichasObrigatorias(fichasIds);

        Refeicao refeicao = refeicaoAlvo != null ? refeicaoAlvo : findRefeicaoById(id);
        validarDuplicidadeNomeUpdate(dto.nome(), refeicao.getNutricionista().getId(), id);

        refeicao.setNome(normalizarTexto(dto.nome()));
        refeicao.setKcalTotal(dto.kcalTotal());
        refeicao.setStatus(dto.status());

        syncFichas(refeicao, fichasIds);
        atualizarNutrientes(refeicao);

        Refeicao salva = refeicaoRepository.save(refeicao);
        return refeicaoMapper.toResponseDTO(salva);
    }

    private void atualizarNutrientes(Refeicao refeicao) {
        RefeicaoNutrientesCalculator.Totais totais = new RefeicaoNutrientesCalculator().calcularTotais(refeicao);
        refeicao.setTotalGramasPTN(totais.totalGramasPTN());
        refeicao.setTotalGramasCHO(totais.totalGramasCHO());
        refeicao.setTotalGramasLIP(totais.totalGramasLIP());
        refeicao.setTotalGramasSodio(totais.totalGramasSodio());
        refeicao.setTotalGramasSaturada(totais.totalGramasSaturada());

        if (refeicao.getFichasPorRefeicao() != null && !refeicao.getFichasPorRefeicao().isEmpty()) {
            BigDecimal totalVtc = BigDecimal.ZERO;
            for (FichasPorRefeicao fp : refeicao.getFichasPorRefeicao()) {
                if (fp != null && fp.getFichaTecnica() != null && fp.getFichaTecnica().getPerfilNutricional() != null) {
                    BigDecimal vtc = fp.getFichaTecnica().getPerfilNutricional().getVtc();
                    if (vtc != null) {
                        totalVtc = totalVtc.add(vtc);
                    }
                }
            }
            if (totalVtc.compareTo(BigDecimal.ZERO) > 0) {
                refeicao.setKcalTotal(totalVtc.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            }
        }
    }

    private void syncFichas(Refeicao refeicao, List<Long> fichasIds) {
        List<FichasPorRefeicao> atuais = refeicao.getFichasPorRefeicao();
        atuais.removeIf(fp -> !fichasIds.contains(fp.getFichaTecnica().getId()));

        Set<Long> idsExistentes = atuais.stream()
                .map(fp -> fp.getFichaTecnica().getId())
                .collect(Collectors.toSet());

        List<Long> novosIds = fichasIds.stream()
                .filter(id -> !idsExistentes.contains(id))
                .toList();

        if (!novosIds.isEmpty()) {
            List<FichaTecnica> fichasParaAdicionar = fichaTecnicaRepository.findAllById(novosIds);
            for (FichaTecnica ft : fichasParaAdicionar) {
                if (refeicao.getNutricionista() != null && ft.getNutricionista() != null
                        && !ft.getNutricionista().getId().equals(refeicao.getNutricionista().getId())) {
                    throw new IllegalArgumentException("Você não pode vincular fichas técnicas de outro nutricionista.");
                }
                atuais.add(new FichasPorRefeicao(refeicao, ft));
            }
        }
    }

    private List<Long> normalizarFichasIds(List<Long> fichasIds) {
        return fichasIds == null ? new ArrayList<>() : fichasIds.stream().filter(id -> id != null).distinct().toList();
    }

    private void validarFichasObrigatorias(List<Long> fichasIds) {
        if (fichasIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione ao menos uma ficha técnica para a refeição.");
        }
    }

    private void validarDuplicidadeNomeCreate(String nome, Long nutricionistaId) {
        String nomeNorm = normalizarTexto(nome);
        if (nomeNorm != null && refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCase(nutricionistaId, nomeNorm)) {
            throw new IllegalArgumentException("Já existe uma refeição com este nome.");
        }
    }

    private void validarDuplicidadeNomeUpdate(String nome, Long nutricionistaId, Long refeicaoId) {
        String nomeNorm = normalizarTexto(nome);
        if (nomeNorm != null && refeicaoRepository.existsByNutricionistaIdAndNomeIgnoreCaseAndIdNot(nutricionistaId, nomeNorm, refeicaoId)) {
            throw new IllegalArgumentException("Já existe outra refeição com este nome.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    @Transactional(readOnly = true)
    public RefeicaoResponseDTO buscarPorId(@NonNull Long id, Long nutricionistaId) {
        var refeicao = findRefeicaoById(id);
        validarPropriedadeRefeicao(refeicao, nutricionistaId);
        return refeicaoMapper.toResponseDTO(refeicao);
    }

    @Transactional(readOnly = true)
    public RefeicaoResponseDTO buscarPorId(@NonNull Long id) {
        return refeicaoMapper.toResponseDTO(findRefeicaoById(id));
    }

    @Transactional
    @RegistrarAcao(acao = TipoAcao.ARQUIVOU_REFEICAO)
    public void atualizaStatus(@NonNull Long id, Long nutricionistaId) {
        var refeicao = findRefeicaoById(id);
        validarPropriedadeRefeicao(refeicao, nutricionistaId);
        refeicao.setStatus(refeicao.getStatus() == Status.ATIVA ? Status.INATIVA : Status.ATIVA);
        refeicaoRepository.save(refeicao);
    }

    private void validarPropriedadeRefeicao(Refeicao refeicao, Long nutricionistaId) {
        if (nutricionistaId == null || refeicao.getNutricionista() == null || !refeicao.getNutricionista().getId().equals(nutricionistaId)) {
            throw new IllegalArgumentException("Você não tem permissão para alterar esta refeição.");
        }
    }

    private void validarAcessoLeituraRefeicao(Refeicao refeicao, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Você não tem permissão para acessar esta refeição.");
        }
        if (usuario.getCargo() != null && "PRODUCAO".equals(usuario.getCargo().name())) {
            Long estabUsuario = usuario.getEstabelecimento() != null ? usuario.getEstabelecimento().getId() : null;
            Long estabRefeicao = (refeicao.getNutricionista() != null && refeicao.getNutricionista().getEstabelecimento() != null)
                    ? refeicao.getNutricionista().getEstabelecimento().getId()
                    : null;
            if (estabUsuario == null || !estabUsuario.equals(estabRefeicao)) {
                throw new IllegalArgumentException("Você não tem permissão para acessar esta refeição.");
            }
            return;
        }
        validarPropriedadeRefeicao(refeicao, usuario.getId());
    }

    @Transactional
    @RegistrarAcao(acao = TipoAcao.ARQUIVOU_REFEICAO)
    public void atualizaStatus(@NonNull Long id) {
        Refeicao refeicaoAlvo = null;
        Long idIgnorado = id;
        Refeicao refeicao = refeicaoAlvo != null ? refeicaoAlvo : findRefeicaoById(id);
        refeicao.setStatus(refeicao.getStatus() == Status.ATIVA ? Status.INATIVA : Status.ATIVA);
        refeicaoRepository.save(refeicao);
    }
}