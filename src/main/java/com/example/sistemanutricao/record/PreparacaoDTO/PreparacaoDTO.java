package com.example.sistemanutricao.record.PreparacaoDTO;

import com.example.sistemanutricao.model.enuns.Categoria;
import java.math.BigDecimal;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;

public record PreparacaoDTO(
        Long id,
        @NotBlank(message = "O nome da preparação é obrigatório.")
        @Size(max = 100, message = "O nome da preparação deve ter no máximo 100 caracteres.")
        String nome,
        @NotNull(message = "O número da preparação é obrigatório.")
        @Max(value = 999999999L, message = "O número deve ter no máximo 9 dígitos.")
        Integer numero,
        @NotBlank(message = "O tempo de preparo é obrigatório.")
        @Size(max = 30, message = "O tempo de preparo pode ter no máximo 30 caracteres.")
        String tempoPreparo,
        @NotBlank(message = "Os equipamentos são obrigatórios.")
        String equipamentos,
        @NotBlank(message = "O modo de preparo é obrigatório.")
        String modoPreparo,
        @Digits(integer = 10, fraction = 5, message = "A quantidade de água deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        @DecimalMax(value = "100", message = "A porcentagem de água deve ser menor ou igual a 100.")
        BigDecimal porcentAgua,
        @Digits(integer = 10, fraction = 5, message = "A quantidade de água deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal qntdAgua,
        @NotNull(message = "O FCC é obrigatório.")
        @Digits(integer = 10, fraction = 5, message = "O FCC deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal fcc,
        @Digits(integer = 10, fraction = 5, message = "O rendimento deve ter no máximo 10 dígitos inteiros e 5 casas decimais.")
        BigDecimal rendimento,
        @NotNull(message = "A categoria é obrigatória.")
        Categoria categoria
) {}
