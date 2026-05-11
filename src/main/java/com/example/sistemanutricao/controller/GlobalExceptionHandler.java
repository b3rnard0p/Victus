package com.example.sistemanutricao.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.sistemanutricao.exception.FormValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        IllegalArgumentException.class, 
        FormValidationException.class, 
        EntityNotFoundException.class,
        org.springframework.dao.DataIntegrityViolationException.class
    })
    public ModelAndView handleException(Exception ex, HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response,
                                                       RedirectAttributes redirectAttributes) {
        String message = ex.getMessage();
        
        if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            message = "Não é possível realizar esta operação pois o registro possui vínculos ou restrições de integridade.";
        }
        String isHtmxRequest = request.getHeader("HX-Request");

        if (isHtmxRequest != null && isHtmxRequest.equals("true")) {
            ModelAndView mav = new ModelAndView("components/Toast :: error");
            mav.addObject("mensagem", message);
            mav.addObject("isHtmx", true);

            response.setHeader("HX-Retarget", "#toast-container");
            response.setHeader("HX-Reswap", "afterbegin");
            mav.setStatus(org.springframework.http.HttpStatus.OK);
            return mav;
        }

        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null) ? referer : "/";
        redirectAttributes.addFlashAttribute("errorMessage", message);

        return new ModelAndView("redirect:" + redirectUrl);
    }
}
