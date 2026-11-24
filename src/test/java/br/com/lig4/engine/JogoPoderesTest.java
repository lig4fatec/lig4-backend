package br.com.lig4.engine;

import static br.com.lig4.engine.Disco.AMARELO;
import static br.com.lig4.engine.Disco.VERMELHO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.lig4.engine.JogadaInvalidaException.Motivo;
import org.junit.jupiter.api.Test;

/** Regras dos poderes do modo Caos, no motor puro (sem banco). */
class JogoPoderesTest {

    private static Motivo motivo(Runnable r) {
        return assertThrows(JogadaInvalidaException.class, r::run).getMotivo();
    }

    /** Enche uma coluna sem checar vitória, só para montar situações de teste. */
    private static void encher(Jogo jogo, int coluna, int pecas) {
        for (int i = 0; i < pecas; i++) {
            jogo.getTabuleiro().soltar(coluna, i % 2 == 0 ? VERMELHO : AMARELO);
        }
    }

    // Congelar Coluna

    @Test
    void congelarBloqueiaAColunaPorUmaVezDoAdversario() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.congelar(VERMELHO, 3);
        assertEquals(AMARELO, jogo.getTurno());

        assertEquals(Motivo.COLUNA_CONGELADA, motivo(() -> jogo.jogar(AMARELO, 3))); // bloqueado na vez dele
        jogo.jogar(AMARELO, 0); // joga em outra coluna; a vez conta e o congelamento acaba
        assertTrue(jogo.getCongelamentos().isEmpty());

        jogo.jogar(VERMELHO, 1);
        jogo.jogar(AMARELO, 3); // agora já pode
        assertEquals(AMARELO, jogo.getTabuleiro().get(0, 3));
    }


    @Test
    void congelarMostraQuantasVezesFaltam() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.congelar(VERMELHO, 5);
        assertEquals(new Congelamento(5, AMARELO, 1), jogo.getCongelamentos().get(0));
        jogo.jogar(AMARELO, 0); // uma vez do adversário já basta para acabar
        assertTrue(jogo.getCongelamentos().isEmpty());
    }

    @Test
    void congelarNaoValeEmColunaCheia() {
        Jogo jogo = new Jogo(VERMELHO);
        encher(jogo, 0, 6);
        assertEquals(Motivo.COLUNA_CHEIA, motivo(() -> jogo.congelar(VERMELHO, 0)));
        assertEquals(Motivo.COLUNA_INVALIDA, motivo(() -> jogo.congelar(VERMELHO, 7)));
    }

    @Test
    void congelarSoUmaVezPorJogador() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.congelar(VERMELHO, 3);
        jogo.jogar(AMARELO, 0);
        assertEquals(Motivo.PODER_JA_USADO, motivo(() -> jogo.congelar(VERMELHO, 4)));
    }

    @Test
    void quemSoTemColunaCongeladaPerdeAVez() {
        Jogo jogo = new Jogo(VERMELHO);
        for (int c = 1; c < 7; c++) {
            encher(jogo, c, 6);
        }
        jogo.congelar(VERMELHO, 0); // o amarelo só poderia jogar na coluna 0
        assertEquals(VERMELHO, jogo.getTurno());
    }

    // Queimar Peça

    @Test
    void queimarRemoveAPecaDoAdversarioEGastaAVez() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.jogar(VERMELHO, 0);
        jogo.queimar(AMARELO, 0, 0);
        assertNull(jogo.getTabuleiro().get(0, 0));
        assertEquals(VERMELHO, jogo.getTurno());
    }

    @Test
    void queimarNaoValeNoPrimeiroLanceNemEmPecaPropriaOuCasaVazia() {
        Jogo jogo = new Jogo(VERMELHO);
        assertEquals(Motivo.ALVO_INVALIDO, motivo(() -> jogo.queimar(VERMELHO, 0, 0)));
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 1);
        assertEquals(Motivo.ALVO_INVALIDO, motivo(() -> jogo.queimar(VERMELHO, 0, 0)));
        assertEquals(Motivo.ALVO_INVALIDO, motivo(() -> jogo.queimar(VERMELHO, 4, 0)));
        assertEquals(Motivo.ALVO_INVALIDO, motivo(() -> jogo.queimar(VERMELHO, 9, 9)));
    }

    @Test
    void queimarSoUmaVezPorJogador() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 1);
        jogo.queimar(VERMELHO, 1, 0);
        jogo.jogar(AMARELO, 1);
        assertEquals(Motivo.PODER_JA_USADO, motivo(() -> jogo.queimar(VERMELHO, 1, 0)));
    }

    @Test
    void depoisDeQueimarAsPecasDeCimaDescem() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 0);
        jogo.jogar(VERMELHO, 2);
        jogo.queimar(AMARELO, 0, 0); // queima o vermelho da base; o amarelo de cima desce
        assertEquals(AMARELO, jogo.getTabuleiro().get(0, 0));
        assertEquals(1, jogo.jogar(VERMELHO, 0).linha()); // a próxima peça cai por cima
    }

    // Peça Extra

    @Test
    void pecaExtraDaDuasJogadasSeguidas() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.pecaExtra(VERMELHO);
        assertTrue(jogo.isPecaExtraAtiva());
        assertEquals(VERMELHO, jogo.getTurno());

        jogo.jogar(VERMELHO, 0);
        assertEquals(VERMELHO, jogo.getTurno());
        jogo.jogar(VERMELHO, 1);
        assertEquals(AMARELO, jogo.getTurno());
        assertTrue(!jogo.isPecaExtraAtiva());
    }

    @Test
    void pecaExtraNaoValeComUmEspacoSo() {
        Jogo jogo = new Jogo(VERMELHO);
        for (int c = 0; c < 6; c++) {
            encher(jogo, c, 6);
        }
        encher(jogo, 6, 5);
        assertEquals(1, jogo.getTabuleiro().espacosLivres());
        assertEquals(Motivo.PECA_EXTRA_SEM_ESPACO, motivo(() -> jogo.pecaExtra(VERMELHO)));
    }

    @Test
    void pecaExtraSoUmaVezEPoderesSoNoComecoDaVez() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.pecaExtra(VERMELHO);
        assertEquals(Motivo.PODER_JA_USADO, motivo(() -> jogo.pecaExtra(VERMELHO)));
        assertEquals(Motivo.PODER_FORA_DE_HORA, motivo(() -> jogo.congelar(VERMELHO, 2)));
        jogo.jogar(VERMELHO, 0);
        assertEquals(Motivo.PODER_FORA_DE_HORA, motivo(() -> jogo.congelar(VERMELHO, 2)));
    }

    @Test
    void pecaExtraGanhaNaPrimeiraPecaSeFizerQuatro() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 6);
        jogo.jogar(VERMELHO, 1);
        jogo.jogar(AMARELO, 6);
        jogo.jogar(VERMELHO, 2);
        jogo.jogar(AMARELO, 5);
        jogo.pecaExtra(VERMELHO);
        jogo.jogar(VERMELHO, 3);
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
    }

    @Test
    void poderForaDaVezOuDepoisDoFim() {
        Jogo jogo = new Jogo(VERMELHO);
        assertEquals(Motivo.FORA_DO_TURNO, motivo(() -> jogo.pecaExtra(AMARELO)));
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 1);
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 1);
        jogo.jogar(VERMELHO, 0);
        jogo.jogar(AMARELO, 1);
        jogo.jogar(VERMELHO, 0); // quatro na coluna 0
        assertEquals(Motivo.JOGO_ENCERRADO, motivo(() -> jogo.pecaExtra(AMARELO)));
    }
}