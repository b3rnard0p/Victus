package com.example.sistemanutricao.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Status;
import static com.example.sistemanutricao.model.enuns.Status.ATIVA;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ingrediente.IngredienteQueryService;
import com.example.sistemanutricao.service.ingrediente.IngredienteService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({"/ingrediente"})
@CrossOrigin("*")
@PreAuthorize("hasRole('NUTRICIONISTA')")
public class IngredienteController {

    private static final int TAMANHO_PAGINA = 6;

    private final IngredienteService ingredienteService;
    private final IngredienteQueryService ingredienteQueryService;
    private final PaginacaoViewSupport paginacaoViewSupport;

    public IngredienteController(IngredienteService ingredienteService,
                                 IngredienteQueryService ingredienteQueryService,
                                 PaginacaoViewSupport paginacaoViewSupport) {
        this.ingredienteService = ingredienteService;
        this.ingredienteQueryService = ingredienteQueryService;
        this.paginacaoViewSupport = paginacaoViewSupport;
    }

    @ModelAttribute("filtroIngredienteOptions")
    public List<Map<String, String>> getFiltroIngredienteOptions() {
        return List.of(
                Map.of("value", "nome",     "label", "Nome"),
                Map.of("value", "PTN",      "label", "PTN"),
                Map.of("value", "CHO",      "label", "CHO"),
                Map.of("value", "LIP",      "label", "LIP"),
                Map.of("value", "sodio",    "label", "Sódio"),
                Map.of("value", "gorduras", "label", "Gord. Saturada")
        );
    }

    @ModelAttribute("tagNivelOptions")
    public List<Map<String, String>> getTagNivelOptions() {
        return List.of(
                Map.of("value", "alta", "label", "Alta"),
                Map.of("value", "media", "label", "Média"),
                Map.of("value", "baixa", "label", "Baixa")
        );
    }

    @GetMapping({"/por-nome", "/por-ptn", "/por-cho", "/por-lip", "/por-sodio", "/por-gordura-saturada", "/por-tag"})
    public String redirectOldSearch(HttpServletRequest request) {
        String query = request.getQueryString();
        return "redirect:/ingrediente" + (query != null ? "?" + query : "");
    }

    @GetMapping({"/taco/por-nome", "/taco/por-ptn", "/taco/por-cho", "/taco/por-lip", "/taco/por-sodio", "/taco/por-gordura-saturada", "/taco/por-tag"})
    public String redirectOldTacoSearch(HttpServletRequest request) {
        String query = request.getQueryString();
        return "redirect:/ingrediente/taco" + (query != null ? "?" + query : "");
    }

    @GetMapping({"/buscar", "/por-status", "/taco", ""})
    public String buscar(
            @RequestParam(value = "campo", required = false) String campo,
            @RequestParam(value = "valorPesquisa", required = false) String valor,
            @RequestParam(value = "tipoPesquisa", required = false) String tipo,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "view", required = false) String view,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        boolean isTaco = "taco".equals(view);
        Long usuarioId = isTaco ? ingredienteService.buscarUsuarioTacoId() : usuarioPrincipal.getId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<?> ingredientesPage;
        
        if (campo != null && valor != null && tipo != null) {
            if (isTaco) {
                ingredientesPage = ingredienteQueryService.pesquisarTaco(campo, valor, tipo, pageable);
            } else {
                ingredientesPage = ingredienteQueryService.pesquisar(campo, valor, tipo, usuarioId, pageable);
            }
            if ("tags".equals(tipo)) {
                model.addAttribute("pesquisaPorTag", true);
                model.addAttribute("isComTagDTO", true);
            }
            model.addAttribute("statusAtual", status != null ? status : ATIVA);
        } else if (status != null) {
            ingredientesPage = ingredienteQueryService.buscarPorStatusEUsuario(status, usuarioId, pageable);
            model.addAttribute("statusAtual", status);
        } else {
            ingredientesPage = ingredienteQueryService.buscarPorStatusEUsuario(ATIVA, usuarioId, pageable);
            model.addAttribute("statusAtual", ATIVA);
        }
        
        model.addAttribute("ingredientes", ingredientesPage.getContent());
        model.addAttribute("viewMode", view != null ? view : "meus");
        paginacaoViewSupport.configurarPaginacao(model, request, page, ingredientesPage);
        return paginacaoViewSupport.renderizarView("pages/ingredientes/List", htmxRequest, model, page > 0);
    }

    @GetMapping("/pesquisar")
    public String pesquisar(
            @RequestParam("campo") String campo,
            @RequestParam("valorPesquisa") String valor,
            @RequestParam("tipoPesquisa") String tipo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        return buscar(campo, valor, tipo, null, "meus", page, model, usuarioPrincipal, request, htmxRequest);
    }

    @GetMapping("/taco/pesquisar")
    public String pesquisarTaco(
            @RequestParam("campo") String campo,
            @RequestParam("valorPesquisa") String valor,
            @RequestParam("tipoPesquisa") String tipo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        return buscar(campo, valor, tipo, null, "taco", page, model, usuarioPrincipal, request, htmxRequest);
    }


    @PostMapping("/atualiza-status/{id}")
    public String atualizaStatus(@PathVariable Long id) {
        ingredienteService.atualizaStatus(id);
        return "redirect:/ingrediente";
    }

    @PostMapping("/novo")
    public Object criarIngrediente(@Valid @ModelAttribute("ingrediente") IngredienteDTO dto,
                                   BindingResult result,
                                   @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        if (result.hasErrors()) {
            var fieldError = result.getFieldError();
            throw new IllegalArgumentException(fieldError != null ? fieldError.getDefaultMessage() : "Erro de validação");
        }

        Long usuarioId = usuarioPrincipal.getId();
        IngredienteGetDTO criado = ingredienteService.create(dto, usuarioId);

        String htmxRequest = request.getHeader("HX-Request");
        if ("true".equals(htmxRequest)) {
            return ResponseEntity.ok(criado);
        }

        return "redirect:/ingrediente";
    }

    @GetMapping("/editar/{id}")
    @ResponseBody
    public IngredienteGetDTO mostrarFormularioEdicaoIngredientes(@PathVariable Long id) {
        return ingredienteService.getIngredienteById(id);
    }

    @PostMapping("/editar/{id}")
    public Object atualizarIngrediente(@PathVariable Long id,
                                       @Valid @ModelAttribute("ingrediente") IngredienteDTO dto,
                                       BindingResult result,
                                       HttpServletRequest request) {
        if (result.hasErrors()) {
            var fieldError = result.getFieldError();
            throw new IllegalArgumentException(fieldError != null ? fieldError.getDefaultMessage() : "Erro de validação");
        }

        ingredienteService.update(id, dto);
        IngredienteGetDTO atualizado = ingredienteService.getIngredienteById(id);

        String htmxRequest = request.getHeader("HX-Request");
        if ("true".equals(htmxRequest)) {
            return ResponseEntity.ok(atualizado);
        }

        return "redirect:/ingrediente";
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public List<IngredienteGetDTO> buscarIngredientesApi(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal) {
        
        List<IngredienteGetDTO> resultados = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 20);

        if (usuarioPrincipal != null) {
            Long usuarioId = usuarioPrincipal.getId();
            @SuppressWarnings("unchecked")
            Page<IngredienteGetDTO> ingredientesUsuario = (Page<IngredienteGetDTO>) ingredienteQueryService.pesquisar("nome", query, "especifico", usuarioId, pageable);
            resultados.addAll(ingredientesUsuario.getContent());
        }

        @SuppressWarnings("unchecked")
        Page<IngredienteGetDTO> ingredientesTaco = (Page<IngredienteGetDTO>) ingredienteQueryService.pesquisarTaco("nome", query, "especifico", pageable);

        resultados.addAll(ingredientesTaco.getContent());

        return resultados;
    }
}
