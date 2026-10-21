package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CorRequestDTO(

        @NotBlank(message = "A cor é obrigatória")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "A cor deve estar no formato #RRGGBB")
        String cor
) {
}
