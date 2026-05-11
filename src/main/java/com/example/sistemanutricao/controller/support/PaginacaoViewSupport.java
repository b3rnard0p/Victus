package com.example.sistemanutricao.controller.support;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PaginacaoViewSupport {

    public void configurarPaginacao(Model model, HttpServletRequest request, int page, Page<?> resultados) {
        model.addAttribute("paginaAtual", page);
        model.addAttribute("temMaisResultados", resultados != null && resultados.hasNext());
        model.addAttribute(
                "proximaPaginaUrl",
                resultados != null && resultados.hasNext() ? montarProximaPaginaUrl(request, page + 1) : null
        );
    }

    public String renderizarView(String viewPath, String htmxRequest, Model model) {
        return renderizarView(viewPath, htmxRequest, model, false);
    }

    public String renderizarView(String viewPath, String htmxRequest, Model model, boolean fragmentoLista) {
        if (fragmentoLista) {
            return viewPath + " :: itens";
        }
        if (htmxRequest != null) {
            return viewPath + " :: conteudo";
        }
        model.addAttribute("view", viewPath);
        return "Layout";
    }

    private String montarProximaPaginaUrl(HttpServletRequest request, int proximaPagina) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(request.getRequestURL().toString());
        request.getParameterMap().forEach((nome, valores) -> {
            if (!"page".equals(nome) && valores != null) {
                for (String valor : valores) {
                    builder.queryParam(nome, valor);
                }
            }
        });
        builder.replaceQueryParam("page", proximaPagina);
        return builder.build().toUriString();
    }
}
