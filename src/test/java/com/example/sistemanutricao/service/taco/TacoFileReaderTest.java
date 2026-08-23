package com.example.sistemanutricao.service.taco;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TacoFileReaderTest {

    private final TacoFileReader tacoFileReader = new TacoFileReader();

    @Test
    void abrirArquivo_WhenFileExists() {
        Optional<InputStream> result = tacoFileReader.abrirArquivo("application.properties"); // any file that exists in classpath
        assertThat(result).isPresent();
    }

    @Test
    void abrirArquivo_WhenFileDoesNotExist() {
        Optional<InputStream> result = tacoFileReader.abrirArquivo("nonexistent.xlsx");
        assertThat(result).isEmpty();
    }

    @Test
    void abrirArquivo_WhenPathIsNull() {
        Optional<InputStream> result = tacoFileReader.abrirArquivo(null);
        assertThat(result).isEmpty();
    }
}
