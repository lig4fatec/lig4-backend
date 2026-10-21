package br.com.lig4.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JogadaRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        @NotNull(message = "A coluna é obrigatória")
        @Min(value = 0, message = "A coluna deve estar entre 0 e 6")
        @Max(value = 6, message = "A coluna deve estar entre 0 e 6")
        Integer coluna
) {
}
