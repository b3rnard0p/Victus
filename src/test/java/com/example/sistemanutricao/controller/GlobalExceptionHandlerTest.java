package com.example.sistemanutricao.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.persistence.EntityNotFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornarToastParaRequisicaoHtmx() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("HX-Request", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        ModelAndView mav = handler.handleException(new IllegalArgumentException("Erro de validação"), request, response, redirectAttributes);

        assertThat(mav.getViewName()).isEqualTo("components/Toast :: error");
        assertThat(mav.getModel().get("mensagem")).isEqualTo("Erro de validação");
        assertThat(response.getHeader("HX-Retarget")).isEqualTo("#toast-container");
        assertThat(response.getHeader("HX-Reswap")).isEqualTo("afterbegin");
    }

    @Test
    void deveRedirecionarParaRefererQuandoNaoEhHtmx() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "/refeicao");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        ModelAndView mav = handler.handleException(new EntityNotFoundException("Refeição não encontrada"), request, response, redirectAttributes);

        assertThat(mav.getViewName()).isEqualTo("redirect:/refeicao");
        assertThat(redirectAttributes.getFlashAttributes().get("errorMessage")).isEqualTo("Refeição não encontrada");
    }
}