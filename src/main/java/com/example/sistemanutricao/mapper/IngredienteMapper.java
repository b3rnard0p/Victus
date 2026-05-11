package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteComTagDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

    @Mapping(source = "usuario.id", target = "usuarioId")
    IngredienteGetDTO toGetDto(Ingrediente ingrediente);

    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "ingredientesPorFicha", ignore = true)
    Ingrediente toEntity(IngredienteDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "ingredientesPorFicha", ignore = true)
    void updateEntity(@MappingTarget Ingrediente ingrediente, IngredienteDTO dto);

    @Mapping(source = "ingrediente.usuario.id", target = "usuarioId")
    IngredienteComTagDTO toTagDto(Ingrediente ingrediente, String tag);
}
