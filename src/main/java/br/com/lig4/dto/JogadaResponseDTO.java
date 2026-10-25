package br.com.lig4.dto;

import br.com.lig4.domain.StatusPartida;
import br.com.lig4.engine.StatusJogo;

public record JogadaResponseDTO(
        int numero,
        Long jogadorId,
        int coluna,
        int linha,
        StatusPartida statusPartida,
        StatusJogo resultado,
        Long vencedorId,
        Long proximoJogadorId,
        String[][] tabuleiro
) {
}
