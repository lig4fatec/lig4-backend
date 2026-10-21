package br.com.lig4.dto;

import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

public record PartidaEstadoDTO(
        Long partidaId,
        StatusPartida statusPartida,
        StatusJogo resultado,
        Long vencedorId,
        Long proximoJogadorId,
        int totalJogadas,
        String[][] tabuleiro
) {
} 
