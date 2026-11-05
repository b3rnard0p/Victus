package com.example.sistemanutricao.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RateLimitService {

    private static final int MAX_TENTATIVAS = 10;
    private static final long JANELA_MS = 60_000L;

    private final Map<String, Deque<Long>> tentativasPorChave = new ConcurrentHashMap<>();

    public void verificarLimite(String chave) {
        if (chave == null || chave.isBlank()) {
            return;
        }

        long agora = System.currentTimeMillis();
        Deque<Long> fila = tentativasPorChave.computeIfAbsent(chave, k -> new ArrayDeque<>());

        synchronized (fila) {
            while (!fila.isEmpty() && (agora - fila.peekFirst()) > JANELA_MS) {
                fila.pollFirst();
            }

            if (fila.size() >= MAX_TENTATIVAS) {
                throw new IllegalArgumentException("Muitas tentativas realizadas. Aguarde um minuto e tente novamente.");
            }

            fila.addLast(agora);
        }
    }
}

