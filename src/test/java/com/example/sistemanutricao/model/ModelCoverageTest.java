package com.example.sistemanutricao.model;

import com.example.sistemanutricao.model.enuns.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelCoverageTest {

    @Test
    void testEstabelecimento() {
        Estabelecimento e = new Estabelecimento();
        e.setId(1L);
        e.setNome("Teste");
        
        assertEquals(1L, e.getId());
        assertEquals("Teste", e.getNome());
        
        Estabelecimento e2 = new Estabelecimento();
        e2.setId(1L);
        assertEquals(e.getId(), e2.getId());
    }

    @Test
    void testFichaTecnica() {
        FichaTecnica f = new FichaTecnica();
        f.setId(1L);
        f.setCustoPerCapita(new BigDecimal("10.00"));
        f.setCustoTotal(new BigDecimal("20.00"));
        f.setPesoPorcao(new BigDecimal("150.0"));
        f.setMedidaCaseira("1 Escumadeira");
        f.setStatus(Status.ATIVA);
        f.setStatusCriacao(StatusCriacao.COMPLETA);
        
        assertEquals(1L, f.getId());
        assertEquals(new BigDecimal("10.00"), f.getCustoPerCapita());
        assertEquals(new BigDecimal("20.00"), f.getCustoTotal());
        assertEquals(new BigDecimal("150.0"), f.getPesoPorcao());
        assertEquals("1 Escumadeira", f.getMedidaCaseira());
        assertEquals(Status.ATIVA, f.getStatus());
        assertEquals(StatusCriacao.COMPLETA, f.getStatusCriacao());
        
        FichaTecnica f2 = new FichaTecnica();
        f2.setId(1L);
        assertEquals(f.getId(), f2.getId());
    }

    @Test
    void testIngrediente() {
        Ingrediente i = new Ingrediente();
        i.setId(1L);
        i.setNome("Arroz");
        i.setPtn(new BigDecimal("2.5"));
        i.setCho(new BigDecimal("28.0"));
        i.setLip(new BigDecimal("0.2"));
        i.setSodio(new BigDecimal("1.0"));
        i.setGorduraSaturada(new BigDecimal("0.1"));
        i.setStatus(Status.ATIVA);
        
        assertEquals(1L, i.getId());
        assertEquals("Arroz", i.getNome());
        assertEquals(new BigDecimal("2.5"), i.getPtn());
        assertEquals(new BigDecimal("28.0"), i.getCho());
        assertEquals(new BigDecimal("0.2"), i.getLip());
        assertEquals(new BigDecimal("1.0"), i.getSodio());
        assertEquals(new BigDecimal("0.1"), i.getGorduraSaturada());
        assertEquals(Status.ATIVA, i.getStatus());
        
        Ingrediente i2 = new Ingrediente();
        i2.setId(1L);
        assertEquals(i.getId(), i2.getId());
    }

    @Test
    void testPerfilNutricional() {
        PerfilNutricional p = new PerfilNutricional();
        p.setId(1L);
        p.setVtc(new BigDecimal("100.0"));
        p.setGramasPTN(new BigDecimal("10.0"));
        p.setGramasCHO(new BigDecimal("20.0"));
        p.setGramasLIP(new BigDecimal("5.0"));
        
        assertEquals(1L, p.getId());
        assertEquals(new BigDecimal("100.0"), p.getVtc());
        assertEquals(new BigDecimal("10.0"), p.getGramasPTN());
        assertEquals(new BigDecimal("20.0"), p.getGramasCHO());
        assertEquals(new BigDecimal("5.0"), p.getGramasLIP());
    }

    @Test
    void testPreparacao() {
        Preparacao p = new Preparacao();
        p.setId(1L);
        p.setNome("Cozimento");
        p.setTempoPreparo("10 min");
        p.setModoPreparo("Ferver");
        p.setEquipamentos("Panela");
        p.setRendimento(new BigDecimal("500.0"));
        
        assertEquals(1L, p.getId());
        assertEquals("Cozimento", p.getNome());
        assertEquals("10 min", p.getTempoPreparo());
        assertEquals("Ferver", p.getModoPreparo());
        assertEquals("Panela", p.getEquipamentos());
        assertEquals(new BigDecimal("500.0"), p.getRendimento());
    }

    @Test
    void testRefeicao() {
        Refeicao r = new Refeicao();
        r.setId(1L);
        r.setNome("Almoço");
        r.setKcalTotal("500");
        r.setStatus(Status.ATIVA);
        
        assertEquals(1L, r.getId());
        assertEquals("Almoço", r.getNome());
        assertEquals("500", r.getKcalTotal());
        assertEquals(Status.ATIVA, r.getStatus());
    }

    @Test
    void testUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("Admin");
        u.setEmail("admin@test.com");
        u.setSenha("123");
        u.setCargo(Cargo.ADMIN);
        
        assertEquals(1L, u.getId());
        assertEquals("Admin", u.getUsername());
        assertEquals("admin@test.com", u.getEmail());
        assertEquals("123", u.getSenha());
        assertEquals(Cargo.ADMIN, u.getCargo());
    }
}
