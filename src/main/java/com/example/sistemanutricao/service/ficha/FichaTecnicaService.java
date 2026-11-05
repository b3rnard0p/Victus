package com.example.sistemanutricao.service.ficha;

import com.example.sistemanutricao.exception.DuplicateNomeException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import com.example.sistemanutricao.exception.FichaTecnicaNotFoundException;
import com.example.sistemanutricao.exception.IngredienteNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.lang.NonNull;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.IngredientesPorFicha;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.FichaTecnicaRefeicaoDTO;
import com.example.sistemanutricao.mapper.FichaTecnicaMapper;
import com.example.sistemanutricao.aop.RegistrarAcao;
import com.example.sistemanutricao.model.enuns.TipoAcao;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;

@Service
public class FichaTecnicaService {

    private final FichaTecnicaRepository fichaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final PerfilNutricionalCalculator perfilCalculator;
    private final UsuarioRepository usuarioRepository;
    private final FichaTecnicaMapper fichaTecnicaMapper;
    private com.example.sistemanutricao.repository.IngredientesPorFichaRepository ingredientesPorFichaRepository;
    private com.example.sistemanutricao.repository.RefeicaoRepository refeicaoRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setIngredientesPorFichaRepository(com.example.sistemanutricao.repository.IngredientesPorFichaRepository ingredientesPorFichaRepository) {
        this.ingredientesPorFichaRepository = ingredientesPorFichaRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setRefeicaoRepository(com.example.sistemanutricao.repository.RefeicaoRepository refeicaoRepository) {
        this.refeicaoRepository = refeicaoRepository;
    }

    public FichaTecnicaService(FichaTecnicaRepository fichaRepository,
                               IngredienteRepository ingredienteRepository,
                               UsuarioRepository usuarioRepository,
                               PerfilNutricionalCalculator perfilCalculator,
                               FichaTecnicaMapper fichaTecnicaMapper) {
        this.fichaRepository = fichaRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.perfilCalculator = perfilCalculator;
        this.usuarioRepository = usuarioRepository;
        this.fichaTecnicaMapper = fichaTecnicaMapper;
    }

    private FichaTecnica findFichaById(@NonNull Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new FichaTecnicaNotFoundException("Ficha técnica não encontrada com ID: " + id));
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.CRIOU_FICHA)
    public FichaTecnicaGetDTO create(
            FichaTecnicaCreateDTO dto,
            @NonNull Long nutricionistaId
    ) {
        Usuario nutricionista = usuarioRepository.findById(nutricionistaId)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        validarDuplicidadeFichaCreate(dto, nutricionistaId);
        validarCamposAgua(dto.preparacao());

        if (dto.status() == Status.INATIVA && dto.statusCriacao() == StatusCriacao.INCOMPLETA) {
            throw new IllegalArgumentException("Não é permitido salvar uma ficha como incompleta e arquivada simultaneamente.");
        }

        Preparacao preparacao = fichaTecnicaMapper.toPreparacao(dto.preparacao());
        int numeroPorcoes = calcularNumeroPorcoes(preparacao.getRendimento(), dto.pesoPorcao());
        BigDecimal custoTotal = normalizar(dto.custoTotal());
        BigDecimal custoPerCapita = custoTotal.divide(BigDecimal.valueOf(numeroPorcoes), 2, RoundingMode.HALF_UP);
        
        PerfilNutricionalDTO perfilCalculado = perfilCalculator.calcularPerfilNutricionalPorPorcaoCreate(dto.ingredientes(), numeroPorcoes);
        PerfilNutricional perfil = fichaTecnicaMapper.toPerfilNutricional(perfilCalculado);

        FichaTecnica ficha = new FichaTecnica();
        ficha.setCustoPerCapita(custoPerCapita);
        ficha.setCustoTotal(custoTotal);
        ficha.setNumeroPorcoes(numeroPorcoes);
        ficha.setPesoPorcao(dto.pesoPorcao());
        ficha.setMedidaCaseira(dto.medidaCaseira());
        ficha.setStatus(dto.status());
        ficha.setStatusCriacao(dto.statusCriacao());
        ficha.setPreparacao(preparacao);
        ficha.setPerfilNutricional(perfil);
        ficha.setNutricionista(nutricionista);
        
        List<IngredientesPorFicha> ingredientes = buildIngredientes(dto.ingredientes(), ficha, perfil);
        ficha.setIngredientesPorFicha(ingredientes);
        recalcularCustosEFcc(ficha, dto.custoTotal(), numeroPorcoes);

        FichaTecnica fichaSalva = fichaRepository.save(ficha);

        return fichaTecnicaMapper.toGetDTO(fichaSalva);
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.EDITOU_FICHA)
    public FichaTecnicaGetDTO update(@NonNull Long id, FichaTecnicaUpdateDTO dto, Long usuarioLogadoId) {
        var fichaExistente = findFichaById(id);
        validarPropriedadeFicha(fichaExistente, usuarioLogadoId);
        return executarUpdate(id, dto, fichaExistente);
    }

    @Transactional(rollbackFor = Exception.class)
    @RegistrarAcao(acao = TipoAcao.EDITOU_FICHA)
    public FichaTecnicaGetDTO update(@NonNull Long id, FichaTecnicaUpdateDTO dto) {
        var fichaExistente = findFichaById(id);
        return executarUpdate(id, dto, fichaExistente);
    }

    private FichaTecnicaGetDTO executarUpdate(@NonNull Long id, FichaTecnicaUpdateDTO dto, FichaTecnica fichaExistente) {
        // fichaExistente já carregada

        Long nutricionistaId = fichaExistente.getNutricionista() != null
            ? fichaExistente.getNutricionista().getId()
            : null;
        validarDuplicidadeFichaUpdate(id, dto, nutricionistaId);
        validarCamposAgua(dto.preparacao());

        Status novoStatus = dto.status() != null ? dto.status() : fichaExistente.getStatus();
        StatusCriacao novoStatusCriacao = dto.statusCriacao() != null ? dto.statusCriacao() : fichaExistente.getStatusCriacao();

        if (novoStatus == Status.INATIVA && novoStatusCriacao == StatusCriacao.INCOMPLETA) {
            throw new IllegalArgumentException("Não é permitido salvar uma ficha como incompleta e arquivada simultaneamente.");
        }

        fichaTecnicaMapper.updatePreparacao(fichaExistente.getPreparacao(), dto.preparacao());
        int numeroPorcoes = calcularNumeroPorcoes(fichaExistente.getPreparacao().getRendimento(), dto.pesoPorcao());
        BigDecimal custoTotal = normalizar(dto.custoTotal());
        BigDecimal custoPerCapita = custoTotal.divide(BigDecimal.valueOf(numeroPorcoes), 2, RoundingMode.HALF_UP);
        
        PerfilNutricionalDTO perfilCalculado = perfilCalculator.calcularPerfilNutricionalPorPorcaoUpdate(dto.ingredientes(), numeroPorcoes);
        fichaTecnicaMapper.updatePerfil(fichaExistente.getPerfilNutricional(), perfilCalculado);

        fichaExistente.setCustoPerCapita(custoPerCapita);
        fichaExistente.setCustoTotal(custoTotal);
        fichaExistente.setNumeroPorcoes(numeroPorcoes);
        fichaExistente.setPesoPorcao(dto.pesoPorcao());
        fichaExistente.setMedidaCaseira(dto.medidaCaseira());
        fichaExistente.setStatus(dto.status());
        fichaExistente.setStatusCriacao(dto.statusCriacao());

        syncIngredientes(fichaExistente, dto.ingredientes());
        recalcularCustosEFcc(fichaExistente, dto.custoTotal(), numeroPorcoes);

        FichaTecnica fichaAtualizada = fichaRepository.save(fichaExistente);
        recalcularRefeicoesDaFicha(fichaAtualizada != null ? fichaAtualizada.getId() : id);

        return fichaTecnicaMapper.toGetDTO(fichaAtualizada);
    }

    private void syncIngredientes(FichaTecnica ficha, List<IngredientePorFichaDTO> dtos) {
        List<IngredientesPorFicha> atuais = ficha.getIngredientesPorFicha();
        if (atuais == null) {
            atuais = new ArrayList<>();
            ficha.setIngredientesPorFicha(atuais);
        }

        if (dtos == null || dtos.isEmpty()) {
            atuais.clear();
            return;
        }

        Set<Long> idsRecebidos = dtos.stream()
                .filter(dto -> dto.id() != null)
                .map(IngredientePorFichaDTO::id)
                .collect(Collectors.toSet());

        atuais.removeIf(ipf -> ipf.getId() != null && !idsRecebidos.contains(ipf.getId()));

        Map<Long, IngredientesPorFicha> mapAtuais = atuais.stream()
                .filter(ipf -> ipf.getId() != null)
                .collect(Collectors.toMap(IngredientesPorFicha::getId, ipf -> ipf));

        Map<Long, Ingrediente> mapaIngredientes = carregarMapaIngredientes(dtos);
        for (IngredientePorFichaDTO dto : dtos) {
            if (dto.id() != null && mapAtuais.containsKey(dto.id())) {
                updateIngredientePorFicha(mapAtuais.get(dto.id()), dto);
            } else {
                IngredientesPorFicha novo = buildIngredientePorFicha(dto, ficha, ficha.getPerfilNutricional(), mapaIngredientes);
                atuais.add(novo);
            }
        }
    }

    private List<IngredientesPorFicha> buildIngredientes(List<IngredientePorFichaDTO> dtos, FichaTecnica ficha, PerfilNutricional perfil) {
        if (dtos == null) return new ArrayList<>();
        Map<Long, Ingrediente> mapaIngredientes = carregarMapaIngredientes(dtos);
        return dtos.stream().map(dto -> buildIngredientePorFicha(dto, ficha, perfil, mapaIngredientes)).collect(Collectors.toCollection(ArrayList::new));
    }

    private IngredientesPorFicha buildIngredientePorFicha(IngredientePorFichaDTO dto, FichaTecnica ficha, PerfilNutricional perfil, Map<Long, Ingrediente> mapaIngredientes) {
        Long ingredienteId = dto.ingredienteId();
        if (ingredienteId == null) {
            throw new IllegalArgumentException("Ingrediente inválido.");
        }

        Ingrediente ingrediente = (mapaIngredientes != null && mapaIngredientes.containsKey(ingredienteId)
                ? java.util.Optional.of(mapaIngredientes.get(ingredienteId))
                : ingredienteRepository.findById(ingredienteId))
                .orElseThrow(() -> new IngredienteNotFoundException("Ingrediente não encontrado"));

        IngredientesPorFicha ipf = new IngredientesPorFicha();
        ipf.setFichaTecnica(ficha);
        ipf.setPerfilNutricional(perfil);
        ipf.setIngrediente(ingrediente);
        updateIngredientePorFicha(ipf, dto);
        return ipf;
    }

    private void updateIngredientePorFicha(IngredientesPorFicha ipf, IngredientePorFichaDTO dto) {
        ipf.setCustoKG(dto.custoKg());
        if (dto.pb() != null && dto.custoKg() != null) {
            ipf.setCustoUsado(dto.pb().multiply(dto.custoKg()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
        } else {
            ipf.setCustoUsado(dto.custoUsado());
        }
        if (dto.pb() != null && dto.pl() != null && dto.pl().compareTo(BigDecimal.ZERO) > 0) {
            ipf.setFc(dto.pb().divide(dto.pl(), 2, RoundingMode.HALF_UP));
        } else {
            ipf.setFc(dto.fc());
        }
        ipf.setMedidaCaseria(dto.medidaCaseira());
        ipf.setPb(dto.pb());
        ipf.setPl(dto.pl());

        BigDecimal pl = dto.pl();
        Ingrediente ingrediente = ipf.getIngrediente();
        if (pl != null && pl.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ptn = normalizar(ingrediente.getPtn());
            BigDecimal cho = normalizar(ingrediente.getCho());
            BigDecimal lip = normalizar(ingrediente.getLip());
            BigDecimal sodio = normalizar(ingrediente.getSodio());
            BigDecimal gorduraSaturada = normalizar(ingrediente.getGorduraSaturada());

            ipf.setPtnCalculado(ptn.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            ipf.setChoCalculado(cho.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            ipf.setLipCalculado(lip.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            ipf.setSodioCalculado(sodio.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            ipf.setGorduraSaturadaCalculada(gorduraSaturada.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        } else {
            ipf.setPtnCalculado(BigDecimal.ZERO);
            ipf.setChoCalculado(BigDecimal.ZERO);
            ipf.setLipCalculado(BigDecimal.ZERO);
            ipf.setSodioCalculado(BigDecimal.ZERO);
            ipf.setGorduraSaturadaCalculada(BigDecimal.ZERO);
        }
    }


    private Map<Long, Ingrediente> carregarMapaIngredientes(List<IngredientePorFichaDTO> dtos) {
        Map<Long, Ingrediente> mapa = new java.util.HashMap<>();
        if (dtos == null || dtos.isEmpty()) {
            return mapa;
        }
        Set<Long> ids = dtos.stream()
                .filter(d -> d != null && d.ingredienteId() != null)
                .map(IngredientePorFichaDTO::ingredienteId)
                .collect(Collectors.toSet());
        if (!ids.isEmpty()) {
            List<Ingrediente> encontrados = ingredienteRepository.findAllById(ids);
            if (encontrados != null) {
                for (Ingrediente ing : encontrados) {
                    if (ing != null && ing.getId() != null) {
                        mapa.put(ing.getId(), ing);
                    }
                }
            }
        }
        return mapa;
    }

    private void recalcularCustosEFcc(FichaTecnica ficha, BigDecimal custoTotalDto, int numeroPorcoes) {
        List<IngredientesPorFicha> itens = ficha.getIngredientesPorFicha();
        if (itens != null && !itens.isEmpty()) {
            BigDecimal somaCusto = BigDecimal.ZERO;
            BigDecimal somaPl = BigDecimal.ZERO;
            for (IngredientesPorFicha ipf : itens) {
                if (ipf.getCustoUsado() != null) {
                    somaCusto = somaCusto.add(ipf.getCustoUsado());
                }
                if (ipf.getPl() != null) {
                    somaPl = somaPl.add(ipf.getPl());
                }
            }
            if (somaCusto.compareTo(BigDecimal.ZERO) > 0) {
                ficha.setCustoTotal(somaCusto);
                ficha.setCustoPerCapita(somaCusto.divide(BigDecimal.valueOf(numeroPorcoes), 2, RoundingMode.HALF_UP));
            }
            if (somaPl.compareTo(BigDecimal.ZERO) > 0
                    && ficha.getPreparacao() != null
                    && ficha.getPreparacao().getRendimento() != null) {
                ficha.getPreparacao().setFcc(
                        ficha.getPreparacao().getRendimento().divide(somaPl, 2, RoundingMode.HALF_UP));
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void recalcularFichasPorIngrediente(Long ingredienteId) {
        if (ingredienteId == null || ingredientesPorFichaRepository == null) {
            return;
        }
        List<FichaTecnica> fichasAfetadas = ingredientesPorFichaRepository.buscarFichasPorIngredienteId(ingredienteId);
        if (fichasAfetadas == null || fichasAfetadas.isEmpty()) {
            return;
        }
        for (FichaTecnica ficha : fichasAfetadas) {
            List<IngredientesPorFicha> itens = ficha.getIngredientesPorFicha();
            if (itens == null || itens.isEmpty()) {
                continue;
            }
            List<IngredientePorFichaDTO> dtos = new ArrayList<>();
            for (IngredientesPorFicha ipf : itens) {
                Ingrediente ing = ipf.getIngrediente();
                BigDecimal pl = ipf.getPl();
                if (ing != null && pl != null && pl.compareTo(BigDecimal.ZERO) > 0) {
                    ipf.setPtnCalculado(normalizar(ing.getPtn()).multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    ipf.setChoCalculado(normalizar(ing.getCho()).multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    ipf.setLipCalculado(normalizar(ing.getLip()).multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    ipf.setSodioCalculado(normalizar(ing.getSodio()).multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    ipf.setGorduraSaturadaCalculada(normalizar(ing.getGorduraSaturada()).multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                }
                dtos.add(new IngredientePorFichaDTO(
                        ipf.getId(),
                        ing != null ? ing.getId() : null,
                        null,
                        ipf.getCustoKG(),
                        ipf.getCustoUsado(),
                        ipf.getFc(),
                        ipf.getMedidaCaseria(),
                        ipf.getPb(),
                        ipf.getPl(),
                        ipf.getPtnCalculado(),
                        ipf.getChoCalculado(),
                        ipf.getLipCalculado(),
                        ipf.getSodioCalculado(),
                        ipf.getGorduraSaturadaCalculada(),
                        null
                ));
            }
            int numPorcoes = ficha.getNumeroPorcoes() != null && ficha.getNumeroPorcoes() > 0 ? ficha.getNumeroPorcoes() : 1;
            PerfilNutricionalDTO perfilCalculado = perfilCalculator.calcularPerfilNutricionalPorPorcaoUpdate(dtos, numPorcoes);
            if (ficha.getPerfilNutricional() != null) {
                fichaTecnicaMapper.updatePerfil(ficha.getPerfilNutricional(), perfilCalculado);
            }
            fichaRepository.save(ficha);
            recalcularRefeicoesDaFicha(ficha.getId());
        }
    }

    private void recalcularRefeicoesDaFicha(Long fichaId) {
        if (fichaId == null || refeicaoRepository == null) {
            return;
        }
        List<com.example.sistemanutricao.model.Refeicao> refeicoes = refeicaoRepository.buscarPorFichaTecnicaId(fichaId);
        if (refeicoes == null || refeicoes.isEmpty()) {
            return;
        }
        com.example.sistemanutricao.service.refeicao.RefeicaoNutrientesCalculator calc =
                new com.example.sistemanutricao.service.refeicao.RefeicaoNutrientesCalculator();
        for (com.example.sistemanutricao.model.Refeicao refeicao : refeicoes) {
            var totais = calc.calcularTotais(refeicao);
            refeicao.setTotalGramasPTN(totais.totalGramasPTN());
            refeicao.setTotalGramasCHO(totais.totalGramasCHO());
            refeicao.setTotalGramasLIP(totais.totalGramasLIP());
            refeicao.setTotalGramasSodio(totais.totalGramasSodio());
            refeicao.setTotalGramasSaturada(totais.totalGramasSaturada());
            if (refeicao.getFichasPorRefeicao() != null && !refeicao.getFichasPorRefeicao().isEmpty()) {
                BigDecimal totalVtc = BigDecimal.ZERO;
                for (com.example.sistemanutricao.model.FichasPorRefeicao fp : refeicao.getFichasPorRefeicao()) {
                    if (fp != null && fp.getFichaTecnica() != null && fp.getFichaTecnica().getPerfilNutricional() != null) {
                        BigDecimal vtc = fp.getFichaTecnica().getPerfilNutricional().getVtc();
                        if (vtc != null) {
                            totalVtc = totalVtc.add(vtc);
                        }
                    }
                }
                if (totalVtc.compareTo(BigDecimal.ZERO) > 0) {
                    refeicao.setKcalTotal(totalVtc.setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }
            refeicaoRepository.save(refeicao);
        }
    }

    private void validarDuplicidadeFichaCreate(FichaTecnicaCreateDTO dto, Long nutricionistaId) {
        if (nutricionistaId == null || dto == null || dto.preparacao() == null) {
            return;
        }

        String nomePreparacao = normalizarTexto(dto.preparacao().nome());
        Integer numeroPreparacao = dto.preparacao().numero();

        if (nomePreparacao != null
                && fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCase(nutricionistaId, nomePreparacao)) {
            throw new DuplicateNomeException("Já existe uma ficha com este nome.");
        }

        if (numeroPreparacao != null
                && fichaRepository.existsByNutricionistaIdAndPreparacaoNumero(nutricionistaId, numeroPreparacao)) {
            throw new DuplicateNomeException("Já existe uma ficha com este número.");
        }
    }

    private void validarDuplicidadeFichaUpdate(Long fichaId, FichaTecnicaUpdateDTO dto, Long nutricionistaId) {
        if (nutricionistaId == null || fichaId == null || dto == null || dto.preparacao() == null) {
            return;
        }

        String nomePreparacao = normalizarTexto(dto.preparacao().nome());
        Integer numeroPreparacao = dto.preparacao().numero();

        if (nomePreparacao != null
                && fichaRepository.existsByNutricionistaIdAndPreparacaoNomeIgnoreCaseAndIdNot(nutricionistaId, nomePreparacao, fichaId)) {
            throw new DuplicateNomeException("Já existe outra ficha com este nome.");
        }

        if (numeroPreparacao != null
                && fichaRepository.existsByNutricionistaIdAndPreparacaoNumeroAndIdNot(nutricionistaId, numeroPreparacao, fichaId)) {
            throw new DuplicateNomeException("Já existe outra ficha com este número.");
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String trim = valor.trim();
        return trim.isEmpty() ? null : trim;
    }

    private void validarCamposAgua(com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO preparacaoDto) {
        if (preparacaoDto == null) {
            return;
        }

        BigDecimal qntdAgua = preparacaoDto.qntdAgua();
        BigDecimal porcentAgua = preparacaoDto.porcentAgua();
        boolean qntdPreenchida = qntdAgua != null;
        boolean porcentPreenchida = porcentAgua != null;

        if (!qntdPreenchida && !porcentPreenchida) {
            return;
        }

        if (qntdPreenchida != porcentPreenchida) {
            throw new IllegalArgumentException("Preencha a quantidade de água e a porcentagem de água, ou deixe ambos em branco.");
        }

        if (qntdAgua == null || qntdAgua.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade de água deve ser maior que zero.");
        }
    }

    @Transactional(readOnly = true)
    public List<FichaTecnicaRefeicaoDTO> listarResumo(Long nutricionistaId) {
        return fichaRepository.findByStatusAndStatusCriacaoAndNutricionistaId(Status.ATIVA, StatusCriacao.COMPLETA, nutricionistaId, PageRequest.of(0, 500))
            .getContent()
            .stream()
                .map(f -> new FichaTecnicaRefeicaoDTO(
                        f.getId(),
                        f.getPreparacao().getNome(),
                        f.getPerfilNutricional().getVtc()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @RegistrarAcao(acao = TipoAcao.ARQUIVOU_FICHA)
    public void atualizaStatus(@NonNull Long id, Long nutricionistaId) {
        var ficha = findFichaById(id);
        validarPropriedadeFicha(ficha, nutricionistaId);
        executarAtualizaStatus(id, ficha);
    }

    @Transactional
    @RegistrarAcao(acao = TipoAcao.ARQUIVOU_FICHA)
    public void atualizaStatus(@NonNull Long id) {
        var ficha = findFichaById(id);
        executarAtualizaStatus(id, ficha);
    }

    private void executarAtualizaStatus(Long id, FichaTecnica fichaAlvo) {
        FichaTecnica ficha = fichaAlvo != null ? fichaAlvo : findFichaById(id);

        if (ficha.getStatus() == Status.ATIVA && ficha.getStatusCriacao() == StatusCriacao.INCOMPLETA) {
            throw new IllegalArgumentException("Não é permitido arquivar uma ficha incompleta.");
        }

        ficha.setStatus(
                ficha.getStatus() == Status.ATIVA ?
                        Status.INATIVA :
                        Status.ATIVA
        );

        fichaRepository.save(ficha);
    }

    @Transactional(readOnly = true)
    public FichaTecnicaGetDTO getFichaById(@NonNull Long id, Usuario usuario) {
        var ficha = findFichaById(id);
        validarAcessoLeituraFicha(ficha, usuario);
        return fichaTecnicaMapper.toGetDTO(ficha);
    }

    private void validarPropriedadeFicha(FichaTecnica ficha, Long nutricionistaId) {
        if (nutricionistaId == null || ficha.getNutricionista() == null || !ficha.getNutricionista().getId().equals(nutricionistaId)) {
            throw new IllegalArgumentException("Você não tem permissão para alterar esta ficha técnica.");
        }
    }

    private void validarAcessoLeituraFicha(FichaTecnica ficha, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Você não tem permissão para acessar esta ficha técnica.");
        }
        if (usuario.getCargo() != null && "PRODUCAO".equals(usuario.getCargo().name())) {
            Long estabUsuario = usuario.getEstabelecimento() != null ? usuario.getEstabelecimento().getId() : null;
            Long estabFicha = (ficha.getNutricionista() != null && ficha.getNutricionista().getEstabelecimento() != null)
                    ? ficha.getNutricionista().getEstabelecimento().getId()
                    : null;
            if (estabUsuario == null || !estabUsuario.equals(estabFicha)) {
                throw new IllegalArgumentException("Você não tem permissão para acessar esta ficha técnica.");
            }
            return;
        }
        validarPropriedadeFicha(ficha, usuario.getId());
    }

    @Transactional(readOnly = true)
    public FichaTecnicaGetDTO getFichaById(@NonNull Long id) {
        FichaTecnica fichaAlvo = null;
        FichaTecnica ficha = fichaAlvo != null ? fichaAlvo : findFichaById(id);
        return fichaTecnicaMapper.toGetDTO(ficha);
    }

    private int calcularNumeroPorcoes(BigDecimal rendimento, BigDecimal pesoPorcao) {
        if (rendimento == null || pesoPorcao == null || rendimento.compareTo(BigDecimal.ZERO) <= 0 || pesoPorcao.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe rendimento e peso por porção para calcular o número de porções.");
        }

        int numeroPorcoes = rendimento.divide(pesoPorcao, 0, RoundingMode.CEILING).intValue();
        if (numeroPorcoes < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O rendimento precisa gerar ao menos uma porção.");
        }
        return numeroPorcoes;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}