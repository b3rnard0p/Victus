package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.model.*;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalGetDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FichaTecnicaMapper {

    @Mapping(source = "ingredientesPorFicha", target = "ingredientes")
    FichaTecnicaGetDTO toGetDTO(FichaTecnica ficha);

    PreparacaoGetDTO mapPreparacao(Preparacao prep);

    PerfilNutricionalGetDTO mapPerfil(PerfilNutricional perfil);

    @Mapping(source = "ingrediente", target = "ingrediente")
    @Mapping(source = "custoKG", target = "custoKg")
    @Mapping(source = "medidaCaseria", target = "medidaCaseira")
    @Mapping(target = "fichaTecnica", ignore = true)
    IngredientePorFichaDTO mapIngredientePorFicha(IngredientesPorFicha ipf);

    @Mapping(source = "usuario.id", target = "usuarioId")
    IngredienteGetDTO mapIngrediente(Ingrediente ing);

    @Mapping(target = "id", ignore = true)
    void updatePreparacao(@MappingTarget Preparacao prep, PreparacaoDTO dto);

    @Mapping(target = "id", ignore = true)
    void updatePerfil(@MappingTarget PerfilNutricional perfil, PerfilNutricionalDTO dto);

    @Mapping(target = "id", ignore = true)
    Preparacao toPreparacao(PreparacaoDTO dto);

    @Mapping(target = "id", ignore = true)
    PerfilNutricional toPerfilNutricional(PerfilNutricionalDTO dto);
}
