package com.example.sistemanutricao.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;

import org.mapstruct.factory.Mappers;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = Mappers.getMapper(UsuarioMapper.class);

    @Test
    void shouldMapUsuarioWithEstabelecimento() {
        Estabelecimento est = new Estabelecimento();
        est.setId(2L);
        est.setNome("Restaurante X");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("ana");
        usuario.setEmail("ana@exemplo.com");
        usuario.setCargo(Cargo.NUTRICIONISTA);
        usuario.setAtivo(true);
        usuario.setEstabelecimento(est);
        usuario.setCaminhoImagem("imagens/ana.jpg");

        GetUsuarioDTO dto = mapper.toGetDTO(usuario);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.username()).isEqualTo("ana");
        assertThat(dto.email()).isEqualTo("ana@exemplo.com");
        assertThat(dto.cargo()).isEqualTo(Cargo.NUTRICIONISTA);
        assertThat(dto.ativo()).isTrue();
        assertThat(dto.estabelecimentoId()).isEqualTo(2L);
        assertThat(dto.estabelecimentoNome()).isEqualTo("Restaurante X");
        assertThat(dto.caminhoImagem()).isEqualTo("imagens/ana.jpg");
    }

    @Test
    void shouldMapUsuarioWithoutEstabelecimento() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setUsername("joao");
        usuario.setEmail("joao@exemplo.com");
        usuario.setCargo(Cargo.PRODUCAO);
        usuario.setAtivo(false);

        GetUsuarioDTO dto = mapper.toGetDTO(usuario);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.estabelecimentoId()).isNull();
        assertThat(dto.estabelecimentoNome()).isNull();
        assertThat(dto.caminhoImagem()).isNull();
    }

    @Test
    void shouldMapAtivoCorrently() {
        Usuario ativo = new Usuario();
        ativo.setId(3L);
        ativo.setUsername("maria");
        ativo.setEmail("m@m.com");
        ativo.setAtivo(true);

        Usuario inativo = new Usuario();
        inativo.setId(4L);
        inativo.setUsername("jose");
        inativo.setEmail("j@j.com");
        inativo.setAtivo(false);

        assertThat(mapper.toGetDTO(ativo).ativo()).isTrue();
        assertThat(mapper.toGetDTO(inativo).ativo()).isFalse();
    }
}
