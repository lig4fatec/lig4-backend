package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaControllerTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private Resposta criarPartida(long um, long dois) {
        return enviar("POST", "/partidas", "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois));
    }

    @Test
    void deveCriarPartidaEFinalizarComVencedor() {
        long j1 = criarJogador("Carla");
        long j2 = criarJogador("Diego");

        Resposta partida = criarPartida(j1, j2);
        assertEquals(201, partida.status());
        assertTrue(partida.corpo().contains("EM_ANDAMENTO"));

        Resposta fim = enviar("PUT", "/partidas/" + partida.id(), "{\"vencedorId\":%d}".formatted(j1));
        assertEquals(200, fim.status());
        assertTrue(fim.corpo().contains("FINALIZADA"));
        assertTrue(fim.corpo().contains("\"vencedor\":{\"id\":" + j1));
    }

    @Test
    void deveRegistrarEmpateQuandoVencedorIdForNulo() {
        long j1 = criarJogador("Elis");
        long j2 = criarJogador("Fabio");

        Resposta partida = criarPartida(j1, j2);
        Resposta fim = enviar("PUT", "/partidas/" + partida.id(), "{}");
        assertEquals(200, fim.status());
        assertTrue(fim.corpo().contains("FINALIZADA"));
        assertTrue(fim.corpo().contains("\"vencedor\":null"));
    }

    @Test
    void naoDeveCriarPartidaComJogadorIgual() {
        long j1 = criarJogador("Gustavo");
        assertEquals(400, criarPartida(j1, j1).status());
    }

    @Test
    void naoDeveCriarPartidaComJogadorInexistente() {
        long j1 = criarJogador("Helena");
        assertEquals(404, criarPartida(j1, 999999999L).status());
    }
}
