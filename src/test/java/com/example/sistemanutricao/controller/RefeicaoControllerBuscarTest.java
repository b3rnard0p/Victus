package com.example.sistemanutricao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.RefeicaoDTO.FichaTecnicaRefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.refeicao.RefeicaoQueryService;
import com.example.sistemanutricao.service.refeicao.RefeicaoService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefeicaoController - endpoint consolidado")
@SuppressWarnings("null")
class RefeicaoControllerBuscarTest {

    @Mock
    private RefeicaoService refeicaoService;

    @Mock
    private RefeicaoQueryService refeicaoQueryService;

    @Mock
    private FichaTecnicaService fichaTecnicaService;

    @Mock
    private PaginacaoViewSupport paginacaoViewSupport;

    @InjectMocks
    private RefeicaoController controller;

    @Test
    void deveBuscarPadraoQuandoNaoHaFiltros() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of(refeicaoDto()));

        when(refeicaoQueryService.buscarPorStatus(eq(Status.ATIVA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/refeicoes/List :: itens");

        String view = controller.buscar(null, null, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(refeicaoQueryService).buscarPorStatus(eq(Status.ATIVA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/refeicoes/List :: itens");
    }

    @Test
    void deveBuscarPorNome() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of(refeicaoDto()));

        when(refeicaoQueryService.buscarPorNome(eq("Almoço"), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/refeicoes/List :: itens");

        String view = controller.buscar("Almoço", null, 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(refeicaoQueryService).buscarPorNome(eq("Almoço"), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/refeicoes/List :: itens");
    }

    @Test
    void deveBuscarPorStatus() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of(refeicaoDto()));

        when(refeicaoQueryService.buscarPorStatus(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/refeicoes/List :: itens");

        String view = controller.buscar(null, "INATIVA", 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(refeicaoQueryService).buscarPorStatus(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/refeicoes/List :: itens");
    }

    @Test
    void deveIgnorarStatusVazioEDefinirPadrao() {
        Model model = new ExtendedModelMap();
        UsuarioSecurity usuario = usuarioNutricionista();
        Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of(refeicaoDto()));

        when(refeicaoQueryService.buscarPorStatus(eq(Status.ATIVA), eq(usuario.getUsuario()), any(Pageable.class)))
                .thenReturn(page);
        when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                .thenReturn("pages/refeicoes/List :: itens");

        String view = controller.buscar(null, "", 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

        verify(refeicaoQueryService).buscarPorStatus(eq(Status.ATIVA), eq(usuario.getUsuario()), any(Pageable.class));
        org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/refeicoes/List :: itens");
    }

        @Test
        void deveAceitarStatusEmMinusculo() {
                Model model = new ExtendedModelMap();
                UsuarioSecurity usuario = usuarioNutricionista();
                Page<RefeicaoResponseDTO> page = new PageImpl<>(List.of(refeicaoDto()));

                when(refeicaoQueryService.buscarPorStatus(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class)))
                                .thenReturn(page);
                when(paginacaoViewSupport.renderizarView(anyString(), any(), any(), anyBoolean()))
                                .thenReturn("pages/refeicoes/List :: itens");

                String view = controller.buscar(null, "inativa", 0, model, usuario, new org.springframework.mock.web.MockHttpServletRequest(), null);

                verify(refeicaoQueryService).buscarPorStatus(eq(Status.INATIVA), eq(usuario.getUsuario()), any(Pageable.class));
                org.assertj.core.api.Assertions.assertThat(view).isEqualTo("pages/refeicoes/List :: itens");
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

    private RefeicaoResponseDTO refeicaoDto() {
        return new RefeicaoResponseDTO(1L, "Refeição Teste", "1200", Status.ATIVA, List.of(new FichaTecnicaRefeicaoDTO(1L, "Ficha", java.math.BigDecimal.TEN)));
    }
}
