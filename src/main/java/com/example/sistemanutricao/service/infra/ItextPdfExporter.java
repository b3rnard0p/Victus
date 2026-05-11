package com.example.sistemanutricao.service.infra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;
import com.example.sistemanutricao.service.port.PdfExporter;
import com.itextpdf.html2pdf.HtmlConverter;

@Service
public class ItextPdfExporter implements PdfExporter {

    private final TemplateEngine templateEngine;

    public ItextPdfExporter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public byte[] generateFichaTecnicaPdf(FichaTecnicaGetDTO ficha) {
        try {
            Context context = new Context();
            context.setVariable("ficha", ficha);

            String htmlContent = templateEngine.process("components/FichaTecnica", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            HtmlConverter.convertToPdf(htmlContent, outputStream);

            byte[] pdfBytes = outputStream.toByteArray();
            outputStream.close();

            return pdfBytes;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
} 