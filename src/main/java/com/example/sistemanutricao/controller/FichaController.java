package com.example.sistemanutricao.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import com.example.sistemanutricao.model.enuns.Categoria;
import com.example.sistemanutricao.model.enuns.Status;
import static com.example.sistemanutricao.model.enuns.Status.ATIVA;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import static com.example.sistemanutricao.model.enuns.StatusCriacao.COMPLETA;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaCreateDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;

import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.security.UsuarioSecurity;
import com.example.sistemanutricao.controller.support.PaginacaoViewSupport;
import com.example.sistemanutricao.exception.FormValidationException;
import com.example.sistemanutricao.service.ficha.FichaQueryService;
import com.example.sistemanutricao.service.ficha.FichaTecnicaService;
import com.example.sistemanutricao.service.port.PdfExporter;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/ficha"})
@CrossOrigin("*")
@PreAuthorize("hasAnyRole('NUTRICIONISTA', 'PRODUCAO')")
public class FichaController {

    private static final int TAMANHO_PAGINA = 6;

    private final FichaTecnicaService fichaTecnicaService;
    private final FichaQueryService fichaQueryService;
    private final PdfExporter pdfExporter;
    private final com.example.sistemanutricao.mapper.FichaFormMapper fichaFormMapper;
    private final PaginacaoViewSupport paginacaoViewSupport;

    public FichaController(FichaTecnicaService fichaTecnicaService,
                           FichaQueryService fichaQueryService,
                           PdfExporter pdfExporter,
                           PaginacaoViewSupport paginacaoViewSupport,
                           com.example.sistemanutricao.mapper.FichaFormMapper fichaFormMapper) {
        this.fichaTecnicaService = fichaTecnicaService;
        this.fichaQueryService = fichaQueryService;
        this.pdfExporter = pdfExporter;
        this.fichaFormMapper = fichaFormMapper;
        this.paginacaoViewSupport = paginacaoViewSupport;
    }

    @ModelAttribute("categorias")
    public com.example.sistemanutricao.model.enuns.Categoria[] getCategorias() {
        return com.example.sistemanutricao.model.enuns.Categoria.values();
    }

    @ModelAttribute("filtroFichaOptions")
    public List<Map<String, String>> getFiltroFichaOptions() {
        return List.of(
                Map.of("value", "por-nome",        "label", "Nome"),
                Map.of("value", "por-categoria",   "label", "Categoria"),
                Map.of("value", "por-numero",      "label", "Número"),
                Map.of("value", "vtc",             "label", "VTC"),
                Map.of("value", "gramasPTN",       "label", "PTN"),
                Map.of("value", "gramasCHO",       "label", "CHO"),
                Map.of("value", "gramasLIP",       "label", "LIP"),
                Map.of("value", "gramasSodio",     "label", "Sódio"),
                Map.of("value", "gramasSaturada",  "label", "Gordura Saturada")
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

    @GetMapping({"/custoPerCapita", "/custoTotal", "/por-nome", "/por-rendimento", "/por-numero", "/por-vtc", "/por-gramas-ptn",
                 "/por-gramas-cho", "/por-gramas-lip", "/por-gramas-sodio", "/por-gramas-saturada", "/por-categoria", "/por-tag"})
    public String redirectOldSearch(HttpServletRequest request) {
        String query = request.getQueryString();
        return "redirect:/ficha" + (query != null ? "?" + query : "");
    }

    @GetMapping("")
    public String listarTodasFichas(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<FichaTecnicaGetDTO> fichas = fichaQueryService.buscarPorStatus(ATIVA, COMPLETA, usuarioPrincipal.getUsuario(), pageable);
        model.addAttribute("fichas", fichas.getContent());
        model.addAttribute("statusAtual", ATIVA);
        paginacaoViewSupport.configurarPaginacao(model, request, page, fichas);
        return paginacaoViewSupport.renderizarView("pages/fichas/List", htmxRequest, model, page > 0);
    }

    @GetMapping({"/por-status", "/por-statusCriacao", "/buscar"})
    public String buscar(
            @RequestParam(value = "campo", required = false) String campo,
            @RequestParam(value = "valorPesquisa", required = false) String valor,
            @RequestParam(value = "tipoPesquisa", required = false) String tipo,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "statusCriacao", required = false) StatusCriacao statusCriacao,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            HttpServletRequest request,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        Pageable pageable = PageRequest.of(Math.max(page, 0), TAMANHO_PAGINA);
        Page<?> fichasPage;
        
        if (campo != null && valor != null && tipo != null) {
            @SuppressWarnings("unchecked")
            Page<?> result = fichaQueryService.pesquisar(campo, valor, tipo, usuarioPrincipal.getUsuario(), pageable);
            fichasPage = result;
            if ("tags".equals(tipo)) {
                model.addAttribute("pesquisaPorTag", true);
                model.addAttribute("isComTagDTO", true);
            }
            model.addAttribute("statusAtual", status != null ? status : ATIVA);
        } else if (status != null && statusCriacao != null) {
            fichasPage = fichaQueryService.buscarPorStatus(status, statusCriacao, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("statusAtual", status);
            model.addAttribute("statusCriacaoAtual", statusCriacao);
        } else if (status != null) {
            fichasPage = fichaQueryService.buscarPorStatusSimples(status, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("statusAtual", status);
        } else if (statusCriacao != null) {
            fichasPage = fichaQueryService.buscarPorStatus(ATIVA, statusCriacao, usuarioPrincipal.getUsuario(), pageable);
            model.addAttribute("statusCriacaoAtual", statusCriacao);
        } else {
            fichasPage = fichaQueryService.buscarPorStatus(ATIVA, COMPLETA, usuarioPrincipal.getUsuario(), pageable);
        }
        
        model.addAttribute("fichas", fichasPage.getContent());
        paginacaoViewSupport.configurarPaginacao(model, request, page, fichasPage);
        return paginacaoViewSupport.renderizarView("pages/fichas/List", htmxRequest, model, page > 0);
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
        return buscar(campo, valor, tipo, null, null, page, model, usuarioPrincipal, request, htmxRequest);
    }



    @GetMapping("/{id:[0-9]+}")
    public String mostrarFichaPorId(
            @PathVariable Long id, Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        try {
            FichaTecnicaGetDTO ficha = fichaTecnicaService.getFichaById(id);
            model.addAttribute("ficha", ficha);
            return paginacaoViewSupport.renderizarView("pages/fichas/Detail", htmxRequest, model);
        } catch (EntityNotFoundException e) {
            return "redirect:/fichas?error=Ficha não encontrada";
        }
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        fichaTecnicaService.atualizaStatus(id);
        return "redirect:/ficha/" + id;
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(
            @PathVariable Long id, Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        FichaTecnicaGetDTO fichaGet = fichaTecnicaService.getFichaById(id);
        FichaTecnicaUpdateDTO fichaEdicao = fichaFormMapper.toUpdateDTO(fichaGet);
        model.addAttribute("ingredientesDisponiveis", new ArrayList<IngredienteGetDTO>());
        model.addAttribute("ficha", fichaEdicao);
        model.addAttribute("categorias", Categoria.values());
        return paginacaoViewSupport.renderizarView("pages/fichas/FormFicha", htmxRequest, model);
    }

    @PostMapping("/editar/{id}")
    public String updateFichaTecnica(
            @PathVariable Long id,
            @Valid @ModelAttribute("ficha") FichaTecnicaUpdateDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        if (result.hasErrors()) {
            throw new FormValidationException(result.getFieldError().getDefaultMessage());
        }

        fichaTecnicaService.update(id, dto);
        return "redirect:/ficha";
    }

    @GetMapping("/nova")
    public String mostrarFormularioNovaFicha(
            Model model,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("ingredientesDisponiveis", new ArrayList<IngredienteGetDTO>());

        model.addAttribute("ficha", new FichaTecnicaCreateDTO(null, null,null, null, null, null,
                ATIVA,null ,null,null, new PreparacaoDTO(null,"", null, "", "","",
                null, null, null, null, null), new ArrayList<>(),
                new PerfilNutricionalDTO(null, null, null, null, null,
                        null, null, null, null, null, null,
                        null, null
                )
        ));

        return paginacaoViewSupport.renderizarView("pages/fichas/FormFicha", htmxRequest, model);
    }

    @PostMapping
    public String salvarFichaTecnica(
            @Valid @ModelAttribute("ficha") FichaTecnicaCreateDTO fichaTecnicaDTO,
            BindingResult result,
            @AuthenticationPrincipal UsuarioSecurity usuarioPrincipal,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest) {
        
        if (result.hasErrors()) {
            throw new FormValidationException(result.getFieldError().getDefaultMessage());
        }

        Long usuarioId = usuarioPrincipal.getId();
        fichaTecnicaService.create(fichaTecnicaDTO, usuarioId);
        return "redirect:/ficha";
    }

    @GetMapping("/exportar-pdf/{id}")
    public ResponseEntity<ByteArrayResource> exportarFichaTecnica(@PathVariable Long id) {
        try {
            FichaTecnicaGetDTO ficha = fichaTecnicaService.getFichaById(id);
            byte[] pdfBytes = pdfExporter.generateFichaTecnicaPdf(ficha);

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            HttpHeaders headers = new HttpHeaders();
            String fileName = ficha.preparacao().nome().replaceAll("[^a-zA-Z0-9\\s-]", "").replaceAll("\\s+", "_");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
