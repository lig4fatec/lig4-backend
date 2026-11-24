package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** O avatar equipado do jogador aparece na sala e na partida (para o front mostrar o do adversário). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AvatarTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    @Test
    void aSalaTrazOAvatarDoDono() {
        long dono = criarJogador("AvatarSala");

        Resposta r = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"modo\":\"CLASSICO\",\"cor\":\"blue\"}".formatted(dono));

        assertEquals(201, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"avatar\":\"avatar-padrao\""), r.corpo());
    }

    @Test
    void oEstadoDaPartidaTrazOAvatarDosJogadores() {
        long j1 = criarJogador("AvatarP1");
        long j2 = criarJogador("AvatarP2");
        long partida = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(j1, j2)).id();

        Resposta r = get("/partidas/" + partida + "/estado");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"avatar\":\"avatar-padrao\""), r.corpo());
    }
}
