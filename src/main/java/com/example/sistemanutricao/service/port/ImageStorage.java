package com.example.sistemanutricao.service.port;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorage {
    String armazenarImagemPerfil(MultipartFile arquivo, String nomeUsuario);
    Resource carregarImagem(String nomeArquivo);
    void removerImagemPerfil(String caminhoImagem);
}
