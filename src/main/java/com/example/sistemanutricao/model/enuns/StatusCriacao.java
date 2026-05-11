package com.example.sistemanutricao.model.enuns;

public enum StatusCriacao {
    INCOMPLETA("Incompleta"),
    COMPLETA("Completa");

    private final String nome;

    StatusCriacao(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
