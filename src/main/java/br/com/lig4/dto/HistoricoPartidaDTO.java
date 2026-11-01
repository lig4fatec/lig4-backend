package br.com.lig4.dto;

import br.com.lig4.domain.ResultadoPartida;

import java.time.Instant;

public record HistoricoPartidaDTO(
        Long id,
        Instant data,
        String adversario,
        ResultadoPartida resultado,
        int jogadas,
        String modo
) {
}
