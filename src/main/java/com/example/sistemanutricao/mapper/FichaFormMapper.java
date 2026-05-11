package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaUpdateDTO;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoDTO;
import com.example.sistemanutricao.record.PreparacaoDTO.PreparacaoGetDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalGetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FichaFormMapper {

    FichaTecnicaUpdateDTO toUpdateDTO(FichaTecnicaGetDTO fichaGet);

    @Mapping(target = "fichaTecnica", ignore = true)
    IngredientePorFichaDTO mapIngrediente(IngredientePorFichaDTO ing);

    PreparacaoDTO mapPreparacao(PreparacaoGetDTO prep);

    PerfilNutricionalDTO mapPerfil(PerfilNutricionalGetDTO perfil);
}
