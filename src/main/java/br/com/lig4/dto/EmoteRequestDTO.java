package br.com.lig4.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Envio de um emote durante a partida. {@code emote} é o id de um item da categoria "emote" da loja. */
public record EmoteRequestDTO(

        @NotNull(message = "O id do jogador é obrigatório")
        Long jogadorId,

        @NotBlank(message = "O emote é obrigatório")
        String emote
) {
}