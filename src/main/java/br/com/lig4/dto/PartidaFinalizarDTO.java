package br.com.lig4.dto;

/**
 * Usado no PUT /partidas/{id} para encerrar a partida.
 * vencedorId nulo = empate.
 */
public record PartidaFinalizarDTO(
        Long vencedorId
) {
}
