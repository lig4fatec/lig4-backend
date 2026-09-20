package br.com.lig4.dto;

import jakarta.validation.constraints.NotNull;

public record PartidaRequestDTO(

        @NotNull(message = "O id do jogador 1 é obrigatório")
        Long jogadorUmId,

        @NotNull(message = "O id do jogador 2 é obrigatório")
        Long jogadorDoisId
) {
}
