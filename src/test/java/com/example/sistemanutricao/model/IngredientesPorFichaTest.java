package com.example.sistemanutricao.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class IngredientesPorFichaTest {

    @Test
    void testGettersAndSetters() {
        IngredientesPorFicha ipf = new IngredientesPorFicha();
        ipf.setId(1L);
        ipf.setMedidaCaseria("1x");
        ipf.setPb(BigDecimal.ONE);
        ipf.setPl(BigDecimal.TEN);
        ipf.setFc(BigDecimal.ZERO);
        ipf.setCustoUsado(new BigDecimal("5.0"));
        ipf.setCustoKG(new BigDecimal("10.0"));
        
        ipf.setPtnCalculado(new BigDecimal("1.0"));
        ipf.setChoCalculado(new BigDecimal("2.0"));
        ipf.setLipCalculado(new BigDecimal("3.0"));
        ipf.setSodioCalculado(new BigDecimal("4.0"));
        ipf.setGorduraSaturadaCalculada(new BigDecimal("5.0"));
        
        FichaTecnica ft = new FichaTecnica();
        ft.setId(2L);
        ipf.setFichaTecnica(ft);
        
        Ingrediente ing = new Ingrediente();
        ing.setId(3L);
        ipf.setIngrediente(ing);
        
        PerfilNutricional pn = new PerfilNutricional();
        pn.setId(4L);
        ipf.setPerfilNutricional(pn);

        assertThat(ipf.getId()).isEqualTo(1L);
        assertThat(ipf.getMedidaCaseria()).isEqualTo("1x");
        assertThat(ipf.getPb()).isEqualTo(BigDecimal.ONE);
        assertThat(ipf.getPl()).isEqualTo(BigDecimal.TEN);
        assertThat(ipf.getFc()).isEqualTo(BigDecimal.ZERO);
        assertThat(ipf.getCustoUsado()).isEqualTo(new BigDecimal("5.0"));
        assertThat(ipf.getCustoKG()).isEqualTo(new BigDecimal("10.0"));
        
        assertThat(ipf.getPtnCalculado()).isEqualTo(new BigDecimal("1.0"));
        assertThat(ipf.getChoCalculado()).isEqualTo(new BigDecimal("2.0"));
        assertThat(ipf.getLipCalculado()).isEqualTo(new BigDecimal("3.0"));
        assertThat(ipf.getSodioCalculado()).isEqualTo(new BigDecimal("4.0"));
        assertThat(ipf.getGorduraSaturadaCalculada()).isEqualTo(new BigDecimal("5.0"));
        
        assertThat(ipf.getFichaTecnica()).isEqualTo(ft);
        assertThat(ipf.getIngrediente()).isEqualTo(ing);
        assertThat(ipf.getPerfilNutricional()).isEqualTo(pn);
        
        assertThat(ipf.toString()).contains("id=1", "medidaCaseira='1x'", "custoUsado=5.0");
    }

    @Test
    void testConstructors() {
        FichaTecnica ft = new FichaTecnica();
        Ingrediente ing = new Ingrediente();
        PerfilNutricional pn = new PerfilNutricional();
        
        IngredientesPorFicha ipf1 = new IngredientesPorFicha(
            1L, "1x", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 
            new BigDecimal("5.0"), new BigDecimal("10.0"), 
            ft, ing, pn
        );
        assertThat(ipf1.getId()).isEqualTo(1L);

        IngredientesPorFicha ipf2 = new IngredientesPorFicha(
            2L, "2x", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, 
            new BigDecimal("5.0"), new BigDecimal("10.0"), 
            new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0"), new BigDecimal("4.0"), new BigDecimal("5.0"),
            ft, ing, pn
        );
        assertThat(ipf2.getId()).isEqualTo(2L);
        assertThat(ipf2.getPtnCalculado()).isEqualTo(new BigDecimal("1.0"));
    }

    @Test
    void testEqualsAndHashCode() {
        IngredientesPorFicha ipf1 = new IngredientesPorFicha();
        ipf1.setId(1L);
        
        IngredientesPorFicha ipf2 = new IngredientesPorFicha();
        ipf2.setId(1L);
        
        IngredientesPorFicha ipf3 = new IngredientesPorFicha();
        ipf3.setId(2L);
        
        assertThat(ipf1).isEqualTo(ipf2)
                        .isNotEqualTo(ipf3)
                        .isNotNull()
                        .isNotEqualTo(new Object())
                        .hasSameHashCodeAs(ipf2);
    }
}
