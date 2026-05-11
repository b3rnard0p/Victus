package com.example.sistemanutricao.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.sistemanutricao.BaseIntegrationTest;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.security.UsuarioSecurity;

@DisplayName("Segurança - Teste de Integração (RBAC)")
class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private com.example.sistemanutricao.repository.EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UsuarioSecurity adminSecurity;
    private UsuarioSecurity nutriSecurity;
    private UsuarioSecurity producaoSecurity;
    private com.example.sistemanutricao.model.Estabelecimento estabelecimento;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        estabelecimentoRepository.deleteAll();

        estabelecimento = new com.example.sistemanutricao.model.Estabelecimento();
        estabelecimento.setNome("Estabelecimento Teste");
        estabelecimento = estabelecimentoRepository.save(estabelecimento);

        Usuario admin = criarUsuario("admin_test", Cargo.ADMIN);
        Usuario nutri = criarUsuario("nutri_test", Cargo.NUTRICIONISTA);
        Usuario producao = criarUsuario("prod_test", Cargo.PRODUCAO);

        adminSecurity = new UsuarioSecurity(admin);
        nutriSecurity = new UsuarioSecurity(nutri);
        producaoSecurity = new UsuarioSecurity(producao);
    }

    private Usuario criarUsuario(String username, Cargo cargo) {
        Usuario user = new Usuario();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setSenha(passwordEncoder.encode("password"));
        user.setCargo(cargo);
        user.setAtivo(true);
        user.setEstabelecimento(estabelecimento);
        return usuarioRepository.save(user);
    }

    @Test
    @DisplayName("ADMIN deve acessar endpoints de administração")
    void adminDeveAcessarAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user(adminSecurity)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("NUTRICIONISTA NÃO deve acessar endpoints de administração")
    void nutriNaoDeveAcessarAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user(nutriSecurity)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("PRODUCAO NÃO deve acessar endpoints de administração")
    void producaoNaoDeveAcessarAdmin() throws Exception {
        mockMvc.perform(get("/admin/usuarios").with(user(producaoSecurity)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("NUTRICIONISTA deve acessar endpoints de ficha")
    void nutriDeveAcessarFicha() throws Exception {
        mockMvc.perform(get("/ficha").with(user(nutriSecurity)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PRODUCAO deve acessar endpoints de ficha")
    void producaoDeveAcessarFicha() throws Exception {
        mockMvc.perform(get("/ficha").with(user(producaoSecurity)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuário não autenticado deve ser redirecionado para login")
    void anonimoDeveSerRedirecionado() throws Exception {
        mockMvc.perform(get("/ficha"))
                .andExpect(status().is3xxRedirection());
    }
}
