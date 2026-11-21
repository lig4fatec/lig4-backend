package br.com.lig4.dto;

import br.com.lig4.engine.Poder;

/** Um poder de um jogador: usado na partida ou ainda disponível para ele. */
public record PoderDTO(Long jogadorId, Poder poder) {
}