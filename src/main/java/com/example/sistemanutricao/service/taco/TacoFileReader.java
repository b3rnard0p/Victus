package com.example.sistemanutricao.service.taco;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Component
public class TacoFileReader {

    private static final Logger logger = LoggerFactory.getLogger(TacoFileReader.class);

    public Optional<InputStream> abrirArquivo(String classpathLocation) {
        try {
            ClassPathResource classPathResource = new ClassPathResource(classpathLocation);
            if (classPathResource.exists()) {
                logger.info("Planilha carregada do classpath: {}", classpathLocation);
                return Optional.of(classPathResource.getInputStream());
            } else {
                logger.warn("Planilha não encontrada no classpath: {}", classpathLocation);
            }
        } catch (Exception e) {
            logger.error("Erro ao abrir planilha: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }
}
