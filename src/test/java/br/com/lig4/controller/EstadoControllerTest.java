package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EstadoControllerTest extends ApiTestSupport {

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

    @Test
    void estadoInicialDeUmaPartidaNova() {
        long j1 = criarJogador("Rita");
        long j2 = criarJogador("Saulo");
        long partida = novaPartida(j1, j2);

        Resposta r = get("/partidas/" + partida + "/estado");
        assertEquals(200, r.status());
        assertTrue(r.corpo().contains("\"resultado\":\"EM_ANDAMENTO\""));
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j1));
        assertTrue(r.corpo().contains("\"totalJogadas\":0"));
    }

    @Test
    void estadoDepoisDeUmaJogada() {
        long j1 = criarJogador("Tania");
        long j2 = criarJogador("Ulisses");
        long partida = novaPartida(j1, j2);

        enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":3}".formatted(j1));

        Resposta r = get("/partidas/" + partida + "/estado");
        assertEquals(200, r.status());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j2));
        assertTrue(r.corpo().contains("\"totalJogadas\":1"));
        assertTrue(r.corpo().contains("VERMELHO"));
    }

    @Test
    void deveRetornar404ParaPartidaInexistente() {
        assertEquals(404, get("/partidas/999999999/estado").status());
    }
}