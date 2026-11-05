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

            validarImagem(arquivo);
            String extensao = "";
            String nomeOriginal = arquivo.getOriginalFilename();
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            String nomeArquivo = nomeUsuario.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;
            Path destino = this.rootLocation.resolve(nomeArquivo).normalize();
            if (!destino.startsWith(this.rootLocation.normalize())) {
                throw new IllegalArgumentException("Caminho de destino inválido.");
            }

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
            
            arquivo = arquivo.normalize();
            if (!arquivo.startsWith(this.rootLocation.normalize())) {
                throw new IllegalArgumentException("Acesso negado ao caminho informado.");
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
        executarRemoverImagemPerfil(caminhoImagem);
    }

    private void validarImagem(MultipartFile arquivo) throws IOException {
        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            throw new IllegalArgumentException("Arquivo sem extensão válida.");
        }
        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf(".")).toLowerCase();
        java.util.Set<String> extensoesPermitidas = java.util.Set.of(".jpg", ".jpeg", ".png", ".webp");
        if (!extensoesPermitidas.contains(extensao)) {
            throw new IllegalArgumentException("Extensão de arquivo não permitida. Use JPG, PNG ou WEBP.");
        }

        String contentType = arquivo.getContentType();
        java.util.Set<String> mimesPermitidos = java.util.Set.of("image/jpeg", "image/png", "image/webp");
        if (contentType == null || !mimesPermitidos.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo MIME não permitido.");
        }

        byte[] bytes = arquivo.getBytes();
        if (!possuiAssinaturaValida(bytes)) {
            throw new IllegalArgumentException("O conteúdo do arquivo não corresponde a uma imagem válida.");
        }
    }

    private boolean possuiAssinaturaValida(byte[] b) {
        if (b == null || b.length < 4) {
            return false;
        }
        boolean isJpeg = (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        boolean isPng = (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47;
        boolean isWebp = b.length >= 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        return isJpeg || isPng || isWebp;
    }

    private void executarRemoverImagemPerfil(String caminhoImagem) {
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
            if (!arquivo.startsWith(this.rootLocation.normalize())) {
                throw new IllegalArgumentException("Caminho inválido para remoção.");
            }

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
