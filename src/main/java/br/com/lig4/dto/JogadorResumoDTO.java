package br.com.lig4.dto;

import br.com.lig4.domain.Jogador;

public record JogadorResumoDTO(
        Long id,
        String nome,
        String cor,
        /** Id do avatar equipado, para o front mostrar o avatar do jogador na partida. */
        String avatar
) {
    public static JogadorResumoDTO from(Jogador jogador) {
        return new JogadorResumoDTO(jogador.id, jogador.nome, jogador.cor, jogador.avatarEquipado);
    }
}