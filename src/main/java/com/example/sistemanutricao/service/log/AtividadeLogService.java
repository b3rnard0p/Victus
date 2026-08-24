package com.example.sistemanutricao.service.log;

import com.example.sistemanutricao.model.AtividadeLog;
import com.example.sistemanutricao.repository.AtividadeLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AtividadeLogService {

    private final AtividadeLogRepository atividadeLogRepository;

    public AtividadeLogService(AtividadeLogRepository atividadeLogRepository) {
        this.atividadeLogRepository = atividadeLogRepository;
    }

    public Page<AtividadeLog> buscarAtividadesPorUsuario(Long usuarioId, Pageable pageable) {
        return atividadeLogRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId, pageable);
    }
}
