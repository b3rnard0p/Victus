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
    public FichaTecnicaGetDTO create(
            FichaTecnicaCreateDTO dto,
            @NonNull Long nutricionistaId
    ) {
        Usuario nutricionista = usuarioRepository.findById(nutricionistaId)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        validarDuplicidadeFichaCreate(dto, nutricionistaId);
        validarCamposAgua(dto.preparacao());

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

        FichaTecnica fichaSalva = fichaRepository.save(ficha);

        return fichaTecnicaMapper.toGetDTO(fichaSalva);
    }

    @Transactional(rollbackFor = Exception.class)
    public FichaTecnicaGetDTO update(@NonNull Long id, FichaTecnicaUpdateDTO dto) {
        FichaTecnica fichaExistente = findFichaById(id);

        Long nutricionistaId = fichaExistente.getNutricionista() != null
            ? fichaExistente.getNutricionista().getId()
            : null;
        validarDuplicidadeFichaUpdate(id, dto, nutricionistaId);
        validarCamposAgua(dto.preparacao());

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

        FichaTecnica fichaAtualizada = fichaRepository.save(fichaExistente);

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

        for (IngredientePorFichaDTO dto : dtos) {
            if (dto.id() != null && mapAtuais.containsKey(dto.id())) {
                updateIngredientePorFicha(mapAtuais.get(dto.id()), dto);
            } else {
                IngredientesPorFicha novo = buildIngredientePorFicha(dto, ficha, ficha.getPerfilNutricional());
                atuais.add(novo);
            }
        }
    }

    private List<IngredientesPorFicha> buildIngredientes(List<IngredientePorFichaDTO> dtos, FichaTecnica ficha, PerfilNutricional perfil) {
        if (dtos == null) return new ArrayList<>();
        return dtos.stream().map(dto -> buildIngredientePorFicha(dto, ficha, perfil)).collect(Collectors.toCollection(ArrayList::new));
    }

    private IngredientesPorFicha buildIngredientePorFicha(IngredientePorFichaDTO dto, FichaTecnica ficha, PerfilNutricional perfil) {
        Long ingredienteId = dto.ingredienteId();
        if (ingredienteId == null) {
            throw new IllegalArgumentException("Ingrediente inválido.");
        }

        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId)
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
        ipf.setCustoUsado(dto.custoUsado());
        ipf.setFc(dto.fc());
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
    public List<FichaTecnicaRefeicaoDTO> listarResumo() {
        return fichaRepository.findByStatusAndStatusCriacao(Status.ATIVA, StatusCriacao.COMPLETA, PageRequest.of(0, 500))
            .getContent()
            .stream()
                .map(f -> new FichaTecnicaRefeicaoDTO(
                        f.getId(),
                        f.getPreparacao().getNome(),
                        f.getPerfilNutricional().getVtc()
                ))
                .collect(Collectors.toList());
    }

    public void atualizaStatus(@NonNull Long id) {
        FichaTecnica ficha = findFichaById(id);

        ficha.setStatus(
                ficha.getStatus() == Status.ATIVA ?
                        Status.INATIVA :
                        Status.ATIVA
        );

        fichaRepository.save(ficha);
    }

    @Transactional(readOnly = true)
    public FichaTecnicaGetDTO getFichaById(@NonNull Long id) {
        FichaTecnica ficha = findFichaById(id);
        return fichaTecnicaMapper.toGetDTO(ficha);
    }

    private int calcularNumeroPorcoes(BigDecimal rendimento, BigDecimal pesoPorcao) {
        if (rendimento == null || pesoPorcao == null || rendimento.compareTo(BigDecimal.ZERO) <= 0 || pesoPorcao.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe rendimento e peso por porção para calcular o número de porções.");
        }

        int numeroPorcoes = rendimento.divide(pesoPorcao, 0, RoundingMode.DOWN).intValue();
        if (numeroPorcoes < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O rendimento precisa gerar ao menos uma porção.");
        }
        return numeroPorcoes;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}