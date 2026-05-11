package com.example.sistemanutricao.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.record.UsuarioDTO.UsuarioDTO;
import com.example.sistemanutricao.security.AuthSessionService;
import com.example.sistemanutricao.security.LoginStatus;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.usuario.PasswordValidationService;
import com.example.sistemanutricao.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final AuthSessionService authSessionService;
    private final PasswordValidationService passwordValidationService;

    public UsuarioController(UsuarioService usuarioService,
                             AuthSessionService authSessionService,
                             PasswordValidationService passwordValidationService) {
        this.usuarioService = usuarioService;
        this.authSessionService = authSessionService;
        this.passwordValidationService = passwordValidationService;
    }

    // PÁGINAS GERAIS

    @GetMapping("/home")
    public String get() {
        return "pages/general/Home";
    }

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "pages/general/Login";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "pages/general/acesso-negado";
    }

    @GetMapping("/registro-sucesso")
    public String mostrarSucessoRegistro() {
        return "pages/general/Registrado";
    }

    // AUTENTICAÇÃO

    @PostMapping("/login")
    public String realizarLogin(@RequestParam String email,
                                @RequestParam String password,
                                jakarta.servlet.http.HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        LoginStatus loginStatus = authSessionService.login(email, password, response);
        if (loginStatus == LoginStatus.SUCCESS) return "redirect:/home";
        if (loginStatus == LoginStatus.ACCESS_DENIED) return "redirect:/acesso-negado";
        redirectAttributes.addAttribute("error", "true");
        return "redirect:/";
    }

    @PostMapping("/sair-do-sistema")
    public String realizarLogout(jakarta.servlet.http.HttpServletResponse response) {
        authSessionService.logout(response);
        return "redirect:/";
    }

    @GetMapping("/auth/status")
    public ResponseEntity<Map<String, Object>> verificarStatusAutenticacao(
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal) {
        Map<String, Object> response = new HashMap<>();
        if (usuarioPrincipal != null) {
            response.put("autenticado", true);
            response.put("cargo", usuarioPrincipal.getCargo().name());
            response.put("username", usuarioPrincipal.getUsername());
        } else {
            response.put("autenticado", false);
        }
        return ResponseEntity.ok(response);
    }

    // REGISTRO

    @PostMapping("/usuario/registrar")
    public String registrarUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO registroDto,
                                   BindingResult result) {
        if (result.hasErrors()) {
            throw new IllegalArgumentException(result.getFieldError().getDefaultMessage());
        }
        usuarioService.create(registroDto);
        return "redirect:/registro-sucesso";
    }

    // PERFIL

    @GetMapping("/usuario/perfil")
    public String redirectPerfil() {
        return "redirect:/home";
    }

    @GetMapping("/usuario/perfil/modal")
    public String mostrarPerfilModal(@AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
                                     Model model) {
        GetUsuarioDTO usuarioDTO = usuarioService.findById(usuarioPrincipal.getId());
        model.addAttribute("usuario", usuarioDTO);
        return "fragments/Perfil";
    }

    @PostMapping("/usuario/editar")
    public Object editarPerfil(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO,
                               BindingResult result,
                               @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
                               @RequestParam(value = "imagemPerfil", required = false) MultipartFile arquivoImagem,
                               RedirectAttributes redirectAttributes,
                               jakarta.servlet.http.HttpServletRequest request) {

        passwordValidationService.validar(
                usuarioDTO.senhaAtual(),
                usuarioDTO.novaSenha(),
                usuarioDTO.confirmarNovaSenha(),
                usuarioPrincipal.getPassword()
        ).ifPresent(erro -> result.rejectValue("senhaAtual", "error.usuario", erro));

        String htmxRequest = request.getHeader("HX-Request");

        if (result.hasErrors()) {
            if ("true".equals(htmxRequest)) {
                return ResponseEntity.badRequest()
                        .body(result.getFieldError().getDefaultMessage());
            }
            return "fragments/Perfil";
        }

        try {
            usuarioService.atualizarPerfilComImagem(usuarioPrincipal.getId(), usuarioDTO, arquivoImagem);
            
            if ("true".equals(htmxRequest)) {
                return ResponseEntity.ok().body("Perfil atualizado com sucesso!");
            }
            
            redirectAttributes.addFlashAttribute("success", "Perfil atualizado com sucesso!");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null && !referer.contains("/editar") ? referer : "/home");
        } catch (Exception e) {
            logger.error("Erro ao atualizar perfil: {}", e.getMessage());
            if ("true".equals(htmxRequest)) {
                return ResponseEntity.internalServerError().body("Erro ao atualizar perfil: " + e.getMessage());
            }
            result.rejectValue("", "error.usuario", "Erro ao atualizar perfil: " + e.getMessage());
            return "fragments/Perfil";
        }
    }

    // IMAGEM

    @GetMapping("/usuario/{id}/imagem")
    public ResponseEntity<Resource> obterImagemUsuario(@PathVariable Long id) {
        try {
            Resource resource = usuarioService.obterImagemPerfil(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Erro ao obter imagem do usuário {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
