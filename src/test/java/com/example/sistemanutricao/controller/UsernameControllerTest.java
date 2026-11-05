package com.example.sistemanutricao.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example.sistemanutricao.mapper.UsuarioMapper;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.usuario.UsuarioService;

class UsernameControllerTest {

    @Test
    void deveMapearUsuarioLogadoDiretamenteDoPrincipalSemIrAoBanco() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);
        UsernameController controller = new UsernameController(usuarioService, mapper);

        Usuario u = new Usuario();
        u.setId(5L);
        u.setUsername("nutri");
        u.setEmail("nutri@victus.com");
        u.setCargo(Cargo.NUTRICIONISTA);
        u.setAtivo(true);

        GetUsuarioDTO dto = controller.getUsuarioLogado(new UsuarioSecurity(u));

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.username()).isEqualTo("nutri");
        verifyNoInteractions(usuarioService);
    }
}

