package com.example.sistemanutricao.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.model.enuns.Status;
import static com.example.sistemanutricao.model.enuns.Status.ATIVA;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoNutrientesResponseDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.refeicao.RefeicaoQueryService;
import com.example.sistemanutricao.service.refeicao.RefeicaoService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.lang.NonNull;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/refeicao"})
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('NUTRICIONISTA', 'PRODUCAO')")
public class RefeicaoController {

    private static final int TAMANHO_PAGINA = 6;

    private final RefeicaoService refeicaoService;
    private final RefeicaoQueryService refeicaoQueryService;
    private final FichaTecnicaService fichaTecnicaService;
    private final PaginacaoViewSupport paginacaoViewSupport;

    public RefeicaoController(RefeicaoService refeicaoService,
                              RefeicaoQueryService refeicaoQueryService,
                              FichaTecnicaService fichaTecnicaService,
                              PaginacaoViewSupport paginacaoViewSupport) {
        this.refeicaoService = refeicaoService;
        this.refeicaoQueryService = refeicaoQueryService;
        this.fichaTecnicaService = fichaTecnicaService;
        this.paginacaoViewSupport = paginacaoViewSupport;
    }


    @GetMapping("/{id}/nutrientes-totais")
    public ResponseEntity<RefeicaoNutrientesResponseDTO> getTotaisNutrientesRefeicao(@PathVariable @NonNull Long id) {
        RefeicaoNutrientesResponseDTO dto = refeicaoService.buscarTotaisNutrientesPorId(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping({"/buscar", "/por-status", ""})
    public String buscar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "status", required = false) String statusParam,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal @NonNull UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<RefeicaoResponseDTO> refeicoes;
        Status status = parseStatus(statusParam);
        
        if (nome != null && !nome.isBlank()) {
            refeicoes = refeicaoQueryService.buscarPorNome(nome, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("termoBusca", nome);
            model.addAttribute("statusAtual", status != null ? status : ATIVA);
        } else if (status != null) {
            refeicoes = refeicaoQueryService.buscarPorStatus(status, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("statusAtual", status);
        } else {
            refeicoes = refeicaoQueryService.buscarPorStatus(ATIVA, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("statusAtual", ATIVA);
        }
        
        model.addAttribute("refeicoes", refeicoes.getContent());
        model.addAttribute("fichasTecnicasLista", fichaTecnicaService.listarResumo());
        paginacaoViewSupport.configurarPaginacao(model, request, page, refeicoes);
        return paginacaoViewSupport.renderizarView("pages/refeicoes/List", htmxRequest, model, page > 0);
    }


    @GetMapping("/pesquisar")
    public String pesquisarPorNome(
            @RequestParam("nome") String nome,
            @RequestParam(value = "status", required = false) String statusParam,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal @NonNull UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        return buscar(nome, statusParam, page, model, usuarioPrincipal, request, htmxRequest);
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatusRefeicao(@PathVariable("id") @NonNull Long id,
                                       @RequestParam(value = "currentStatus", required = false) String currentStatus) {
        refeicaoService.atualizaStatus(id);

        if (currentStatus != null && currentStatus.equals("INATIVA")) {
            return "redirect:/refeicao/por-status?status=INATIVA";
        }
        return "redirect:/refeicao";
    }

    @GetMapping("/editar/{id}")
    @ResponseBody
    public RefeicaoResponseDTO mostrarFormularioEdicaoRefeicoes(@PathVariable("id") @NonNull Long id) {
        return refeicaoService.buscarPorId(id);
    }

    @PostMapping("/novo")
    public String criarRefeicao(
            @Valid @ModelAttribute("dto") RefeicaoDTO dto,
            BindingResult result,
            @AuthenticationPrincipal @NonNull UsuarioSecurity usuarioPrincipal) {

        if (result.hasErrors()) {
            var fieldError = result.getFieldError();
            throw new IllegalArgumentException(fieldError != null ? fieldError.getDefaultMessage() : "Erro de validação");
        }

        refeicaoService.create(dto, Objects.requireNonNull(usuarioPrincipal.getId()));
        return "redirect:/refeicao";
    }

    @PostMapping("/editar/{id}")
    public String atualizarRefeicao(
            @PathVariable("id") @NonNull Long id,
            @Valid @ModelAttribute("dto") RefeicaoDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            var fieldError = result.getFieldError();
            throw new IllegalArgumentException(fieldError != null ? fieldError.getDefaultMessage() : "Erro de validação");
        }

        refeicaoService.update(id, dto);
        return "redirect:/refeicao";
    }

    private Status parseStatus(String statusParam) {
        if (statusParam == null || statusParam.isBlank()) {
            return null;
        }

        try {
            return Status.valueOf(statusParam.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
