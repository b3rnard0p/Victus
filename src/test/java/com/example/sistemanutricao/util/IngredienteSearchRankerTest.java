package com.example.sistemanutricao.util;

import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredienteSearchRankerTest {

    private IngredienteGetDTO criar(Long id, String nome, Long usuarioId) {
        return new IngredienteGetDTO(
                id, nome, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                Status.ATIVA, BigDecimal.ONE, BigDecimal.ONE, usuarioId
        );
    }

    @Test
    @DisplayName("Deve priorizar correspondência exata sobre nomes mais longos contendo o termo")
    void devePriorizarCorrespondenciaExata() {
        IngredienteGetDTO boloComOvo = criar(1L, "Bolo de chocolate com ovo", 99L);
        IngredienteGetDTO ovoExato = criar(2L, "Ovo", 99L);
        IngredienteGetDTO ovoGalinha = criar(3L, "Ovo, de galinha, cru", 99L);

        List<IngredienteGetDTO> ordenados = IngredienteSearchRanker.ordenarPorRelevancia(
                List.of(boloComOvo, ovoGalinha, ovoExato), "ovo", 1L
        );

        assertEquals("Ovo", ordenados.get(0).nome());
        assertEquals("Ovo, de galinha, cru", ordenados.get(1).nome());
        assertEquals("Bolo de chocolate com ovo", ordenados.get(2).nome());
    }

    @Test
    @DisplayName("Deve priorizar itens que começam com o termo pesquisado")
    void devePriorizarInicioDoNome() {
        IngredienteGetDTO farinhaArroz = criar(1L, "Farinha, de arroz", 99L);
        IngredienteGetDTO arrozBranco = criar(2L, "Arroz, tipo 1, cozido", 99L);
        IngredienteGetDTO biscoitoArroz = criar(3L, "Biscoito sabor arroz integral", 99L);

        List<IngredienteGetDTO> ordenados = IngredienteSearchRanker.ordenarPorRelevancia(
                List.of(farinhaArroz, biscoitoArroz, arrozBranco), "arroz", 1L
        );

        assertEquals("Arroz, tipo 1, cozido", ordenados.get(0).nome());
        assertEquals("Farinha, de arroz", ordenados.get(1).nome());
        assertEquals("Biscoito sabor arroz integral", ordenados.get(2).nome());
    }

    @Test
    @DisplayName("Deve ser insensível a acentos (diacríticos)")
    void deveIgnorarAcentosNaBusca() {
        IngredienteGetDTO acucar = criar(1L, "Açúcar, cristal", 99L);
        IngredienteGetDTO sal = criar(2L, "Sal, refinado", 99L);

        List<IngredienteGetDTO> ordenados = IngredienteSearchRanker.ordenarPorRelevancia(
                List.of(sal, acucar), "acucar", 1L
        );

        assertEquals("Açúcar, cristal", ordenados.get(0).nome());
    }

    @Test
    @DisplayName("Deve priorizar ingredientes do próprio usuário em caso de proximidade")
    void deveDarBonusParaIngredienteDoUsuario() {
        IngredienteGetDTO tacoArroz = criar(1L, "Arroz integral", 99L);
        IngredienteGetDTO userArroz = criar(2L, "Arroz integral", 10L);

        List<IngredienteGetDTO> ordenados = IngredienteSearchRanker.ordenarPorRelevancia(
                List.of(tacoArroz, userArroz), "arroz integral", 10L
        );

        assertEquals(2L, ordenados.get(0).id());
    }

    @Test
    @DisplayName("Deve lidar com listas vazias e consultas nulas com segurança")
    void deveLidarComValoresNulosOuVazios() {
        assertTrue(IngredienteSearchRanker.ordenarPorRelevancia(null, "arroz", 1L).isEmpty());
        assertTrue(IngredienteSearchRanker.ordenarPorRelevancia(List.of(), "arroz", 1L).isEmpty());
    }
}

