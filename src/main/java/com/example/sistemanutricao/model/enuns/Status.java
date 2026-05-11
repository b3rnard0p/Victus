package com.example.sistemanutricao.model.enuns;

public enum Status {
    ATIVA("Ativa"),
    INATIVA("Inativa");

    private final String nome;

    Status(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
