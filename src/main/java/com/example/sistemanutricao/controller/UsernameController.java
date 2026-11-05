package com.example.sistemanutricao.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.usuario.UsuarioService;

@ControllerAdvice
public class UsernameController {

    private final UsuarioService usuarioService;
    private final com.example.sistemanutricao.mapper.UsuarioMapper usuarioMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public UsernameController(UsuarioService usuarioService, com.example.sistemanutricao.mapper.UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    public UsernameController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = null;
    }

    @ModelAttribute("Username")
    public String getUsername(@AuthenticationPrincipal UsuarioSecurity usuarioPrincipal) {
        return usuarioPrincipal != null ? usuarioPrincipal.getUsername() : "";
    }

    @ModelAttribute("usuarioLogado")
    public GetUsuarioDTO getUsuarioLogado(@AuthenticationPrincipal UsuarioSecurity usuarioPrincipal) {
        if (usuarioPrincipal != null) {
            GetUsuarioDTO usuario = (usuarioMapper != null && usuarioPrincipal.getUsuario() != null)
                    ? usuarioMapper.toGetDTO(usuarioPrincipal.getUsuario())
                    : usuarioService.findById(usuarioPrincipal.getId());
            return usuario != null ? usuario : getEmptyUsuario();
        }
        return getEmptyUsuario();
    }

    private GetUsuarioDTO getEmptyUsuario() {
        return new GetUsuarioDTO(null, "", "", null, null, "", false, "", "");
    }
}