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

@Mapper(componentModel = "spring")
public interface FichaTecnicaMapper {

    @Mapping(source = "ingredientesPorFicha", target = "ingredientes")
    FichaTecnicaGetDTO toGetDTO(FichaTecnica ficha);

    PreparacaoGetDTO mapPreparacao(Preparacao prep);

    PerfilNutricionalGetDTO mapPerfil(PerfilNutricional perfil);

    default IngredientePorFichaDTO mapIngredientePorFicha(IngredientesPorFicha ipf) {
        if (ipf == null) {
            return null;
        }

        return new IngredientePorFichaDTO(
            ipf.getId(),
            ipf.getIngrediente() != null ? ipf.getIngrediente().getId() : null,
            mapIngrediente(ipf.getIngrediente()),
            ipf.getCustoKG(),
            ipf.getCustoUsado(),
            ipf.getFc(),
            ipf.getMedidaCaseria(),
            ipf.getPb(),
            ipf.getPl(),
            ipf.getPtnCalculado(),
            ipf.getChoCalculado(),
            ipf.getLipCalculado(),
            ipf.getSodioCalculado(),
            ipf.getGorduraSaturadaCalculada(),
            null
        );
    }

    @Mapping(source = "usuario.id", target = "usuarioId")
    IngredienteGetDTO mapIngrediente(Ingrediente ing);

    @Mapping(target = "id", ignore = true)
    void updatePreparacao(@MappingTarget Preparacao prep, PreparacaoDTO dto);
    @Mapping(target = "id", ignore = true)
    Preparacao toPreparacao(PreparacaoDTO dto);

    @Mapping(target = "id", ignore = true)
    PerfilNutricional toPerfilNutricional(PerfilNutricionalDTO dto);

    @Mapping(target = "id", ignore = true)
    void updatePerfil(@MappingTarget PerfilNutricional perfil, PerfilNutricionalDTO dto);
}
