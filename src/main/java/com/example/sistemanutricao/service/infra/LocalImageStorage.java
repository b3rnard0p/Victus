package com.example.sistemanutricao.service.infra;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.sistemanutricao.service.port.ImageStorage;

import jakarta.annotation.PostConstruct;

@Service
public class LocalImageStorage implements ImageStorage {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageStorage.class);
    private final Path rootLocation = Paths.get("./uploads/imagens-perfil");

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Diretório de imagens inicializado em: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar o diretório de upload", e);
        }
    }

    @Override
    public String armazenarImagemPerfil(MultipartFile arquivo, String nomeUsuario) {
        try {
            if (arquivo.isEmpty()) {
                throw new RuntimeException("Arquivo vazio");
            }

            String extensao = "";
            String nomeOriginal = arquivo.getOriginalFilename();
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            String nomeArquivo = nomeUsuario.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;
            Path destino = this.rootLocation.resolve(nomeArquivo);

            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Imagem salva em: {}", destino.toAbsolutePath());

            return "imagens-perfil/" + nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar arquivo", e);
        }
    }

    @Override
    public Resource carregarImagem(String nomeArquivo) {
        try {
            Path arquivo;
            if (nomeArquivo.startsWith("imagens-perfil/")) {
                arquivo = rootLocation.resolve(nomeArquivo.substring("imagens-perfil/".length()));
            } else {
                arquivo = rootLocation.resolve(nomeArquivo);
            }
            
            Resource resource = new UrlResource(arquivo.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Não foi possível ler o arquivo: " + nomeArquivo);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erro ao ler o arquivo: " + nomeArquivo, e);
        }
    }

    @Override
    public void removerImagemPerfil(String caminhoImagem) {
        if (caminhoImagem == null || caminhoImagem.isEmpty()) {
            return;
        }

        try {
            String nomeArquivo = caminhoImagem;

            if (nomeArquivo.startsWith("/")) {
                nomeArquivo = nomeArquivo.substring(1);
            }

            if (nomeArquivo.startsWith("imagens-perfil/")) {
                nomeArquivo = nomeArquivo.substring("imagens-perfil/".length());
            }
            
            Path arquivo = this.rootLocation.resolve(nomeArquivo).normalize();

            if (Files.exists(arquivo) && Files.isRegularFile(arquivo)) {
                Files.delete(arquivo);
                logger.info("Imagem de perfil antiga removida: {}", arquivo.getFileName());
            } else {
                logger.debug("Nenhum arquivo físico encontrado para remover em: {}", arquivo);
            }
        } catch (IOException e) {
            logger.error("Falha ao tentar remover imagem de perfil: {}", caminhoImagem, e);
        }
    }
}
