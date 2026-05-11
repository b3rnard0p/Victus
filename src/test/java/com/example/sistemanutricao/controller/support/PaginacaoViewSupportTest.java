package com.example.sistemanutricao.controller.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

class PaginacaoViewSupportTest {

    private final PaginacaoViewSupport paginacaoViewSupport = new PaginacaoViewSupport();

    @Test
    void shouldConfigurePaginationKeepingExistingQueryParams() {
        ExtendedModelMap model = new ExtendedModelMap();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ingrediente/por-nome");
        request.setRequestURI("/ingrediente/por-nome");
        request.setParameter("nome", "arroz");

        var pagina = new PageImpl<>(List.of("a"), PageRequest.of(0, 1), 2);

        paginacaoViewSupport.configurarPaginacao(model, request, 0, pagina);

        assertThat(model.getAttribute("paginaAtual")).isEqualTo(0);
        assertThat(model.getAttribute("temMaisResultados")).isEqualTo(true);
        assertThat(model.getAttribute("proximaPaginaUrl"))
                .isEqualTo("http://localhost/ingrediente/por-nome?nome=arroz&page=1");
    }

    @Test
    void shouldRenderItemsFragmentWhenListIsBeingAppended() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = paginacaoViewSupport.renderizarView("pages/ingredientes/List", "true", model, true);

        assertThat(view).isEqualTo("pages/ingredientes/List :: itens");
    }

    @Test
    void shouldRenderLayoutWhenRequestIsNotHtmx() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = paginacaoViewSupport.renderizarView("pages/ingredientes/List", null, model, false);

        assertThat(view).isEqualTo("Layout");
        assertThat(model.getAttribute("view")).isEqualTo("pages/ingredientes/List");
    }
}
