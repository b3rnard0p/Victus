package com.example.sistemanutricao.service.port;

import com.example.sistemanutricao.record.FichaTecnicaDTO.FichaTecnicaGetDTO;

public interface PdfExporter {
    byte[] generateFichaTecnicaPdf(FichaTecnicaGetDTO ficha);
}
