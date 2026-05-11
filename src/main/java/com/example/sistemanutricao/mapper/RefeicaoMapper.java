package com.example.sistemanutricao.mapper;

import com.example.sistemanutricao.model.FichasPorRefeicao;
import com.example.sistemanutricao.model.Refeicao;
import com.example.sistemanutricao.record.RefeicaoDTO.FichaTecnicaRefeicaoDTO;
import com.example.sistemanutricao.record.RefeicaoDTO.RefeicaoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefeicaoMapper {

    @Mapping(source = "fichasPorRefeicao", target = "fichasTecnicas")
    RefeicaoResponseDTO toResponseDTO(Refeicao refeicao);

    @Mapping(source = "fichaTecnica.id", target = "id")
    @Mapping(source = "fichaTecnica.preparacao.nome", target = "nomePreparacao")
    @Mapping(source = "fichaTecnica.perfilNutricional.vtc", target = "vct")
    FichaTecnicaRefeicaoDTO toFichaDTO(FichasPorRefeicao fp);

    List<FichaTecnicaRefeicaoDTO> toFichasDTOList(List<FichasPorRefeicao> fichasPorRefeicao);
}
