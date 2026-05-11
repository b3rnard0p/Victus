package com.example.sistemanutricao.model;
import com.example.sistemanutricao.model.enuns.Status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@NamedEntityGraph(
        name = "Refeicao.completa",
        attributeNodes = {
                @NamedAttributeNode("nutricionista"),
        @NamedAttributeNode(value = "fichasPorRefeicao", subgraph = "fichas-subgraph")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "fichas-subgraph",
            attributeNodes = {
                @NamedAttributeNode(value = "fichaTecnica", subgraph = "ficha-tecnica-subgraph")
            }
        ),
        @NamedSubgraph(
            name = "ficha-tecnica-subgraph",
            attributeNodes = {
                @NamedAttributeNode("preparacao"),
                @NamedAttributeNode("perfilNutricional")
            }
        )
        }
)
public class Refeicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome não pode estar em branco")
    @Column(nullable = false)
    private String nome;

    private String kcalTotal;

    private BigDecimal totalGramasPTN;
    private BigDecimal totalGramasCHO;
    private BigDecimal totalGramasLIP;
    private BigDecimal totalGramasSodio;
    private BigDecimal totalGramasSaturada;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutricionista_id")
    private Usuario nutricionista;

    @OneToMany(mappedBy = "refeicao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichasPorRefeicao> fichasPorRefeicao = new ArrayList<>();

    public Refeicao() {
    }

    public Refeicao(Long id, String nome, String kcalTotal, Status status, Usuario nutricionista, List<FichasPorRefeicao> fichasPorRefeicao) {
        this.id = id;
        this.nome = nome;
        this.kcalTotal = kcalTotal;
        this.status = status;
        this.nutricionista = nutricionista;
        this.fichasPorRefeicao = fichasPorRefeicao;
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

    public String getKcalTotal() {
        return kcalTotal;
    }

    public void setKcalTotal(String kcalTotal) {
        this.kcalTotal = kcalTotal;
    }

    public BigDecimal getTotalGramasPTN() {
        return totalGramasPTN;
    }

    public void setTotalGramasPTN(BigDecimal totalGramasPTN) {
        this.totalGramasPTN = totalGramasPTN;
    }

    public BigDecimal getTotalGramasCHO() {
        return totalGramasCHO;
    }

    public void setTotalGramasCHO(BigDecimal totalGramasCHO) {
        this.totalGramasCHO = totalGramasCHO;
    }

    public BigDecimal getTotalGramasLIP() {
        return totalGramasLIP;
    }

    public void setTotalGramasLIP(BigDecimal totalGramasLIP) {
        this.totalGramasLIP = totalGramasLIP;
    }

    public BigDecimal getTotalGramasSodio() {
        return totalGramasSodio;
    }

    public void setTotalGramasSodio(BigDecimal totalGramasSodio) {
        this.totalGramasSodio = totalGramasSodio;
    }

    public BigDecimal getTotalGramasSaturada() {
        return totalGramasSaturada;
    }

    public void setTotalGramasSaturada(BigDecimal totalGramasSaturada) {
        this.totalGramasSaturada = totalGramasSaturada;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Usuario getNutricionista() {
        return nutricionista;
    }

    public void setNutricionista(Usuario nutricionista) {
        this.nutricionista = nutricionista;
    }

    public List<FichasPorRefeicao> getFichasPorRefeicao() {
        return fichasPorRefeicao;
    }

    public void setFichasPorRefeicao(List<FichasPorRefeicao> fichasPorRefeicao) {
        this.fichasPorRefeicao = fichasPorRefeicao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refeicao refeicao = (Refeicao) o;
        return Objects.equals(id, refeicao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Refeicao{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", kcalTotal='" + kcalTotal + '\'' +
                ", status=" + status +
                ", nutricionista=" + (nutricionista != null ? nutricionista.getId() : "null") +
                ", fichasPorRefeicao=" + (fichasPorRefeicao != null ? fichasPorRefeicao.size() + " items" : "null") +
                ", totalGramasPTN=" + totalGramasPTN +
                ", totalGramasCHO=" + totalGramasCHO +
                ", totalGramasLIP=" + totalGramasLIP +
                '}';
    }
}
