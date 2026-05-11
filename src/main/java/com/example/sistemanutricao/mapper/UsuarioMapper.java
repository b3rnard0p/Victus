package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "estabelecimento.id", target = "estabelecimentoId")
    @Mapping(source = "estabelecimento.nome", target = "estabelecimentoNome")
    GetUsuarioDTO toGetDTO(Usuario usuario);
}
