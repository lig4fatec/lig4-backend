package br.com.lig4.engine;

import static br.com.lig4.engine.Disco.AMARELO;
import static br.com.lig4.engine.Disco.VERMELHO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Gravidade depois do Queimar (PRD-0026). Os testes montam o tabuleiro direto (soltar), porque
 * recriar as situações com jogadas legais seria longo. O turno começa no VERMELHO.
 */
class JogoGravidadeTest {

    @Test
    void aQuedaFazAsPecasDeCimaDescerem() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.getTabuleiro().soltar(2, AMARELO); // base
        jogo.getTabuleiro().soltar(2, VERMELHO);

        jogo.queimar(VERMELHO, 2, 0); // queima o amarelo da base

        assertEquals(VERMELHO, jogo.getTabuleiro().get(0, 2)); // o vermelho desceu
        assertNull(jogo.getTabuleiro().get(1, 2));
        assertEquals(StatusJogo.EM_ANDAMENTO, jogo.getStatus());
        assertEquals(AMARELO, jogo.getTurno()); // Queimar gasta a vez
    }

    @Test
    void aQuedaFormaQuatroVerticalDeQuemQueimou() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.getTabuleiro().soltar(3, AMARELO); // base: alvo
        for (int i = 0; i < 4; i++) {
            jogo.getTabuleiro().soltar(3, VERMELHO);
        }

        jogo.queimar(VERMELHO, 3, 0); // queima o amarelo; os 4 vermelhos descem e alinham

        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getVencedor());
    }

    @Test
    void aQuedaPodeDarVitoriaAoAdversarioFisicaPura() {
        Jogo jogo = new Jogo(VERMELHO);
        // col0..1 base vermelho, col2 base amarelo; col3 = amarelo, vermelho, amarelo
        jogo.getTabuleiro().soltar(0, VERMELHO);
        jogo.getTabuleiro().soltar(0, AMARELO);
        jogo.getTabuleiro().soltar(1, VERMELHO);
        jogo.getTabuleiro().soltar(1, AMARELO);
        jogo.getTabuleiro().soltar(2, AMARELO);
        jogo.getTabuleiro().soltar(2, AMARELO);
        jogo.getTabuleiro().soltar(3, AMARELO); // base: alvo
        jogo.getTabuleiro().soltar(3, VERMELHO);
        jogo.getTabuleiro().soltar(3, AMARELO);
        // linha 1 (acima da base) antes: col0=A, col1=A, col2=A, col3=V -> sem 4

        jogo.queimar(VERMELHO, 3, 0); // queima o amarelo da base da col3; col3 vira V, A

        // col3 linha1 passa a ser A -> linha 1 fica A,A,A,A (física pura: vence o amarelo)
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(AMARELO, jogo.getVencedor());
    }

    @Test
    void seAsDuasCoresFormamQuatroVenceOAdversarioDeQuemQueimou() {
        Jogo jogo = new Jogo(VERMELHO);
        // base: col0=V,col1=V,col2=V ; col3 = A(base), V, A
        jogo.getTabuleiro().soltar(0, VERMELHO);
        jogo.getTabuleiro().soltar(0, AMARELO);
        jogo.getTabuleiro().soltar(1, VERMELHO);
        jogo.getTabuleiro().soltar(1, AMARELO);
        jogo.getTabuleiro().soltar(2, VERMELHO);
        jogo.getTabuleiro().soltar(2, AMARELO);
        jogo.getTabuleiro().soltar(3, AMARELO); // alvo
        jogo.getTabuleiro().soltar(3, VERMELHO);
        jogo.getTabuleiro().soltar(3, AMARELO);

        jogo.queimar(VERMELHO, 3, 0);
        // depois da queda: linha0 = V,V,V,V e linha1 = A,A,A,A -> as duas; vence o amarelo
        assertEquals(StatusJogo.VENCEDOR, jogo.getStatus());
        assertEquals(AMARELO, jogo.getVencedor());
    }

    @Test
    void semQuatroAQuedaSoPassaAVez() {
        Jogo jogo = new Jogo(VERMELHO);
        jogo.getTabuleiro().soltar(0, VERMELHO);
        jogo.getTabuleiro().soltar(1, AMARELO);
        jogo.getTabuleiro().soltar(1, VERMELHO);

        jogo.queimar(VERMELHO, 1, 0); // queima o amarelo da base da col1

        assertEquals(StatusJogo.EM_ANDAMENTO, jogo.getStatus());
        assertEquals(VERMELHO, jogo.getTabuleiro().get(0, 1));
        assertEquals(AMARELO, jogo.getTurno());
    }
}