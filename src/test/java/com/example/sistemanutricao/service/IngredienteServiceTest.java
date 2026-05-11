package com.example.sistemanutricao.service;

import com.example.sistemanutricao.exception.IngredienteNotFoundException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sistemanutricao.mapper.IngredienteMapper;
import com.example.sistemanutricao.model.Ingrediente;
import com.example.sistemanutricao.model.enuns.Status;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteDTO;
import com.example.sistemanutricao.record.IngredienteDTO.IngredienteGetDTO;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.ingrediente.IngredienteService;

import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private IngredienteService ingredienteService;

    @BeforeEach
    void setUp() {
        ingredienteService = new IngredienteService(
                ingredienteRepository,
                usuarioRepository,
                Mappers.getMapper(IngredienteMapper.class)
        );
        ReflectionTestUtils.setField(ingredienteService, "tacoUsername", "TACO");
    }


    @Test
    void shouldCreateIngredienteWhenDataIsValid() {
        Usuario usuario = criarUsuario(5L);
        IngredienteDTO dto = new IngredienteDTO(
                null,
                "Arroz",
                new BigDecimal("7.00"),
                new BigDecimal("80.00"),
                new BigDecimal("1.00"),
                new BigDecimal("5.00"),
                Status.ATIVA,
                new BigDecimal("0.20"),
                5L
        );


        when(ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatus("Arroz", 5L, Status.ATIVA))
                .thenReturn(false);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(Optional.of(criarUsuario(99L)));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(ingredienteRepository.save(any(Ingrediente.class))).thenAnswer(invocation -> {
            Ingrediente ingrediente = invocation.getArgument(0);
            ingrediente.setId(10L);
            return ingrediente;
        });

        IngredienteGetDTO criado = ingredienteService.create(dto, 5L);

        assertThat(criado.id()).isEqualTo(10L);
        assertThat(criado.nome()).isEqualTo("Arroz");
        assertThat(criado.usuarioId()).isEqualTo(5L);
        assertThat(criado.status()).isEqualTo(Status.ATIVA);
        verify(ingredienteRepository).save(any(Ingrediente.class));
    }

    @Test
    void shouldThrowUsuarioNotFoundExceptionWhenCreateReceivesUnknownUsuario() {
        IngredienteDTO dto = new IngredienteDTO(
                null,
                "Feijao",
                new BigDecimal("20.00"),
                new BigDecimal("60.00"),
                new BigDecimal("2.00"),
                new BigDecimal("12.00"),
                Status.ATIVA,
                new BigDecimal("0.10"),
                77L
        );


        when(ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatus("Feijao", 77L, Status.ATIVA))
                .thenReturn(false);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(Optional.of(criarUsuario(99L)));
        when(usuarioRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.create(dto, 77L))
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void shouldThrowIngredienteNotFoundExceptionWhenIngredienteDoesNotExist() {
        when(ingredienteRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.getIngredienteById(100L))
                .isInstanceOf(IngredienteNotFoundException.class)
                .hasMessage("Ingrediente não encontrado");
    }

    @Test
    void shouldUpdateIngredienteWhenFound() {
        Usuario usuario = criarUsuario(8L);
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(15L);
        ingrediente.setNome("Arroz Branco");
        ingrediente.setPtn(new BigDecimal("6.00"));
        ingrediente.setCho(new BigDecimal("70.00"));
        ingrediente.setLip(new BigDecimal("1.00"));
        ingrediente.setSodio(new BigDecimal("3.00"));
        ingrediente.setGorduraSaturada(new BigDecimal("0.10"));
        ingrediente.setStatus(Status.ATIVA);
        ingrediente.setUsuario(usuario);

        IngredienteDTO dto = new IngredienteDTO(
                15L,
                "Arroz Integral",
                new BigDecimal("8.00"),
                new BigDecimal("72.00"),
                new BigDecimal("2.00"),
                new BigDecimal("4.00"),
                Status.INATIVA,
                new BigDecimal("0.30"),
                8L
        );


        when(ingredienteRepository.findById(15L)).thenReturn(Optional.of(ingrediente));
        when(ingredienteRepository.existsByNomeIgnoreCaseAndUsuario_IdAndStatusAndIdNot("Arroz Integral", 8L, Status.ATIVA, 15L))
                .thenReturn(false);
        when(usuarioRepository.findByUsernameIgnoreCase("TACO")).thenReturn(Optional.of(criarUsuario(99L)));
        when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);

        IngredienteGetDTO atualizado = ingredienteService.update(15L, dto);


        assertThat(atualizado.nome()).isEqualTo("Arroz Integral");
        assertThat(atualizado.ptn()).isEqualByComparingTo("8.00");
        assertThat(atualizado.status()).isEqualTo(Status.INATIVA);
        verify(ingredienteRepository).save(ingrediente);
    }

    private Usuario criarUsuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername("usuario-" + id);
        usuario.setEmail("usuario" + id + "@exemplo.com");
        usuario.setSenha("senha");
        usuario.setAtivo(true);
        return usuario;
    }
}
