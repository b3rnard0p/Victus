package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.port.PdfExporter;

@ExtendWith(MockitoExtension.class)
@DisplayName("FichaController - endpoint consolidado")
class FichaControllerBuscarTest {

    @Mock
    private FichaTecnicaService fichaTecnicaService;

    @Mock
    private FichaQueryService fichaQueryService;

    @Mock
    private PdfExporter pdfExporter;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    @InjectMocks
    private FichaController controller;

    @Test
    void deveBuscarPadraoQuandoNaoHaFiltros() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of(fichaDto()));

        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn((Page) page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List :: itens");

        String view = controller.buscar(null, null, null, null, null, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(fichaQueryService).buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.COMPLETA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/fichas/List :: itens");
        org.assertj.core.api.Assertions.assertThat(model.getAttribute("fichas")).isNotNull();
    }

    @Test
    void deveBuscarPorNomeQuandoInformado() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of(fichaDto()));

        when(fichaQueryService.pesquisar(eq("nome"), eq("teste"), eq("por-nome"), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn((Page) page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List :: itens");

        String view = controller.buscar("nome", "teste", "por-nome", null, null, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(fichaQueryService).pesquisar(eq("nome"), eq("teste"), eq("por-nome"), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/fichas/List :: itens");
    }

    @Test
    void deveBuscarPorStatusSimples() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of(fichaDto()));

        when(fichaQueryService.buscarPorStatusSimples(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn((Page) page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List :: itens");

        String view = controller.buscar(null, null, null, Status.INATIVA, null, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(fichaQueryService).buscarPorStatusSimples(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/fichas/List :: itens");
    }

    @Test
    void deveBuscarPorStatusCriacao() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<FichaTecnicaGetDTO> page = new PageImpl<>(List.of(fichaDto()));

        when(fichaQueryService.buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.INCOMPLETA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn((Page) page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/fichas/List :: itens");

        String view = controller.buscar(null, null, null, Status.ATIVA, StatusCriacao.INCOMPLETA, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(fichaQueryService).buscarPorStatus(eq(Status.ATIVA), eq(StatusCriacao.INCOMPLETA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/fichas/List :: itens");
    }

    private UsuarioSecurity usuarioNutricionista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("nutri");
        usuario.setEmail("nutri@example.com");
        usuario.setSenha("senha");
        usuario.setCargo(Cargo.NUTRICIONISTA);
        usuario.setAtivo(true);
        return new UsuarioSecurity(usuario);
    }

    private FichaTecnicaGetDTO fichaDto() {
        return new FichaTecnicaGetDTO(
                1L,
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(1500),
                "Porção",
                2,
                BigDecimal.valueOf(100),
                Status.ATIVA,
                StatusCriacao.COMPLETA,
                null,
                List.of(),
                null
        );
    }
}
