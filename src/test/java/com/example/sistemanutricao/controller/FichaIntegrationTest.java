package com.example.sistemanutricao.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

@DisplayName("Ficha Técnica - Teste de Integração")
class FichaIntegrationTest extends BaseIntegrationTest {

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
        estabelecimento.setNome("Restaurante Central");
        estabelecimento = estabelecimentoRepository.save(estabelecimento);

        nutriUser = new Usuario();
        nutriUser.setUsername("nutri_integracao");
        nutriUser.setEmail("nutri@integracao.com");
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
        preparacao.setCategoria(Categoria.COMPLEMENTO);
        preparacao.setNumero(numero);
        preparacao.setTempoPreparo("20 min");
        preparacao.setEquipamentos("Equipamentos");
        preparacao.setModoPreparo("Modo de preparo");
        preparacao.setQntdAgua(new BigDecimal("1.0"));
        preparacao.setPorcentAgua(new BigDecimal("10.0"));
        preparacao.setFcc(new BigDecimal("1.0"));
        preparacao.setRendimento(new BigDecimal("100.0"));
        preparacao = preparacaoRepository.save(preparacao);

        PerfilNutricional perfil = new PerfilNutricional();
        perfil.setVtc(new BigDecimal("100"));
        perfil.setGramasPTN(new BigDecimal("10"));
        perfil.setGramasCHO(new BigDecimal("10"));
        perfil.setGramasLIP(new BigDecimal("10"));
        perfil.setGramasSodio(new BigDecimal("10"));
        perfil.setGramasSaturada(new BigDecimal("10"));
        perfil = perfilNutricionalRepository.save(perfil);

        FichaTecnica ficha = new FichaTecnica();
        ficha.setCustoPerCapita(new BigDecimal("1.0"));
        ficha.setCustoTotal(new BigDecimal("10.0"));
        ficha.setNumeroPorcoes(10);
        ficha.setPesoPorcao(new BigDecimal("100"));
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
    @DisplayName("Deve listar fichas técnicas do banco de dados")
    void deveListarFichas() throws Exception {
        // GIVEN
        criarFicha("Arroz Branco", 101);

        // WHEN & THEN
        mockMvc.perform(get("/ficha").with(user(nutriSecurity)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("fichas"))
                .andExpect(model().attribute("view", "pages/fichas/List"))
                .andExpect(view().name("Layout"));
    }

    @Test
    @DisplayName("Deve exibir detalhes de uma ficha existente")
    void deveExibirDetalhes() throws Exception {
        // GIVEN
        FichaTecnica ficha = criarFicha("Feijão Preto", 102);

        // WHEN & THEN
        mockMvc.perform(get("/ficha/" + ficha.getId()).with(user(nutriSecurity)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ficha"))
                .andExpect(model().attribute("view", "pages/fichas/Detail"))
                .andExpect(view().name("Layout"));
    }
}
