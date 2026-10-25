package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JogadaControllerTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private long novaPartida(long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois)).id();
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    @Test
    void deveJogarAteAVitoriaVerticalEFinalizarAPartida() {
        long j1 = criarJogador("Iara");
        long j2 = criarJogador("Joao");
        long partida = novaPartida(j1, j2);

        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());

        Resposta ultima = jogar(partida, j1, 0);
        assertEquals(201, ultima.status());
        assertTrue(ultima.corpo().contains("\"resultado\":\"VENCEDOR\""));
        assertTrue(ultima.corpo().contains("\"statusPartida\":\"FINALIZADA\""));
        assertTrue(ultima.corpo().contains("\"vencedorId\":" + j1));

        // partida encerrada: nova jogada é recusada
        assertEquals(409, jogar(partida, j2, 3).status());

        Resposta historico = get("/partidas/" + partida + "/jogadas");
        assertEquals(200, historico.status());
        assertTrue(historico.corpo().contains("\"numero\":7"));
    }

    @Test
    void naoDeveJogarForaDoTurno() {
        long j1 = criarJogador("Karen");
        long j2 = criarJogador("Lucas");
        long partida = novaPartida(j1, j2);

        assertEquals(409, jogar(partida, j2, 0).status());
    }

    @Test
    void naoDeveJogarEmColunaInvalida() {
        long j1 = criarJogador("Marta");
        long j2 = criarJogador("Nuno");
        long partida = novaPartida(j1, j2);

        assertEquals(400, jogar(partida, j1, 7).status());
    }

    @Test
    void naoDevePermitirJogadorDeFora() {
        long j1 = criarJogador("Olga");
        long j2 = criarJogador("Paulo");
        long intruso = criarJogador("Quico");
        long partida = novaPartida(j1, j2);

        assertEquals(400, jogar(partida, intruso, 0).status());
    }
}
