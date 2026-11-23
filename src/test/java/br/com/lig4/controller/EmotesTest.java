package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Emotes durante a partida, por HTTP (o WebSocket é testado em PartidaAcoesWebSocketTest). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmotesTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    private long partida(long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois)).id();
    }

    private Resposta emote(long partida, long jogador, String emote) {
        return enviar("POST", "/partidas/" + partida + "/emotes",
                "{\"jogadorId\":%d,\"emote\":\"%s\"}".formatted(jogador, emote));
    }

    @Test
    void participanteEnviaUmEmote() {
        long j1 = criarJogador("EmoteA");
        long j2 = criarJogador("EmoteB");
        long p = partida(j1, j2);

        Resposta r = emote(p, j1, "emote-risadinha");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"jogadorId\":" + j1), r.corpo());
        assertTrue(r.corpo().contains("\"emote\":\"emote-risadinha\""), r.corpo());
    }

    @Test
    void emoteInexistenteOuDeOutraCategoriaEhRecusado() {
        long j1 = criarJogador("EmoteC");
        long j2 = criarJogador("EmoteD");
        long p = partida(j1, j2);

        esperar(emote(p, j1, "emote-que-nao-existe"), 400, "EMOTE_INVALIDO");
        esperar(emote(p, j1, "avatar-samurai"), 400, "EMOTE_INVALIDO"); // existe, mas não é emote
    }

    @Test
    void quemNaoParticipaNaoEnvia() {
        long j1 = criarJogador("EmoteE");
        long j2 = criarJogador("EmoteF");
        long fora = criarJogador("EmoteG");
        long p = partida(j1, j2);

        esperar(emote(p, fora, "emote-risadinha"), 400, "PARTIDA_INVALIDA");
    }

    @Test
    void partidaInexistenteDevolve404() {
        long j1 = criarJogador("EmoteH");
        esperar(emote(999999999L, j1, "emote-risadinha"), 404, "PARTIDA_NAO_ENCONTRADA");
    }
}
