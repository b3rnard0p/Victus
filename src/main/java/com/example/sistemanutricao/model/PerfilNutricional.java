package com.example.sistemanutricao.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;


@Entity
public class PerfilNutricional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal vtc;

    private BigDecimal kcalPTN;

    private BigDecimal kcalCHO;

    private BigDecimal kcalLIP;

    private BigDecimal gramasPTN;

    private BigDecimal gramasCHO;

    private BigDecimal gramasLIP;

    private BigDecimal gramasSodio;

    private BigDecimal gramasSaturada;

    private BigDecimal porcentPTN;

    private BigDecimal porcentCHO;

    private BigDecimal porcentLIP;

    @OneToMany(mappedBy = "perfilNutricional", cascade = CascadeType.ALL)
    private List<IngredientesPorFicha> ingredientesPorFicha;

    public PerfilNutricional() {
    }

    public PerfilNutricional(Long id) {
        this.id = id;
    }

    public PerfilNutricional(Long id, BigDecimal vtc, BigDecimal kcalPTN, BigDecimal kcalCHO, BigDecimal kcalLIP, BigDecimal gramasPTN,
                             BigDecimal gramasCHO, BigDecimal gramasLIP, BigDecimal gramasSodio, BigDecimal gramasSaturada,
                             BigDecimal porcentPTN, BigDecimal porcentCHO, BigDecimal porcentLIP, List<IngredientesPorFicha> ingredientesPorFicha) {
        this.id = id;
        this.vtc = vtc;
        this.kcalPTN = kcalPTN;
        this.kcalCHO = kcalCHO;
        this.kcalLIP = kcalLIP;
        this.gramasPTN = gramasPTN;
        this.gramasCHO = gramasCHO;
        this.gramasLIP = gramasLIP;
        this.gramasSodio = gramasSodio;
        this.gramasSaturada = gramasSaturada;
        this.porcentPTN = porcentPTN;
        this.porcentCHO = porcentCHO;
        this.porcentLIP = porcentLIP;
        this.ingredientesPorFicha = ingredientesPorFicha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getVtc() {
        return vtc;
    }

    public void setVtc(BigDecimal vtc) {
        this.vtc = vtc;
    }

    public BigDecimal getKcalPTN() {
        return kcalPTN;
    }

    public void setKcalPTN(BigDecimal kcalPTN) {
        this.kcalPTN = kcalPTN;
    }

    public BigDecimal getKcalCHO() {
        return kcalCHO;
    }

    public void setKcalCHO(BigDecimal kcalCHO) {
        this.kcalCHO = kcalCHO;
    }

    public BigDecimal getKcalLIP() {
        return kcalLIP;
    }

    public void setKcalLIP(BigDecimal kcalLIP) {
        this.kcalLIP = kcalLIP;
    }

    public BigDecimal getGramasPTN() {
        return gramasPTN;
    }

    public void setGramasPTN(BigDecimal gramasPTN) {
        this.gramasPTN = gramasPTN;
    }

    public BigDecimal getGramasCHO() {
        return gramasCHO;
    }

    public void setGramasCHO(BigDecimal gramasCHO) {
        this.gramasCHO = gramasCHO;
    }

    public BigDecimal getGramasLIP() {
        return gramasLIP;
    }

    public void setGramasLIP(BigDecimal gramasLIP) {
        this.gramasLIP = gramasLIP;
    }

    public BigDecimal getGramasSodio() {
        return gramasSodio;
    }

    public void setGramasSodio(BigDecimal gramasSodio) {
        this.gramasSodio = gramasSodio;
    }

    public BigDecimal getGramasSaturada() {
        return gramasSaturada;
    }

    public void setGramasSaturada(BigDecimal gramasSaturada) {
        this.gramasSaturada = gramasSaturada;
    }

    public BigDecimal getPorcentPTN() {
        return porcentPTN;
    }

    public void setPorcentPTN(BigDecimal porcentPTN) {
        this.porcentPTN = porcentPTN;
    }

    public BigDecimal getPorcentCHO() {
        return porcentCHO;
    }

    public void setPorcentCHO(BigDecimal porcentCHO) {
        this.porcentCHO = porcentCHO;
    }

    public BigDecimal getPorcentLIP() {
        return porcentLIP;
    }

    public void setPorcentLIP(BigDecimal porcentLIP) {
        this.porcentLIP = porcentLIP;
    }

    public List<IngredientesPorFicha> getIngredientesPorFicha() {
        return ingredientesPorFicha;
    }

    public void setIngredientesPorFicha(List<IngredientesPorFicha> ingredientesPorFicha) {
        this.ingredientesPorFicha = ingredientesPorFicha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfilNutricional that = (PerfilNutricional) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PerfilNutricional{" +
                "id=" + id +
                ", vtc=" + vtc +
                ", kcalPTN=" + kcalPTN +
                ", kcalCHO=" + kcalCHO +
                ", kcalLIP=" + kcalLIP +
                '}';
    }
}
