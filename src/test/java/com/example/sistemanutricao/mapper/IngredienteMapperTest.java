package com.example.sistemanutricao.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import org.mapstruct.factory.Mappers;

class IngredienteMapperTest {

    private final IngredienteMapper ingredienteMapper = Mappers.getMapper(IngredienteMapper.class);

    @Test
    void shouldMapIngredienteToGetDto() {
        Ingrediente ingrediente = criarIngrediente();

        var dto = ingredienteMapper.toGetDto(ingrediente);

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.nome()).isEqualTo("Aveia");
        assertThat(dto.usuarioId()).isEqualTo(3L);
        assertThat(dto.gorduraSaturada()).isEqualByComparingTo("0.40");
    }

    @Test
    void shouldMapIngredienteToTagDto() {
        Ingrediente ingrediente = criarIngrediente();

        var dto = ingredienteMapper.toTagDto(ingrediente, "Alta");

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.nome()).isEqualTo("Aveia");
        assertThat(dto.usuarioId()).isEqualTo(3L);
        assertThat(dto.tag()).isEqualTo("Alta");
    }

    private Ingrediente criarIngrediente() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(12L);
        ingrediente.setNome("Aveia");
        ingrediente.setPtn(new BigDecimal("14.00"));
        ingrediente.setCho(new BigDecimal("60.00"));
        ingrediente.setLip(new BigDecimal("8.00"));
        ingrediente.setSodio(new BigDecimal("5.00"));
        ingrediente.setGorduraSaturada(new BigDecimal("0.40"));
        ingrediente.setStatus(Status.ATIVA);
        ingrediente.setUsuario(usuario);
        return ingrediente;
    }
}
