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
    @Mapping(target = "pesoPorcao", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ficha.getPesoPorcao()))")
    @Mapping(target = "custoTotal", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ficha.getCustoTotal()))")
    @Mapping(target = "custoPerCapita", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ficha.getCustoPerCapita()))")
    FichaTecnicaGetDTO toGetDTO(FichaTecnica ficha);

    @Mapping(target = "rendimento", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(prep.getRendimento()))")
    @Mapping(target = "fcc", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(prep.getFcc()))")
    @Mapping(target = "qntdAgua", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(prep.getQntdAgua()))")
    @Mapping(target = "porcentAgua", expression = "java(com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(prep.getPorcentAgua()))")
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
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getCustoKG()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getCustoUsado()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getFc()),
            ipf.getMedidaCaseria(),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getPb()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getPl()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getPtnCalculado()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getChoCalculado()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getLipCalculado()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getSodioCalculado()),
            com.example.sistemanutricao.util.NumberFormatUtil.limparDecimais(ipf.getGorduraSaturadaCalculada()),
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
