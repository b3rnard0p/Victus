package com.example.sistemanutricao.controller;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.record.EstabelecimentoDTO.EstabelecimentoDTO;
import com.example.sistemanutricao.record.EstabelecimentoDTO.GetEstabelecimentoDTO;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.EstabelecimentoService;
import com.example.sistemanutricao.service.log.AtividadeLogService;
import com.example.sistemanutricao.model.AtividadeLog;
import com.example.sistemanutricao.service.usuario.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final int TAMANHO_PAGINA = 6;

    private final UsuarioService usuarioService;
    private final EstabelecimentoService estabelecimentoService;
    private final PaginacaoViewSupport paginacaoViewSupport;
    private final AtividadeLogService atividadeLogService;

    public AdminController(UsuarioService usuarioService,
            EstabelecimentoService estabelecimentoService,
            PaginacaoViewSupport paginacaoViewSupport,
            AtividadeLogService atividadeLogService) {
        this.usuarioService = usuarioService;
        this.estabelecimentoService = estabelecimentoService;
        this.paginacaoViewSupport = paginacaoViewSupport;
        this.atividadeLogService = atividadeLogService;
    }

    // USUÁRIOS

    @GetMapping("/usuarios")
    public String listUsuarios(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @AuthenticationPrincipal UsuarioSecurity adminLogado,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        Long adminId = adminLogado != null ? adminLogado.getId() : -1L;
        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<GetUsuarioDTO> usuarios = usuarioService.listPage(adminId, pageable);
        model.addAttribute("usuarios", usuarios.getContent());
        model.addAttribute("cargos", Cargo.values());
        model.addAttribute("adminId", adminId);
        paginacaoViewSupport.configurarPaginacao(model, request, page, usuarios);

        return paginacaoViewSupport.renderizarView("pages/admin/usuarios/List", htmxRequest, model, page > 0);
    }

    @PostMapping("/usuarios/{id}/toggle-ativo")
    public String toggleAtivo(@PathVariable Long id, Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            @AuthenticationPrincipal UsuarioSecurity adminLogado) {
        usuarioService.toggleAtivo(id);
        if (htmxRequest != null) {
            GetUsuarioDTO usuario = usuarioService.findById(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("adminId", adminLogado != null ? adminLogado.getId() : -1L);
            return "pages/admin/usuarios/List :: usuario-item";
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/{id}/estabelecimento")
    public String formVinculoEstabelecimento(
            @PathVariable Long id,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        GetUsuarioDTO usuario = usuarioService.findById(id);
        List<GetEstabelecimentoDTO> estabelecimentos = estabelecimentoService.listAll();

        if (usuario == null) {
            return "redirect:/admin/usuarios";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("estabelecimentos", estabelecimentos);

        List<Map<String, String>> estabelecimentoOptions = new ArrayList<>();
        estabelecimentoOptions.add(Map.of("value", "", "label", "Sem vínculo"));
        for (GetEstabelecimentoDTO estabelecimento : estabelecimentos) {
            estabelecimentoOptions.add(Map.of(
                    "value", String.valueOf(estabelecimento.id()),
                    "label", estabelecimento.nome()));
        }
        model.addAttribute("estabelecimentoOptions", estabelecimentoOptions);

        return paginacaoViewSupport.renderizarView("pages/admin/usuarios/Form", htmxRequest, model, false);
    }

    @PostMapping("/usuarios/{id}/estabelecimento")
    public String atualizarVinculoEstabelecimento(
            @PathVariable Long id,
            @RequestParam(required = false) Long estabelecimentoId,
            Model model, @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            @AuthenticationPrincipal UsuarioSecurity adminLogado) {

        usuarioService.atualizarEstabelecimento(id, estabelecimentoId);
        if (htmxRequest != null) {
            GetUsuarioDTO usuario = usuarioService.findById(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("adminId", adminLogado != null ? adminLogado.getId() : -1L);
            return "pages/admin/usuarios/List :: usuario-item";
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/cargo")
    public String updateCargo(
            @PathVariable Long id,
            @RequestParam(required = false) Cargo cargo,
            @AuthenticationPrincipal UsuarioSecurity adminLogado,
            Model model, @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        if (cargo == null) {
            throw new IllegalArgumentException("Não é possivel retirar o cargo de um usuário");
        }
        
        if (adminLogado != null && adminLogado.getId().equals(id)) {
            throw new IllegalArgumentException("Você não pode alterar o próprio cargo.");
        }
        usuarioService.updateCargo(id, cargo);
        if (htmxRequest != null) {
            GetUsuarioDTO usuario = usuarioService.findById(id);
            model.addAttribute("usuario", usuario);
            model.addAttribute("adminId", adminLogado != null ? adminLogado.getId() : -1L);
            return "pages/admin/usuarios/List :: usuario-item";
        }
        return "redirect:/admin/usuarios";
    }

    // ESTABELECIMENTOS

    @GetMapping("/estabelecimentos")
    public String listEstabelecimentos(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<GetEstabelecimentoDTO> estabelecimentos = estabelecimentoService.listPage(pageable);
        model.addAttribute("estabelecimentos", estabelecimentos.getContent());
        paginacaoViewSupport.configurarPaginacao(model, request, page, estabelecimentos);

        return paginacaoViewSupport.renderizarView("pages/admin/estabelecimentos/List", htmxRequest, model, page > 0);
    }

    @GetMapping("/estabelecimentos/novo")
    public String createForm(
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        model.addAttribute("estabelecimento", new EstabelecimentoDTO(null, ""));
        return paginacaoViewSupport.renderizarView("pages/admin/estabelecimentos/Form", htmxRequest, model, false);
    }

    @PostMapping("/estabelecimentos")
    public String create(@Valid @ModelAttribute EstabelecimentoDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            throw new IllegalArgumentException(result.getAllErrors().get(0).getDefaultMessage());
        }
        estabelecimentoService.create(dto);
        return "redirect:/admin/estabelecimentos";
    }

    @GetMapping("/estabelecimentos/{id}/editar")
    public String updateForm(
            @PathVariable Long id,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        GetEstabelecimentoDTO estabelecimento = estabelecimentoService.findById(id);
        model.addAttribute("estabelecimento", estabelecimento);

        return paginacaoViewSupport.renderizarView("pages/admin/estabelecimentos/Form", htmxRequest, model, false);
    }

    @PostMapping("/estabelecimentos/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute EstabelecimentoDTO dto, BindingResult result, Model model, @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        if (result.hasErrors()) {
            throw new IllegalArgumentException(result.getAllErrors().get(0).getDefaultMessage());
        }
        estabelecimentoService.update(id, dto);
        if (htmxRequest != null) {
            GetEstabelecimentoDTO estabelecimento = estabelecimentoService.findById(id);
            model.addAttribute("estabelecimento", estabelecimento);
            return "pages/admin/estabelecimentos/List :: estabelecimento-item";
        }
        return "redirect:/admin/estabelecimentos";
    }

    @GetMapping("/usuarios/{id}/atividades")
    public String listarAtividades(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Model model) {
        Page<AtividadeLog> atividades = atividadeLogService.buscarAtividadesPorUsuario(id, PageRequest.of(page, size));
        GetUsuarioDTO usuario = usuarioService.findById(id);
        
        model.addAttribute("atividades", atividades.getContent());
        model.addAttribute("usuario", usuario);
        
        paginacaoViewSupport.configurarPaginacao(model, request, page, atividades);
        
        return paginacaoViewSupport.renderizarView("pages/admin/atividades/List", htmxRequest, model, page > 0);
    }
}