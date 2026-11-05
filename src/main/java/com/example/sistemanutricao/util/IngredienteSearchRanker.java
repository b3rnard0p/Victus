package com.example.sistemanutricao.util;

import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;

import java.text.Normalizer;
import java.util.*;

public class IngredienteSearchRanker {

    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static int calcularPontuacao(String nomeOriginal, String queryOriginal, boolean isUsuario) {
        if (nomeOriginal == null || queryOriginal == null) {
            return 0;
        }

        String nome = normalizar(nomeOriginal);
        String query = normalizar(queryOriginal);

        if (nome.isEmpty() || query.isEmpty()) {
            return 0;
        }

        int score = 0;

        if (nome.equals(query)) {
            score += 10000;
        }
        else if (nome.startsWith(query + " ") || nome.startsWith(query)) {
            score += 5000;
        }
        else if (nome.contains(" " + query + " ") || nome.endsWith(" " + query)) {
            score += 3000;
        }
        else if (nome.contains(" " + query)) {
            score += 2000;
        }
        else if (nome.contains(query)) {
            score += 1000;
        }

        String[] tokens = query.split("\\s+");
        if (tokens.length > 1) {
            int tokensEncontrados = 0;
            int bonusPosicao = 0;
            for (String token : tokens) {
                if (!token.isBlank()) {
                    int pos = nome.indexOf(token);
                    if (pos >= 0) {
                        tokensEncontrados++;
                        bonusPosicao += Math.max(0, 50 - pos);
                    }
                }
            }
            if (tokensEncontrados == tokens.length) {
                score += 2500 + bonusPosicao;
            } else {
                score += (tokensEncontrados * 300);
            }
        }

        if (isUsuario) {
            score += 150;
        }

        int lengthPenalty = Math.min(nome.length(), 200);
        score -= lengthPenalty;

        return score;
    }

    public static List<IngredienteGetDTO> ordenarPorRelevancia(
            List<IngredienteGetDTO> ingredientes,
            String query,
            Long usuarioId) {

        if (ingredientes == null || ingredientes.isEmpty() || query == null || query.isBlank()) {
            return ingredientes != null ? ingredientes : Collections.emptyList();
        }

        List<IngredienteGetDTO> lista = new ArrayList<>(ingredientes);

        lista.sort((a, b) -> {
            boolean aIsUser = usuarioId != null && a != null && usuarioId.equals(a.usuarioId());
            boolean bIsUser = usuarioId != null && b != null && usuarioId.equals(b.usuarioId());

            String nomeA = (a != null && a.nome() != null) ? a.nome() : "";
            String nomeB = (b != null && b.nome() != null) ? b.nome() : "";

            int scoreA = calcularPontuacao(nomeA, query, aIsUser);
            int scoreB = calcularPontuacao(nomeB, query, bIsUser);

            if (scoreA != scoreB) {
                return Integer.compare(scoreB, scoreA);
            }

            int lenComp = Integer.compare(nomeA.length(), nomeB.length());
            if (lenComp != 0) {
                return lenComp;
            }

            return nomeA.compareToIgnoreCase(nomeB);
        });

        return lista;
    }
}
