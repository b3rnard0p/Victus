package com.example.sistemanutricao.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sistemanutricao.BaseIntegrationTest;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.model.FichaTecnica;
import com.example.sistemanutricao.model.PerfilNutricional;
import com.example.sistemanutricao.model.Preparacao;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.enuns.Categoria;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import com.example.sistemanutricao.repository.FichaTecnicaRepository;
import com.example.sistemanutricao.repository.PerfilNutricionalRepository;
import com.example.sistemanutricao.repository.PreparacaoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;

@DisplayName("Ficha Técnica - Teste de UI (HTMX)")
class FichaHtmxIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FichaTecnicaRepository fichaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private PreparacaoRepository preparacaoRepository;

    @Autowired
    private PerfilNutricionalRepository perfilNutricionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario nutriUser;
    private UsuarioSecurity nutriSecurity;
    private Estabelecimento estabelecimento;

    @BeforeEach
    void setUp() {
        fichaRepository.deleteAll();
        usuarioRepository.deleteAll();
        estabelecimentoRepository.deleteAll();
        preparacaoRepository.deleteAll();
        perfilNutricionalRepository.deleteAll();

        estabelecimento = new Estabelecimento();
        estabelecimento.setNome("Restaurante HTMX");
        estabelecimento = estabelecimentoRepository.save(estabelecimento);

        nutriUser = new Usuario();
        nutriUser.setUsername("nutri_htmx");
        nutriUser.setEmail("nutri@htmx.com");
        nutriUser.setSenha(passwordEncoder.encode("password"));
        nutriUser.setCargo(Cargo.NUTRICIONISTA);
        nutriUser.setAtivo(true);
        nutriUser.setEstabelecimento(estabelecimento);
        nutriUser = usuarioRepository.save(nutriUser);

        nutriSecurity = new UsuarioSecurity(nutriUser);
    }

    private FichaTecnica criarFicha(String nome, int numero) {
        Preparacao preparacao = new Preparacao();
        preparacao.setNome(nome);
        preparacao.setCategoria(Categoria.PRATOPRINCIPAL);
        preparacao.setNumero(numero);
        preparacao.setTempoPreparo("30 min");
        preparacao.setEquipamentos("Equipamentos");
        preparacao.setModoPreparo("Modo");
        preparacao.setQntdAgua(BigDecimal.ONE);
        preparacao.setPorcentAgua(BigDecimal.TEN);
        preparacao.setFcc(BigDecimal.ONE);
        preparacao.setRendimento(BigDecimal.valueOf(100));
        preparacao = preparacaoRepository.save(preparacao);

        PerfilNutricional perfil = new PerfilNutricional();
        perfil.setVtc(BigDecimal.valueOf(200));
        perfil.setGramasPTN(BigDecimal.TEN);
        perfil.setGramasCHO(BigDecimal.TEN);
        perfil.setGramasLIP(BigDecimal.TEN);
        perfil.setGramasSodio(BigDecimal.TEN);
        perfil.setGramasSaturada(BigDecimal.TEN);
        perfil = perfilNutricionalRepository.save(perfil);

        FichaTecnica ficha = new FichaTecnica();
        ficha.setCustoPerCapita(BigDecimal.ONE);
        ficha.setCustoTotal(BigDecimal.TEN);
        ficha.setNumeroPorcoes(10);
        ficha.setPesoPorcao(BigDecimal.valueOf(100));
        ficha.setMedidaCaseira("1 un");
        ficha.setStatus(Status.ATIVA);
        ficha.setStatusCriacao(StatusCriacao.COMPLETA);
        ficha.setNutricionista(nutriUser);
        ficha.setPreparacao(preparacao);
        ficha.setPerfilNutricional(perfil);
        ficha.setIngredientesPorFicha(new ArrayList<>());
        return fichaRepository.save(ficha);
    }

    @Test
    @DisplayName("Deve retornar apenas o fragmento de conteúdo quando HX-Request for presente")
    void deveRetornarFragmentoConteudo() throws Exception {
        mockMvc.perform(get("/ficha")
                .header("HX-Request", "true")
                .with(user(nutriSecurity)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List :: conteudo"));
    }

    @Test
    @DisplayName("Deve retornar apenas o fragmento de itens na paginação (page > 0)")
    void deveRetornarFragmentoItens() throws Exception {
        // Criar fichas suficientes para ter paginação se necessário (embora o código use page > 0 para decidir)
        criarFicha("Ficha 1", 201);
        
        mockMvc.perform(get("/ficha")
                .param("page", "1")
                .header("HX-Request", "true")
                .with(user(nutriSecurity)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/List :: itens"));
    }

    @Test
    @DisplayName("Deve retornar fragmento de detalhe quando HX-Request for presente")
    void deveRetornarFragmentoDetalhe() throws Exception {
        FichaTecnica ficha = criarFicha("Ficha Detalhe", 202);

        mockMvc.perform(get("/ficha/" + ficha.getId())
                .header("HX-Request", "true")
                .with(user(nutriSecurity)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/fichas/Detail :: conteudo"));
    }
}
