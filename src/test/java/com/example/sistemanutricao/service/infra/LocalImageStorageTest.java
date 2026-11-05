package com.example.sistemanutricao.service.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class LocalImageStorageTest {

    private final LocalImageStorage storage = new LocalImageStorage();

    @Test
    void deveAceitarJpegValidoERemoverCorretamente() {
        storage.init();
        byte[] jpegMagic = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        MockMultipartFile file = new MockMultipartFile("imagemPerfil", "avatar.jpg", "image/jpeg", jpegMagic);

        String path = storage.armazenarImagemPerfil(file, "usuario_teste");
        assertThat(path).startsWith("imagens-perfil/usuario_teste_").endsWith(".jpg");

        storage.removerImagemPerfil(path);
    }

    @Test
    void deveRejeitarExtensaoInvalidaComoSvgOuHtml() {
        byte[] svgBytes = "<svg onload=alert(1)></svg>".getBytes();
        MockMultipartFile svg = new MockMultipartFile("imagemPerfil", "exploit.svg", "image/svg+xml", svgBytes);

        assertThatThrownBy(() -> storage.armazenarImagemPerfil(svg, "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Extensão de arquivo não permitida");
    }

    @Test
    void deveRejeitarMimeTypeInvalido() {
        byte[] jpegMagic = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        MockMultipartFile badMime = new MockMultipartFile("imagemPerfil", "foto.jpg", "text/html", jpegMagic);

        assertThatThrownBy(() -> storage.armazenarImagemPerfil(badMime, "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo MIME não permitido");
    }

    @Test
    void deveRejeitarMagicBytesFalsosMesmoComExtensaoEMimeCorretos() {
        byte[] fakeBytes = "<html><script>alert(1)</script></html>".getBytes();
        MockMultipartFile fakeJpg = new MockMultipartFile("imagemPerfil", "foto.jpg", "image/jpeg", fakeBytes);

        assertThatThrownBy(() -> storage.armazenarImagemPerfil(fakeJpg, "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conteúdo do arquivo não corresponde");
    }

    @Test
    void deveBloquearPathTraversalNoCarregamento() {
        assertThatThrownBy(() -> storage.carregarImagem("../../pom.xml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");
    }
}

