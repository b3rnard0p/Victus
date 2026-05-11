package com.example.sistemanutricao.record.UsuarioDTO;

import com.example.sistemanutricao.model.enuns.Cargo;

public record GetUsuarioDTO(
        Long id,
        String username,
        String email,
        Cargo cargo,
        Long estabelecimentoId,
        String estabelecimentoNome,
        boolean ativo,
        String caminhoImagem
) {
}
