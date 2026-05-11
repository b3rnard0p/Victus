package com.example.sistemanutricao.service.taco;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TacoExcelParserTest {

    private TacoExcelParser parser;

    @BeforeEach
    void setUp() {
        parser = new TacoExcelParser();
    }

    @Test
    void testNormalizarNome() {
        assertThat(parser.normalizarNome("  Maçã   Fuji   ")).isEqualTo("maca fuji");
        assertThat(parser.normalizarNome("Café (infusão)")).isEqualTo("cafe infusao");
        assertThat(parser.normalizarNome("Açúcar, cristal")).isEqualTo("acucar cristal");
        assertThat(parser.normalizarNome("")).isEqualTo("");
        assertThat(parser.normalizarNome(null)).isEqualTo("");
    }

    @Test
    void testLerPlanilhaTaco() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Taco");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Descrição do Alimento");
        header.createCell(1).setCellValue("Proteína(g)");
        header.createCell(2).setCellValue("Carboidrato(g)");
        header.createCell(3).setCellValue("Lipídeos(g)");
        header.createCell(4).setCellValue("Sódio(mg)");

        // Linha de dados 1
        Row data1 = sheet.createRow(1);
        data1.createCell(0).setCellValue("Arroz, integral, cozido");
        data1.createCell(1).setCellValue("2,6");
        data1.createCell(2).setCellValue("25,8");
        data1.createCell(3).setCellValue("1,0");
        data1.createCell(4).setCellValue("1");

        // Linha de dados 2 (com valores vazios e "Tr" / "NA")
        Row data2 = sheet.createRow(2);
        data2.createCell(0).setCellValue("Feijão");
        data2.createCell(1).setCellValue("Tr");
        data2.createCell(2).setCellValue("NA");
        data2.createCell(3).setCellValue("-");
        data2.createCell(4).setCellValue("15.5");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        List<TacoIngredienteLinha> linhas = parser.lerPlanilhaTaco(in);

        assertThat(linhas).hasSize(2);

        TacoIngredienteLinha linha1 = linhas.get(0);
        assertThat(linha1.nome()).isEqualTo("Arroz, integral, cozido");
        assertThat(linha1.ptn()).isEqualTo(new BigDecimal("2.6"));
        assertThat(linha1.cho()).isEqualTo(new BigDecimal("25.8"));
        assertThat(linha1.lip()).isEqualTo(new BigDecimal("1.0"));
        assertThat(linha1.sodio()).isEqualTo(new BigDecimal("1"));

        TacoIngredienteLinha linha2 = linhas.get(1);
        assertThat(linha2.nome()).isEqualTo("Feijão");
        assertThat(linha2.ptn()).isNull();
        assertThat(linha2.cho()).isNull();
        assertThat(linha2.lip()).isNull();
        assertThat(linha2.sodio()).isEqualTo(new BigDecimal("15.5"));
    }

    @Test
    void testLerPlanilhaAcidos() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Acidos");

        // Cabeçalhos (podem estar em qualquer lugar na Acidos)
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Descricao dos alimentos");
        header.createCell(1).setCellValue("Saturados");

        Row data1 = sheet.createRow(1);
        data1.createCell(0).setCellValue("Manteiga");
        data1.createCell(1).setCellValue("50,5");

        Row data2 = sheet.createRow(2);
        data2.createCell(0).setCellValue("Oleo de Soja");
        data2.createCell(1).setCellValue("15,2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Map<String, BigDecimal> acidos = parser.lerPlanilhaAcidos(in);

        assertThat(acidos).hasSize(2);
        assertThat(acidos.get("manteiga")).isEqualTo(new BigDecimal("50.5"));
        assertThat(acidos.get("oleo de soja")).isEqualTo(new BigDecimal("15.2"));
    }
}
