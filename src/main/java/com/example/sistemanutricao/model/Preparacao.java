package com.example.sistemanutricao.model;
import com.example.sistemanutricao.model.enuns.Categoria;

import jakarta.persistence.*;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Preparacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Categoria categoria;

    @NotNull
    private Integer numero;

    @NotBlank
    private String tempoPreparo;

    @Column(length = 1000)
    @NotBlank
    private String equipamentos;

    @Column(length = 1000)
    @NotBlank
    private String modoPreparo;

    private BigDecimal qntdAgua;

    private BigDecimal porcentAgua;

    @NotNull
    private BigDecimal fcc;

    @NotNull
    private BigDecimal rendimento;

    public Preparacao() {
    }

    public Preparacao(Long id) {
        this.id = id;
    }

    public Preparacao(Long id, String nome, Integer numero, String tempoPreparo, String equipamentos,
                      String modoPreparo, BigDecimal porcentAgua, BigDecimal qntdAgua,
                      BigDecimal fcc, BigDecimal rendimento, Categoria categoria) {
        this.id = id;
        this.nome = nome;
        this.numero = numero;
        this.tempoPreparo = tempoPreparo;
        this.equipamentos = equipamentos;
        this.modoPreparo = modoPreparo;
        this.porcentAgua = porcentAgua;
        this.qntdAgua = qntdAgua;
        this.fcc = fcc;
        this.rendimento = rendimento;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer  getNumero() {
        return numero;
    }

    public void setNumero(Integer  numero) {
        this.numero = numero;
    }

    public String getTempoPreparo() {
        return tempoPreparo;
    }

    public void setTempoPreparo(String tempoPreparo) {
        this.tempoPreparo = tempoPreparo;
    }

    public String getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(String equipamentos) {
        this.equipamentos = equipamentos;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public BigDecimal getQntdAgua() {
        return qntdAgua;
    }

    public void setQntdAgua(BigDecimal qntdAgua) {
        this.qntdAgua = qntdAgua;
    }

    public BigDecimal getPorcentAgua() {
        return porcentAgua;
    }

    public void setPorcentAgua(BigDecimal porcentAgua) {
        this.porcentAgua = porcentAgua;
    }

    public BigDecimal getFcc() {
        return fcc;
    }

    public void setFcc(BigDecimal fcc) {
        this.fcc = fcc;
    }

    public BigDecimal getRendimento() {
        return rendimento;
    }

    public void setRendimento(BigDecimal rendimento) {
        this.rendimento = rendimento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Preparacao that = (Preparacao) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Preparacao{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", categoria=" + categoria +
                ", numero=" + numero +
                '}';
    }
}
