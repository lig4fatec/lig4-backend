package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModoDeJogoTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private String extrair(Resposta r, String regex) {
        Matcher m = Pattern.compile(regex).matcher(r.corpo());
        if (!m.find()) {
            throw new AssertionError("Não encontrei " + regex + " em: " + r);
        }
        return m.group(1);
    }

    private Resposta jogar(String partidaId, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partidaId + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private void esperarMoedas(long jogador, int moedas) {
        Resposta r = get("/jogadores/" + jogador + "/perfil");
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":" + moedas), r.corpo());
    }

    /** Sala no modo pedido, em andamento (dono blue, convidado red; os dois marcaram pronto). Devolve {pin, partidaId}. */
    private String[] salaIniciada(String modo, long dono, long convidado) {
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"modo\":\"%s\",\"cor\":\"blue\"}".formatted(dono, modo));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = marcarProntos(pin, dono, convidado);
        assertEquals(200, iniciada.status(), iniciada.corpo());
        return new String[]{pin, extrair(iniciada, "\"partidaId\"\\s*:\\s*(\\d+)")};
    }

    /** O dono, que começa a primeira partida, empilha 4 peças na coluna 0 e vence. */
    private void donoVence(String partidaId, long dono, long convidado) {
        assertEquals(201, jogar(partidaId, dono, 0).status());
        assertEquals(201, jogar(partidaId, convidado, 1).status());
        assertEquals(201, jogar(partidaId, dono, 0).status());
        assertEquals(201, jogar(partidaId, convidado, 1).status());
        assertEquals(201, jogar(partidaId, dono, 0).status());
        assertEquals(201, jogar(partidaId, convidado, 1).status());
        assertEquals(201, jogar(partidaId, dono, 0).status());
    }

    @Test
    void salaCaosDaMoedasDeCaos() {
        long dono = criarJogador("CaosA");
        long convidado = criarJogador("CaosB");
        String[] sala = salaIniciada("CAOS", dono, convidado);

        donoVence(sala[1], dono, convidado);

        // Caos: vitória +30, derrota +5
        esperarMoedas(dono, 530);
        esperarMoedas(convidado, 505);
    }

    @Test
    void salaClassicaDaMoedasDoClassico() {
        long dono = criarJogador("ClasA");
        long convidado = criarJogador("ClasB");
        String[] sala = salaIniciada("CLASSICO", dono, convidado);

        donoVence(sala[1], dono, convidado);

        // Clássico: vitória +15, derrota +2
        esperarMoedas(dono, 515);
        esperarMoedas(convidado, 502);
    }

    @Test
    void partidaCriadaComModoEmQualquerCaixaEFinalizadaDaMoedasDeCaos() {
        long j1 = criarJogador("DiretoA");
        long j2 = criarJogador("DiretoB");
        Resposta criada = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d,\"modo\":\"caos\"}".formatted(j1, j2));

        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"modo\":\"CAOS\""), criada.corpo());

        assertEquals(200, enviar("PUT", "/partidas/" + criada.id(), "{}").status());
        // Caos: empate +10 para os dois
        esperarMoedas(j1, 510);
        esperarMoedas(j2, 510);
    }

    @Test
    void modoInvalidoNaPartidaEhRecusado() {
        long j1 = criarJogador("InvalidoA");
        long j2 = criarJogador("InvalidoB");

        Resposta r = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d,\"modo\":\"SERIE\"}".formatted(j1, j2));

        assertEquals(400, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"MODO_INVALIDO\""), r.corpo());
    }

    @Test
    void revancheMantemOModoDaSala() {
        long dono = criarJogador("RevA");
        long convidado = criarJogador("RevB");
        String[] sala = salaIniciada("CAOS", dono, convidado);
        donoVence(sala[1], dono, convidado);

        assertEquals(200, enviar("POST", "/salas/" + sala[0] + "/revanche",
                "{\"jogadorId\":%d}".formatted(dono)).status());
        Resposta segundo = enviar("POST", "/salas/" + sala[0] + "/revanche",
                "{\"jogadorId\":%d}".formatted(convidado));
        assertEquals(200, segundo.status(), segundo.corpo());

        String novaPartida = extrair(segundo, "\"partidaId\"\\s*:\\s*(\\d+)");
        Resposta estado = get("/partidas/" + novaPartida + "/estado");
        assertTrue(estado.corpo().contains("\"modo\":\"CAOS\""), estado.corpo());
    }
}