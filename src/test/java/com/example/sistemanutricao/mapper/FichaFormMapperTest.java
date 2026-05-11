package com.example.sistemanutricao.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.model.enuns.Categoria;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import org.mapstruct.factory.Mappers;

class FichaFormMapperTest {

    @Test
    void toUpdateDTO_copiesFields() {
        FichaFormMapper mapper = Mappers.getMapper(FichaFormMapper.class);

        IngredienteGetDTO ing = new IngredienteGetDTO(1L, "Sal", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, Status.ATIVA, BigDecimal.ZERO, BigDecimal.ZERO, 1L);
        IngredientePorFichaDTO ipf = new IngredientePorFichaDTO(10L, 1L, ing, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "g", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);

        PreparacaoGetDTO prep = new PreparacaoGetDTO(2L, "Cozinhar", 1, "10m", "Panela", "Ferver", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, Categoria.PRATOPRINCIPAL);
        PerfilNutricionalGetDTO perfil = new PerfilNutricionalGetDTO(3L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        FichaTecnicaGetDTO fichaGet = new FichaTecnicaGetDTO(5L, BigDecimal.TEN, BigDecimal.TEN, "colher", 2, BigDecimal.ONE, Status.ATIVA, StatusCriacao.COMPLETA, prep, List.of(ipf), perfil);


        FichaTecnicaUpdateDTO dto = mapper.toUpdateDTO(fichaGet);

        assertNotNull(dto);
        assertEquals(fichaGet.id(), dto.id());
        assertEquals(fichaGet.custoTotal(), dto.custoTotal());
        assertEquals(fichaGet.preparacao().nome(), dto.preparacao().nome());
        assertEquals(1, dto.ingredientes().size());
        assertEquals(fichaGet.perfilNutricional().id(), dto.perfilNutricional().id());
    }
}
