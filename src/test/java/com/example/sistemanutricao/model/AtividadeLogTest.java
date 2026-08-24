package com.example.sistemanutricao.model;

import com.example.sistemanutricao.model.enuns.TipoAcao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtividadeLogTest {

    @Test
    void testGettersAndSetters() {
        AtividadeLog log = new AtividadeLog();
        assertNull(log.getId());

        LocalDateTime now = LocalDateTime.now();
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        log.setDataHora(now);
        log.setUsuario(usuario);
        log.setTipoAcao(TipoAcao.CRIOU_FICHA);
        log.setDetalhes("Detalhes da ação");

        assertEquals(now, log.getDataHora());
        assertEquals(usuario, log.getUsuario());
        assertEquals(TipoAcao.CRIOU_FICHA, log.getTipoAcao());
        assertEquals("Detalhes da ação", log.getDetalhes());
    }

    @Test
    void testConstructorComParametros() {
        LocalDateTime now = LocalDateTime.now();
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        AtividadeLog log = new AtividadeLog(now, usuario, TipoAcao.EDITOU_FICHA, "Editou");

        assertEquals(now, log.getDataHora());
        assertEquals(usuario, log.getUsuario());
        assertEquals(TipoAcao.EDITOU_FICHA, log.getTipoAcao());
        assertEquals("Editou", log.getDetalhes());
    }

    @Test
    void testEqualsAndHashCode() throws Exception {
        AtividadeLog log1 = new AtividadeLog();
        AtividadeLog log2 = new AtividadeLog();
        
        // Use reflection to set ID since there is no setId
        var idField = AtividadeLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(log1, 1L);
        idField.set(log2, 1L);
        
        org.junit.jupiter.api.Assertions.assertEquals(log1, log2);
        org.junit.jupiter.api.Assertions.assertEquals(log1, log1);
        org.junit.jupiter.api.Assertions.assertNotEquals(null, log1);
        org.junit.jupiter.api.Assertions.assertNotEquals(new Object(), log1);
        assertEquals(log1.hashCode(), log2.hashCode());
        
        idField.set(log2, 2L);
        org.junit.jupiter.api.Assertions.assertNotEquals(log1, log2);
    }
}
