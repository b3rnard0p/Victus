package com.example.sistemanutricao.service;

import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.record.IngredientePorFichaDTO;
import com.example.sistemanutricao.record.PerfilNutricionalDTO.PerfilNutricionalDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.service.ficha.PerfilNutricionalCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilNutricionalCalculatorTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @InjectMocks
    private PerfilNutricionalCalculator calculator;

    @Test
    void calcularPerfilNutricional_Success() {
        Ingrediente ing1 = new Ingrediente();
        ing1.setId(1L);
        ing1.setPtn(BigDecimal.valueOf(10)); // 10g/100g
        ing1.setCho(BigDecimal.valueOf(20)); // 20g/100g
        ing1.setLip(BigDecimal.valueOf(5));  // 5g/100g
        ing1.setSodio(BigDecimal.valueOf(50));
        ing1.setGorduraSaturada(BigDecimal.valueOf(2));

        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ing1));

        IngredientePorFichaDTO dto = new IngredientePorFichaDTO(
            null, 1L, null, null, null, null, null, null, BigDecimal.valueOf(200), // PL = 200g
            null, null, null, null, null, null
        );

        PerfilNutricionalDTO result = calculator.calcularPerfilNutricionalPorPorcaoCreate(List.of(dto), 2); // 2 porcoes

        assertNotNull(result);
        // Total PL = 200. PTN in 200g = 10 * 2 = 20. Por portion = 10.
        assertEquals(0, BigDecimal.valueOf(10).compareTo(result.gramasPTN()));
        // CHO in 200g = 20 * 2 = 40. Por portion = 20.
        assertEquals(0, BigDecimal.valueOf(20).compareTo(result.gramasCHO()));
        // LIP in 200g = 5 * 2 = 10. Por portion = 5.
        assertEquals(0, BigDecimal.valueOf(5).compareTo(result.gramasLIP()));
        // Sodio = 50 * 2 = 100. Por portion = 50.
        assertEquals(0, BigDecimal.valueOf(50).compareTo(result.gramasSodio()));
        // Saturada = 2 * 2 = 4. Por portion = 2.
        assertEquals(0, BigDecimal.valueOf(2).compareTo(result.gramasSaturada()));

        // Kcal:
        // PTN = 10 * 4 = 40
        // CHO = 20 * 4 = 80
        // LIP = 5 * 9 = 45
        // VTC = 40 + 80 + 45 = 165
        assertEquals(0, BigDecimal.valueOf(165).compareTo(result.vtc()));
    }

    @Test
    void calcularPerfilNutricional_NullsAndMissing() {
        Ingrediente ing1 = new Ingrediente(); // everything null
        ing1.setId(1L);
        when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ing1));

        IngredientePorFichaDTO dto1 = new IngredientePorFichaDTO(null, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null);
        IngredientePorFichaDTO dto2 = new IngredientePorFichaDTO(null, 99L, null, null, null, null, null, null, null, null, null, null, null, null, null); // not found
        IngredientePorFichaDTO dto3 = new IngredientePorFichaDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null); // null id
        
        PerfilNutricionalDTO result = calculator.calcularPerfilNutricionalPorPorcaoUpdate(Arrays.asList(dto1, dto2, dto3, null), 1);

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.vtc()));
    }
    
    @Test
    void calcularPerfilNutricional_NullList() {
        PerfilNutricionalDTO result = calculator.calcularPerfilNutricionalPorPorcaoCreate(null, 1);
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.vtc()));
    }
}
