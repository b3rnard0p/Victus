package com.example.sistemanutricao.service.log;

import com.example.sistemanutricao.model.AtividadeLog;
import com.example.sistemanutricao.repository.AtividadeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeLogServiceTest {

    @Mock
    private AtividadeLogRepository atividadeLogRepository;

    @InjectMocks
    private AtividadeLogService atividadeLogService;

    @Test
    void buscarAtividadesPorUsuario_DeveRetornarPaginaDeLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        AtividadeLog log = new AtividadeLog();
        Page<AtividadeLog> page = new PageImpl<>(List.of(log));
        
        when(atividadeLogRepository.findByUsuarioIdOrderByDataHoraDesc(1L, pageable)).thenReturn(page);
        
        Page<AtividadeLog> result = atividadeLogService.buscarAtividadesPorUsuario(1L, pageable);
        
        assertEquals(1, result.getTotalElements());
        verify(atividadeLogRepository).findByUsuarioIdOrderByDataHoraDesc(1L, pageable);
    }
}
