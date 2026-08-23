package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.model.*;
import com.example.sistemanutricao.record.UsuarioDTO.*;
import com.example.sistemanutricao.record.IngredienteDTO.*;
import com.example.sistemanutricao.record.RefeicaoDTO.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class MapperCoverageTest {

    private final UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);
    private final IngredienteMapper ingredienteMapper = Mappers.getMapper(IngredienteMapper.class);
    private final RefeicaoMapper refeicaoMapper = Mappers.getMapper(RefeicaoMapper.class);

    @Test
    void testUsuarioMapperNulls() {
        assertNull(usuarioMapper.toGetDTO(null));
    }

    @Test
    void testUsuarioMapperValores() {
        Usuario u = new Usuario();
        u.setUsername("Teste");
        
        GetUsuarioDTO dto = usuarioMapper.toGetDTO(u);
        assertNotNull(dto);
        assertEquals("Teste", dto.username());
    }

    @Test
    void testIngredienteMapperNulls() {
        assertNull(ingredienteMapper.toEntity((IngredienteDTO)null));
        assertNull(ingredienteMapper.toGetDto(null));
    }

    @Test
    void testIngredienteMapperValores() {
        Ingrediente i = new Ingrediente();
        i.setNome("Alho");
        IngredienteGetDTO dto = ingredienteMapper.toGetDto(i);
        assertNotNull(dto);
        assertEquals("Alho", dto.nome());
    }

    @Test
    void testRefeicaoMapperNulls() {
        assertNull(refeicaoMapper.toResponseDTO(null));
    }

    @Test
    void testRefeicaoMapperValores() {
        Refeicao r = new Refeicao();
        r.setNome("Janta");
        RefeicaoResponseDTO dto = refeicaoMapper.toResponseDTO(r);
        assertNotNull(dto);
        assertEquals("Janta", dto.nome());
    }
}
