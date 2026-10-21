package br.com.lig4.engine;

import static br.com.lig4.engine.Disco.AMARELO;
import static br.com.lig4.engine.Disco.VERMELHO;
import static org.junit.jupiter.api.Assertions.*;

import br.com.lig4.engine.JogadaInvalidaException.Motivo;
import org.junit.jupiter.api.Test;

class JogoTest {

    private static Motivo motivo(Runnable r) {
        return assertThrows(JogadaInvalidaException.class, r::run).getMotivo();
    }

    /** Joga a sequência alternando as cores a partir do vermelho. */
    private static Jogo jogar(int... colunas) {
        Jogo jogo = new Jogo(VERMELHO);
        for (int c : colunas) {
            jogo.jogar(jogo.getTurno(), c);
        }
        return jogo;
    }

    @Test
    void pecaCaiAteABase() {
        Jogo jogo = new Jogo(VERMELHO);
        assertEquals(0, jogo.jogar(VERMELHO, 3).linha());
        assertEquals(1, jogo.jogar(AMARELO, 3).linha());
    }

    @Test
    void turnoAlterna() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.jogar(VERMELHO, 0);
        assertEquals(AMARELO, jogo.getTurno());
    }

    @Test
    void foraDoTurno() {
        Jogo jogo = new Jogo(VERMELHO);
        assertEquals(Motivo.FORA_DO_TURNO, motivo(() -> jogo.jogar(AMARELO, 0)));
    }

    @Test
    void colunaInvalida() {
        Jogo jogo = new Jogo(VERMELHO);
        assertEquals(Motivo.COLUNA_INVALIDA, motivo(() -> jogo.jogar(VERMELHO, -1)));
        assertEquals(Motivo.COLUNA_INVALIDA, motivo(() -> jogo.jogar(VERMELHO, 7)));
    }

    @Test
    void colunaCheia() {
        Jogo jogo = jogar(0, 0, 0, 0, 0, 0); // sem 4 em linha
        assertEquals(Motivo.COLUNA_CHEIA, motivo(() -> jogo.jogar(jogo.getTurno(), 0)));
    }

    @Test
    void jogadaInvalidaNaoTrocaOTurno() {
        Jogo jogo = new Jogo(VERMELHO);
        assertThrows(JogadaInvalidaException.class, () -> jogo.jogar(VERMELHO, 9));
        assertEquals(VERMELHO, jogo.getTurno());
    }

    @Test
    void vitoriaHorizontal() {
        Jogo jogo = jogar(0, 0, 1, 1, 2, 2, 3);
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
        assertEquals(4, jogo.getSequenciaVencedora().size());
    }

    @Test
    void vitoriaVertical() {
        Jogo jogo = jogar(0, 1, 0, 1, 0, 1, 0);
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
    }

    @Test
    void vitoriaDiagonalAscendente() {
        // vermelho em (0,0) (1,1) (2,2) (3,3)
        Jogo jogo = jogar(0, 1, 1, 2, 3, 2, 2, 3, 3, 6, 3);
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
    }

    @Test
    void vitoriaDiagonalDescendente() {
        // vermelho em (0,3) (1,2) (2,1) (3,0)
        Jogo jogo = jogar(3, 2, 2, 1, 0, 1, 1, 0, 0, 6, 0);
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
    }

    @Test
    void vitoriaDoAmarelo() {
        Jogo jogo = jogar(0, 1, 0, 1, 0, 1, 6, 1);
        assertEquals(AMARELO, jogo.getVencedor());
    }

    @Test
    void naoJogaDepoisDoFim() {
        Jogo jogo = jogar(0, 0, 1, 1, 2, 2, 3);
        assertEquals(Motivo.JOGO_ENCERRADO, motivo(() -> jogo.jogar(AMARELO, 4)));
    }

    @Test
    void empateComTabuleiroCheio() {
        // Sequência de 42 jogadas que enche o tabuleiro sem nenhum 4 em linha.
        int[] sequencia = {0, 5, 4, 6, 6, 4, 2, 1, 1, 0, 6, 4, 1, 3, 4, 2, 6, 2, 3, 5, 4,
                4, 3, 6, 3, 0, 6, 3, 3, 0, 1, 1, 5, 2, 0, 0, 1, 5, 2, 5, 5, 2};
        Jogo jogo = jogar(sequencia);
        assertEquals(StatusJogo.EMPATE, jogo.getStatus());
        assertNull(jogo.getVencedor());
        assertEquals(42, jogo.getHistorico().size());
        assertTrue(jogo.getTabuleiro().cheio());
    }
}
