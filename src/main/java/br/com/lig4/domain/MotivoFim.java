package br.com.lig4.domain;

/** Como a partida terminou. Fica vazio enquanto ela está em andamento. */
public enum MotivoFim {
    /** Alguém ligou quatro peças. */
    VITORIA,
    /** O tabuleiro encheu sem vencedor. */
    TABULEIRO_CHEIO,
    /** Um jogador desistiu e o adversário venceu. */
    DESISTENCIA,
    /** Um jogador propôs empate e o outro aceitou. */
    EMPATE_ACORDADO,
    /** O tempo de um jogador acabou (previsto para o relógio da partida). */
    TEMPO
}
