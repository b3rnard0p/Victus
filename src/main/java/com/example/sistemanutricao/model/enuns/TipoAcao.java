package com.example.sistemanutricao.model.enuns;

public enum TipoAcao {
    CRIOU_FICHA("Criou uma ficha"),
    EDITOU_FICHA("Editou uma ficha"),
    ARQUIVOU_FICHA("Arquivou uma ficha"),
    
    CRIOU_REFEICAO("Criou uma refeição"),
    EDITOU_REFEICAO("Editou uma refeição"),
    ARQUIVOU_REFEICAO("Arquivou uma refeição"),
    
    CRIOU_INGREDIENTE("Criou um ingrediente"),
    EDITOU_INGREDIENTE("Editou um ingrediente"),
    ARQUIVOU_INGREDIENTE("Arquivou um ingrediente");

    private final String descricao;

    TipoAcao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
