package com.example.sistemanutricao.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.FichasPorRefeicaoId;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.record.RefeicaoDTO.FichaTecnicaRefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;

import java.math.BigDecimal;
import org.mapstruct.factory.Mappers;

class RefeicaoMapperTest {

    private final RefeicaoMapper mapper = Mappers.getMapper(RefeicaoMapper.class);

    private FichasPorRefeicao buildFichaPorRefeicao(Refeicao refeicao, Long fichaId, String prepNome, BigDecimal vtc) {
        Preparacao prep = new Preparacao();
        prep.setNome(prepNome);

        PerfilNutricional perfil = new PerfilNutricional();
        perfil.setVtc(vtc);

        FichaTecnica ft = new FichaTecnica();
        ft.setId(fichaId);
        ft.setPreparacao(prep);
        ft.setPerfilNutricional(perfil);

        FichasPorRefeicao fpr = new FichasPorRefeicao();
        fpr.setRefeicao(refeicao);
        fpr.setFichaTecnica(ft);
        fpr.setId(new FichasPorRefeicaoId(refeicao.getId(), fichaId));
        return fpr;
    }

    @Test
    void shouldMapRefeicaoToResponseDTO() {
        Refeicao refeicao = new Refeicao();
        refeicao.setId(1L);
        refeicao.setNome("Almoço");
        refeicao.setKcalTotal("600");
        refeicao.setStatus(Status.ATIVA);
        refeicao.setFichasPorRefeicao(new ArrayList<>());

        FichasPorRefeicao fpr = buildFichaPorRefeicao(refeicao, 10L, "Frango Grelhado", new BigDecimal("350"));
        refeicao.getFichasPorRefeicao().add(fpr);

        RefeicaoResponseDTO dto = mapper.toResponseDTO(refeicao);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("Almoço");
        assertThat(dto.kcalTotal()).isEqualTo("600");
        assertThat(dto.status()).isEqualTo(Status.ATIVA);
        assertThat(dto.fichasTecnicas()).hasSize(1);
        assertThat(dto.fichasTecnicas().get(0).id()).isEqualTo(10L);
        assertThat(dto.fichasTecnicas().get(0).nomePreparacao()).isEqualTo("Frango Grelhado");
    }

    @Test
    void shouldMapEmptyFichasListToEmptyDTO() {
        Refeicao refeicao = new Refeicao();
        refeicao.setId(2L);
        refeicao.setNome("Jantar");
        refeicao.setKcalTotal("400");
        refeicao.setStatus(Status.INATIVA);
        refeicao.setFichasPorRefeicao(new ArrayList<>());

        RefeicaoResponseDTO dto = mapper.toResponseDTO(refeicao);

        assertThat(dto.fichasTecnicas()).isEmpty();
    }

    @Test
    void shouldMapMultipleFichasToList() {
        Refeicao refeicao = new Refeicao();
        refeicao.setId(3L);
        refeicao.setNome("Lanche");
        refeicao.setKcalTotal("200");
        refeicao.setStatus(Status.ATIVA);
        refeicao.setFichasPorRefeicao(new ArrayList<>());

        refeicao.getFichasPorRefeicao().add(buildFichaPorRefeicao(refeicao, 20L, "Iogurte", new BigDecimal("120")));
        refeicao.getFichasPorRefeicao().add(buildFichaPorRefeicao(refeicao, 21L, "Fruta", new BigDecimal("80")));

        List<FichaTecnicaRefeicaoDTO> fichas = mapper.toFichasDTOList(refeicao.getFichasPorRefeicao());

        assertThat(fichas).hasSize(2);
        assertThat(fichas).extracting(FichaTecnicaRefeicaoDTO::id).containsExactly(20L, 21L);
    }
}
