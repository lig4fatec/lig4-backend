package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Uso de um poder. {@code poder} é QUEIMAR (coluna e linha do alvo, com linha 0 na base),
 * CONGELAR (coluna) ou EXTRA (sem alvo).
 */
public record PoderRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        @NotBlank(message = "O poder é obrigatório")
        String poder,

        Integer coluna,

        Integer linha
) {
}