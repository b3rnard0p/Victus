package com.example.sistemanutricao.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class IngredientesPorFicha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100, message = "A medida caseira pode ter no máximo 100 caracteres.")
    private String medidaCaseira;

    @NotNull
    @Digits(integer = 4, fraction = 2, message = "O PB deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
    private BigDecimal pb;

    @NotNull
    @Digits(integer = 4, fraction = 2, message = "O PL deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
    private BigDecimal pl;

    @NotNull
    private BigDecimal fc;

    @NotNull
    private BigDecimal custoUsado;

    @NotNull
    @Digits(integer = 4, fraction = 2, message = "O custo por kg deve ter no máximo 4 dígitos inteiros e 2 casas decimais.")
    private BigDecimal custoKG;

    private BigDecimal ptnCalculado;
    private BigDecimal choCalculado;
    private BigDecimal lipCalculado;
    private BigDecimal sodioCalculado;
    private BigDecimal gorduraSaturadaCalculada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fichaTecnica_id", nullable = false)
    private FichaTecnica fichaTecnica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_nutricional_id", nullable = false)
    private PerfilNutricional perfilNutricional;

    public IngredientesPorFicha() {
    }

    public IngredientesPorFicha(Long id, String medidaCaseria, BigDecimal pb, BigDecimal pl, BigDecimal fc,
                                BigDecimal custoUsado, BigDecimal custoKG, FichaTecnica fichaTecnica, Ingrediente ingrediente,
                                PerfilNutricional perfilNutricional) {
        this.id = id;
        this.medidaCaseira = medidaCaseria;
        this.pb = pb;
        this.pl = pl;
        this.fc = fc;
        this.custoUsado = custoUsado;
        this.custoKG = custoKG;
        this.fichaTecnica = fichaTecnica;
        this.ingrediente = ingrediente;
        this.perfilNutricional = perfilNutricional;
    }

    public IngredientesPorFicha(Long id, String medidaCaseria, BigDecimal pb, BigDecimal pl, BigDecimal fc,
                                BigDecimal custoUsado, BigDecimal custoKG, BigDecimal ptnCalculado, BigDecimal choCalculado,
                                BigDecimal lipCalculado, BigDecimal sodioCalculado, BigDecimal gorduraSaturadaCalculada,
                                FichaTecnica fichaTecnica, Ingrediente ingrediente, PerfilNutricional perfilNutricional) {
        this.id = id;
        this.medidaCaseira = medidaCaseria;
        this.pb = pb;
        this.pl = pl;
        this.fc = fc;
        this.custoUsado = custoUsado;
        this.custoKG = custoKG;
        this.ptnCalculado = ptnCalculado;
        this.choCalculado = choCalculado;
        this.lipCalculado = lipCalculado;
        this.sodioCalculado = sodioCalculado;
        this.gorduraSaturadaCalculada = gorduraSaturadaCalculada;
        this.fichaTecnica = fichaTecnica;
        this.ingrediente = ingrediente;
        this.perfilNutricional = perfilNutricional;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedidaCaseria() {
        return medidaCaseira;
    }

    public void setMedidaCaseria(String medidaCaseria) {
        this.medidaCaseira = medidaCaseria;
    }

    public BigDecimal getPb() {
        return pb;
    }

    public void setPb(BigDecimal pb) {
        this.pb = pb;
    }

    public BigDecimal getPl() {
        return pl;
    }

    public void setPl(BigDecimal pl) {
        this.pl = pl;
    }

    public BigDecimal getFc() {
        return fc;
    }

    public void setFc(BigDecimal fc) {
        this.fc = fc;
    }

    public BigDecimal getCustoUsado() {
        return custoUsado;
    }

    public void setCustoUsado(BigDecimal custoUsado) {
        this.custoUsado = custoUsado;
    }

    public BigDecimal getCustoKG() {
        return custoKG;
    }

    public void setCustoKG(BigDecimal custoKG) {
        this.custoKG = custoKG;
    }

    public BigDecimal getPtnCalculado() {
        return ptnCalculado;
    }

    public void setPtnCalculado(BigDecimal ptnCalculado) {
        this.ptnCalculado = ptnCalculado;
    }

    public BigDecimal getChoCalculado() {
        return choCalculado;
    }

    public void setChoCalculado(BigDecimal choCalculado) {
        this.choCalculado = choCalculado;
    }

    public BigDecimal getLipCalculado() {
        return lipCalculado;
    }

    public void setLipCalculado(BigDecimal lipCalculado) {
        this.lipCalculado = lipCalculado;
    }

    public BigDecimal getSodioCalculado() {
        return sodioCalculado;
    }

    public void setSodioCalculado(BigDecimal sodioCalculado) {
        this.sodioCalculado = sodioCalculado;
    }

    public BigDecimal getGorduraSaturadaCalculada() {
        return gorduraSaturadaCalculada;
    }

    public void setGorduraSaturadaCalculada(BigDecimal gorduraSaturadaCalculada) {
        this.gorduraSaturadaCalculada = gorduraSaturadaCalculada;
    }

    public FichaTecnica getFichaTecnica() {
        return fichaTecnica;
    }

    public void setFichaTecnica(FichaTecnica fichaTecnica) {
        this.fichaTecnica = fichaTecnica;
    }

    public Ingrediente getIngrediente() {
        return ingrediente;
    }

    public void setIngrediente(Ingrediente ingrediente) {
        this.ingrediente = ingrediente;
    }

    public PerfilNutricional getPerfilNutricional() {
        return perfilNutricional;
    }

    public void setPerfilNutricional(PerfilNutricional perfilNutricional) {
        this.perfilNutricional = perfilNutricional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientesPorFicha that = (IngredientesPorFicha) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "IngredientesPorFicha{" +
                "id=" + id +
                ", medidaCaseira='" + medidaCaseira + '\'' +
                ", custoUsado=" + custoUsado +
                '}';
    }
}
