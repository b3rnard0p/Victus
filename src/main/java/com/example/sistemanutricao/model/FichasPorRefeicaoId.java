package com.example.sistemanutricao.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FichasPorRefeicaoId implements Serializable {

    private Long refeicaoId;
    private Long fichaTecnicaId;


    public FichasPorRefeicaoId() {
    }

    public FichasPorRefeicaoId(Long refeicaoId, Long fichaTecnicaId) {
        this.refeicaoId = refeicaoId;
        this.fichaTecnicaId = fichaTecnicaId;
    }

    public Long getRefeicaoId() {
        return refeicaoId;
    }

    public void setRefeicaoId(Long refeicaoId) {
        this.refeicaoId = refeicaoId;
    }

    public Long getFichaTecnicaId() {
        return fichaTecnicaId;
    }

    public void setFichaTecnicaId(Long fichaTecnicaId) {
        this.fichaTecnicaId = fichaTecnicaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FichasPorRefeicaoId that = (FichasPorRefeicaoId) o;
        return Objects.equals(refeicaoId, that.refeicaoId) &&
                Objects.equals(fichaTecnicaId, that.fichaTecnicaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refeicaoId, fichaTecnicaId);
    }

    @Override
    public String toString() {
        return "FichasPorRefeicaoId{" +
                "refeicaoId=" + refeicaoId +
                ", fichaTecnicaId=" + fichaTecnicaId +
                '}';
    }
}