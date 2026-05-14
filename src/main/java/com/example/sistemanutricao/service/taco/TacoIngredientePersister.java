package com.example.sistemanutricao.service.taco;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.specification.IngredienteSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class TacoIngredientePersister {

    private static final int MAX_SEARCH_RESULTS = 500;

    private static final Logger logger = LoggerFactory.getLogger(TacoIngredientePersister.class);

    private final IngredienteRepository ingredienteRepository;
    private final TacoExcelParser tacoExcelParser;

    public TacoIngredientePersister(IngredienteRepository ingredienteRepository, TacoExcelParser tacoExcelParser) {
        this.ingredienteRepository = ingredienteRepository;
        this.tacoExcelParser = tacoExcelParser;
    }

    @Transactional
    public void persistir(Usuario tacoUsuario, List<TacoIngredienteLinha> linhasTaco, Map<String, BigDecimal> saturadasPorNome) {
        var spec = IngredienteSpecification.filter(null, tacoUsuario.getId(), null, null);
        List<Ingrediente> ingredientesAtuais = ingredienteRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(0, MAX_SEARCH_RESULTS)).getContent();
        Set<String> nomesExistentes = new HashSet<>();
        Map<String, Ingrediente> ingredientesExistentesPorNome = new HashMap<>();

        for (Ingrediente ingrediente : ingredientesAtuais) {
            if (ingrediente.getNome() != null) {
                String nomeNormalizado = tacoExcelParser.normalizarNome(ingrediente.getNome());
                nomesExistentes.add(nomeNormalizado);
                ingredientesExistentesPorNome.putIfAbsent(nomeNormalizado, ingrediente);
            }
        }

        List<Ingrediente> novosIngredientes = new ArrayList<>();
        List<Ingrediente> ingredientesParaAtualizar = new ArrayList<>();
        int ingredientesSemGorduraSaturada = 0;

        for (TacoIngredienteLinha linha : linhasTaco) {
            String nomeNormalizado = tacoExcelParser.normalizarNome(linha.nome());
            if (nomeNormalizado.isEmpty()) {
                continue;
            }

            BigDecimal gorduraSaturada = saturadasPorNome.get(nomeNormalizado);
            if (nomesExistentes.contains(nomeNormalizado)) {
                Ingrediente ingredienteExistente = ingredientesExistentesPorNome.get(nomeNormalizado);
                if (ingredienteExistente != null && gorduraSaturada != null && !Objects.equals(ingredienteExistente.getGorduraSaturada(), gorduraSaturada)) {
                    ingredienteExistente.setGorduraSaturada(gorduraSaturada);
                    ingredientesParaAtualizar.add(ingredienteExistente);
                }
                continue;
            }

            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setNome(linha.nome());
            ingrediente.setPtn(linha.ptn() != null ? linha.ptn() : BigDecimal.ZERO);
            ingrediente.setCho(linha.cho() != null ? linha.cho() : BigDecimal.ZERO);
            ingrediente.setLip(linha.lip() != null ? linha.lip() : BigDecimal.ZERO);
            ingrediente.setSodio(linha.sodio() != null ? linha.sodio() : BigDecimal.ZERO);
            if (gorduraSaturada == null) {
                ingrediente.setGorduraSaturada(BigDecimal.ZERO);
                ingredientesSemGorduraSaturada++;
            } else {
                ingrediente.setGorduraSaturada(gorduraSaturada);
            }
            ingrediente.setStatus(Status.ATIVA);
            ingrediente.setUsuario(tacoUsuario);
            novosIngredientes.add(ingrediente);
        }

        if (!ingredientesParaAtualizar.isEmpty()) {
            ingredienteRepository.saveAll(ingredientesParaAtualizar);
            logger.info("Tabela TACO atualizada com gordura saturada em {} ingrediente(s) existente(s).", ingredientesParaAtualizar.size());
        }

        if (novosIngredientes.isEmpty()) {
            logger.info("Tabela TACO já está preenchida para o usuário {}.", tacoUsuario.getUsername());
            return;
        }

        ingredienteRepository.saveAll(novosIngredientes);
        if (ingredientesSemGorduraSaturada > 0) {
            logger.info("Importação da TACO concluída. {} ingrediente(s) adicionados ao usuário {}. {} ingrediente(s) com gordura saturada padrão (0) por não encontrar no arquivo de ácidos.",
                    novosIngredientes.size(), tacoUsuario.getUsername(), ingredientesSemGorduraSaturada);
        } else {
            logger.info("Importação da TACO concluída. {} ingrediente(s) adicionados ao usuário {}.", novosIngredientes.size(), tacoUsuario.getUsername());
        }
    }

}
