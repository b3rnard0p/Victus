package com.example.sistemanutricao.model;
import com.example.sistemanutricao.model.enuns.StatusCriacao;
import com.example.sistemanutricao.model.enuns.Status;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@NamedEntityGraph(
        name = "Ficha.completa",
        attributeNodes = {
                @NamedAttributeNode("nutricionista"),
                @NamedAttributeNode("preparacao"),
                @NamedAttributeNode("perfilNutricional"),
                @NamedAttributeNode(value = "ingredientesPorFicha", subgraph = "ingredientes-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "ingredientes-subgraph",
                        attributeNodes = { @NamedAttributeNode("ingrediente") }
                )
        }
)
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private BigDecimal custoPerCapita;

    @NotNull
    private BigDecimal custoTotal;

    @NotNull

    private Integer numeroPorcoes;

    @NotNull
    private BigDecimal pesoPorcao;

    @NotNull
    private String medidaCaseira;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private StatusCriacao statusCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id")
    private Usuario nutricionista;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "preparacao_id", nullable = false)
    private Preparacao preparacao;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_nutricional_id", nullable = false)
    private PerfilNutricional perfilNutricional;

    @OneToMany(mappedBy = "fichaTecnica", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngredientesPorFicha> ingredientesPorFicha;

    public FichaTecnica() {
    }

    public FichaTecnica(Long id, BigDecimal custoPerCapita, BigDecimal custoTotal, Integer numeroPorcoes, BigDecimal pesoPorcao,
                        String medidaCaseira, Status status, StatusCriacao statusCriacao, Usuario nutricionista, Preparacao preparacao,
                        PerfilNutricional perfilNutricional, List<IngredientesPorFicha> ingredientesPorFicha) {
        this.id = id;
        this.custoPerCapita = custoPerCapita;
        this.custoTotal = custoTotal;
        this.numeroPorcoes = numeroPorcoes;
        this.pesoPorcao = pesoPorcao;
        this.medidaCaseira = medidaCaseira;
        this.status = status;
        this.statusCriacao = statusCriacao;
        this.nutricionista = nutricionista;
        this.preparacao = preparacao;
        this.perfilNutricional = perfilNutricional;
        this.ingredientesPorFicha = ingredientesPorFicha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCustoPerCapita() {
        return custoPerCapita;
    }

    public void setCustoPerCapita(BigDecimal custoPerCapita) {
        this.custoPerCapita = custoPerCapita;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public Integer getNumeroPorcoes() {
        return numeroPorcoes;
    }

    public void setNumeroPorcoes(Integer numeroPorcoes) {
        this.numeroPorcoes = numeroPorcoes;
    }

    public BigDecimal getPesoPorcao() {
        return pesoPorcao;
    }

    public void setPesoPorcao(BigDecimal pesoPorcao) {
        this.pesoPorcao = pesoPorcao;
    }

    public String getMedidaCaseira() {
        return medidaCaseira;
    }

    public void setMedidaCaseira(String medidaCaseira) {
        this.medidaCaseira = medidaCaseira;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public StatusCriacao getStatusCriacao() {
        return statusCriacao;
    }

    public void setStatusCriacao(StatusCriacao statusCriacao) {
        this.statusCriacao = statusCriacao;
    }

    public Usuario getNutricionista() {
        return nutricionista;
    }

    public void setNutricionista(Usuario nutricionista) {
        this.nutricionista = nutricionista;
    }

    public Preparacao getPreparacao() {
        return preparacao;
    }

    public void setPreparacao(Preparacao preparacao) {
        this.preparacao = preparacao;
    }

    public PerfilNutricional getPerfilNutricional() {
        return perfilNutricional;
    }

    public void setPerfilNutricional(PerfilNutricional perfilNutricional) {
        this.perfilNutricional = perfilNutricional;
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
        FichaTecnica that = (FichaTecnica) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FichaTecnica{" +
                "id=" + id +
                ", custoPerCapita=" + custoPerCapita +
                ", custoTotal=" + custoTotal +
                ", numeroPorcoes=" + numeroPorcoes +
                ", pesoPorcao=" + pesoPorcao +
                ", medidaCaseira='" + medidaCaseira + '\'' +
                ", status=" + status +
                ", statusCriacao=" + statusCriacao +
                ", nutricionista=" + (nutricionista != null ? nutricionista.getId() : "null") +
                ", preparacao=" + (preparacao != null ? preparacao.getId() : "null") +
                ", perfilNutricional=" + (perfilNutricional != null ? perfilNutricional.getId() : "null") +
                ", ingredientesPorFicha=" + (ingredientesPorFicha != null ? ingredientesPorFicha.size() + " items" : "null") +
                '}';
    }
}
