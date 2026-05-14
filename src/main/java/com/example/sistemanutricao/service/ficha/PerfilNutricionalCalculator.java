package com.example.sistemanutricao.service.ficha;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;

@Component
public class PerfilNutricionalCalculator {

    private final IngredienteRepository ingredienteRepository;

    public PerfilNutricionalCalculator(IngredienteRepository ingredienteRepository) {
        this.ingredienteRepository = ingredienteRepository;
    }

    public PerfilNutricionalDTO calcularPerfilNutricionalPorPorcaoCreate(List<IngredientePorFichaDTO> ingredientes, int numeroPorcoes) {
        TotaisNutrientes totais = calcularTotaisNutrientes(ingredientes);
        return new PerfilNutricionalDTO(
            null,
            dividir(totais.vtc(), numeroPorcoes),
            dividir(totais.kcalPTN(), numeroPorcoes),
            dividir(totais.kcalCHO(), numeroPorcoes),
            dividir(totais.kcalLIP(), numeroPorcoes),
            dividir(totais.gramasPTN(), numeroPorcoes),
            dividir(totais.gramasCHO(), numeroPorcoes),
            dividir(totais.gramasLIP(), numeroPorcoes),
            dividir(totais.gramasSodio(), numeroPorcoes),
            dividir(totais.gramasSaturada(), numeroPorcoes),
            totais.porcentPTN(),
            totais.porcentCHO(),
            totais.porcentLIP()
        );
    }

    public PerfilNutricionalDTO calcularPerfilNutricionalPorPorcaoUpdate(List<IngredientePorFichaDTO> ingredientes, int numeroPorcoes) {
        TotaisNutrientes totais = calcularTotaisNutrientes(ingredientes);
        return new PerfilNutricionalDTO(
            null,
            dividir(totais.vtc(), numeroPorcoes),
            dividir(totais.kcalPTN(), numeroPorcoes),
            dividir(totais.kcalCHO(), numeroPorcoes),
            dividir(totais.kcalLIP(), numeroPorcoes),
            dividir(totais.gramasPTN(), numeroPorcoes),
            dividir(totais.gramasCHO(), numeroPorcoes),
            dividir(totais.gramasLIP(), numeroPorcoes),
            dividir(totais.gramasSodio(), numeroPorcoes),
            dividir(totais.gramasSaturada(), numeroPorcoes),
            totais.porcentPTN(),
            totais.porcentCHO(),
            totais.porcentLIP()
        );
    }

    private TotaisNutrientes calcularTotaisNutrientes(List<IngredientePorFichaDTO> ingredientes) {
        BigDecimal gramasPtn = BigDecimal.ZERO;
        BigDecimal gramasCho = BigDecimal.ZERO;
        BigDecimal gramasLip = BigDecimal.ZERO;
        BigDecimal gramasSodio = BigDecimal.ZERO;
        BigDecimal gramasSaturada = BigDecimal.ZERO;

        if (ingredientes == null) {
            return new TotaisNutrientes(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        for (IngredientePorFichaDTO ingrediente : ingredientes) {
            if (ingrediente == null || ingrediente.ingredienteId() == null) {
                continue;
            }

            Long ingredienteId = ingrediente.ingredienteId();
            Ingrediente ingredienteBase = ingredienteRepository.findById(ingredienteId).orElse(null);
            if (ingredienteBase == null) {
                continue;
            }

            BigDecimal pl = normalizar(ingrediente.pl());
            BigDecimal ptn = normalizar(ingredienteBase.getPtn());
            BigDecimal cho = normalizar(ingredienteBase.getCho());
            BigDecimal lip = normalizar(ingredienteBase.getLip());
            BigDecimal sodio = normalizar(ingredienteBase.getSodio());
            BigDecimal saturada = normalizar(ingredienteBase.getGorduraSaturada());

            gramasPtn = gramasPtn.add(ptn.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            gramasCho = gramasCho.add(cho.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            gramasLip = gramasLip.add(lip.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            gramasSodio = gramasSodio.add(sodio.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            gramasSaturada = gramasSaturada.add(saturada.multiply(pl).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }

        BigDecimal kcalPtn = gramasPtn.multiply(BigDecimal.valueOf(4));
        BigDecimal kcalCho = gramasCho.multiply(BigDecimal.valueOf(4));
        BigDecimal kcalLip = gramasLip.multiply(BigDecimal.valueOf(9));
        BigDecimal vtc = kcalPtn.add(kcalCho).add(kcalLip);
        BigDecimal porcentPtn = calcularPercentual(kcalPtn, vtc);
        BigDecimal porcentCho = calcularPercentual(kcalCho, vtc);
        BigDecimal porcentLip = calcularPercentual(kcalLip, vtc);

        return new TotaisNutrientes(vtc, kcalPtn, kcalCho, kcalLip, gramasPtn, gramasCho, gramasLip, gramasSodio, gramasSaturada, porcentPtn, porcentCho, porcentLip);
    }

    private BigDecimal calcularPercentual(BigDecimal valor, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal dividir(BigDecimal valor, int divisor) {
        return divisor > 0 ? valor.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

        private record TotaisNutrientes(
            BigDecimal vtc,
            BigDecimal kcalPTN,
            BigDecimal kcalCHO,
            BigDecimal kcalLIP,
            BigDecimal gramasPTN,
            BigDecimal gramasCHO,
            BigDecimal gramasLIP,
            BigDecimal gramasSodio,
            BigDecimal gramasSaturada,
            BigDecimal porcentPTN,
            BigDecimal porcentCHO,
            BigDecimal porcentLIP
        ) {}
}
