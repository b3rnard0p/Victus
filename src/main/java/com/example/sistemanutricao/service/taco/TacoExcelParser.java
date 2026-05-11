package com.example.sistemanutricao.service.taco;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class TacoExcelParser {

    public List<TacoIngredienteLinha> lerPlanilhaTaco(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return List.of();
            }

            int colunaDescricao = localizarColuna(headerRow, formatter, "Descrição do Alimento");
            int colunaPtn = localizarColuna(headerRow, formatter, "Proteína(g)");
            int colunaCho = localizarColuna(headerRow, formatter, "Carboidrato(g)");
            int colunaLip = localizarColuna(headerRow, formatter, "Lipídeos(g)");
            int colunaSodio = localizarColuna(headerRow, formatter, "Sódio(mg)");

            List<TacoIngredienteLinha> linhas = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String nome = lerCelulaComoTexto(row, colunaDescricao, formatter)
                        .map(String::trim)
                        .filter(n -> !n.isBlank())
                        .orElse(null);
                if (nome == null) {
                    continue;
                }

                linhas.add(new TacoIngredienteLinha(
                        nome,
                        lerCelulaComoBigDecimal(row, colunaPtn, formatter).orElse(null),
                        lerCelulaComoBigDecimal(row, colunaCho, formatter).orElse(null),
                        lerCelulaComoBigDecimal(row, colunaLip, formatter).orElse(null),
                        lerCelulaComoBigDecimal(row, colunaSodio, formatter).orElse(null)
                ));
            }
            return linhas;
        }
    }

    public Map<String, BigDecimal> lerPlanilhaAcidos(InputStream inputStream) throws Exception {
        Map<String, BigDecimal> saturadasPorNome = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                int colunaDescricao = -1;
                int colunaSaturados = -1;

                for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        continue;
                    }

                    int novaColunaDescricao = encontrarColunaPorConteudo(row, formatter, "descricao dos alimentos");
                    int novaColunaSaturados = encontrarColunaPorConteudo(row, formatter, "saturados");

                    if (novaColunaDescricao >= 0 && novaColunaSaturados >= 0) {
                        colunaDescricao = novaColunaDescricao;
                        colunaSaturados = novaColunaSaturados;
                        continue;
                    }

                    if (colunaDescricao < 0 || colunaSaturados < 0) {
                        continue;
                    }

                    String nome = lerCelulaComoTexto(row, colunaDescricao, formatter)
                            .map(String::trim)
                            .filter(n -> !n.isBlank())
                            .orElse(null);
                    if (nome == null) {
                        continue;
                    }

                    String nomeNormalizado = normalizarNome(nome);
                    if (nomeNormalizado.isEmpty()) {
                        continue;
                    }

                    lerCelulaComoBigDecimal(row, colunaSaturados, formatter)
                            .ifPresent(saturada -> saturadasPorNome.putIfAbsent(nomeNormalizado, saturada));
                }
            }
        }

        return saturadasPorNome;
    }

    public String normalizarNome(String nome) {
        String base = normalizarTextoLivre(nome);
        if (base.isBlank()) {
            return "";
        }
        return base.replaceAll("\\s+", " ").trim();
    }

    private int encontrarColunaPorConteudo(Row row, DataFormatter formatter, String trechoEsperado) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Optional<String> valorOpt = lerCelulaComoTexto(row, i, formatter);
            if (valorOpt.isPresent()) {
                String valorNormalizado = normalizarTextoLivre(valorOpt.get());
                if (!valorNormalizado.isBlank() && valorNormalizado.contains(trechoEsperado)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int localizarColuna(Row headerRow, DataFormatter formatter, String nomeColuna) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Optional<String> valorOpt = lerCelulaComoTexto(headerRow, i, formatter);
            if (valorOpt.isPresent() && valorOpt.get().equalsIgnoreCase(nomeColuna)) {
                return i;
            }
        }
        throw new IllegalStateException("Coluna não encontrada na planilha TACO: " + nomeColuna);
    }

    private Optional<String> lerCelulaComoTexto(Row row, int indiceColuna, DataFormatter formatter) {
        if (indiceColuna < 0) {
            return Optional.empty();
        }
        Cell cell = row.getCell(indiceColuna);
        if (cell == null) {
            return Optional.empty();
        }
        String valor = formatter.formatCellValue(cell);
        return Optional.ofNullable(valor).map(String::trim);
    }

    private Optional<BigDecimal> lerCelulaComoBigDecimal(Row row, int indiceColuna, DataFormatter formatter) {
        Optional<String> valorOpt = lerCelulaComoTexto(row, indiceColuna, formatter);
        if (valorOpt.isEmpty()) {
            return Optional.empty();
        }

        String valor = valorOpt.get();
        String normalizado = valor.replace("NA", "").replace("Tr", "").trim();
        if (normalizado.isBlank()) {
            return Optional.empty();
        }

        normalizado = normalizado.replace(",", ".");
        if (normalizado.equals("-")) {
            return Optional.empty();
        }

        try {
            return Optional.of(new BigDecimal(normalizado));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private String normalizarTextoLivre(String valor) {
        if (valor == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
