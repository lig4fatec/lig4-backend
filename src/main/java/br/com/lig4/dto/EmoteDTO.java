package br.com.lig4.dto;

/** Emote enviado por um jogador, transmitido aos dois participantes da partida. */
public record EmoteDTO(Long jogadorId, String emote) {
}