package br.com.lig4.dto;

import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

public record PartidaEstadoDTO(
        Long partidaId,
        JogadorResumoDTO jogadorUm,
        JogadorResumoDTO jogadorDois,
        StatusPartida statusPartida,
        StatusJogo resultado,
        Long vencedorId,
        Long proximoJogadorId,
        int totalJogadas,
        String[][] tabuleiro
) {
}
