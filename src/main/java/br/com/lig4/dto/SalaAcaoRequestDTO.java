package br.com.lig4.dto;

import jakarta.validation.constraints.NotNull;

public record SalaAcaoRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId
) {
}