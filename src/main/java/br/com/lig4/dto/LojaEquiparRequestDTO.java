package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;

public record LojaEquiparRequestDTO(

        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

        @NotBlank(message = "O item é obrigatório")
        String itemId
) {
}