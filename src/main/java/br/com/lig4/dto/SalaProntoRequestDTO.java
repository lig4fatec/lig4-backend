package br.com.lig4.dto;

import jakarta.validation.constraints.NotNull;

public record SalaProntoRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        @NotNull(message = "Informe se o jogador está pronto (true ou false)")
        Boolean pronto
) {
}
