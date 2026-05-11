package com.example.sistemanutricao.record.EstabelecimentoDTO;

import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;

import java.util.List;

public record GetEstabelecimentoDTO(
        Long id,
        String nome,
        List<GetUsuarioDTO> usuarios
) {
}
