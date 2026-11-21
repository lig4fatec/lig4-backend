package br.com.lig4.dto;

/** Coluna bloqueada para um jogador, com quantas vezes dele ainda faltam. */
public record CongelamentoDTO(int coluna, Long jogadorId, int turnosRestantes) {
}