package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalaSerieTest extends ApiTestSupport {

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

    private String partidaIdDe(Resposta r) {
        return extrair(r, "\"partidaId\"\\s*:\\s*(\\d+)");
    }

    private Resposta jogar(String partidaId, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partidaId + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private Resposta revanche(String pin, long jogador) {
        return enviar("POST", "/salas/" + pin + "/revanche", "{\"jogadorId\":%d}".formatted(jogador));
    }

    /** Sala iniciada (dono blue, convidado red). Devolve {pin, partidaId}. */
    private String[] salaIniciada(int serie, long dono, long convidado) {
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"serie\":%d,\"cor\":\"blue\"}".formatted(dono, serie));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = enviar("POST", "/salas/" + pin + "/iniciar", "{\"jogadorId\":%d}".formatted(dono));
        assertEquals(200, iniciada.status(), iniciada.corpo());
        return new String[]{pin, partidaIdDe(iniciada)};
    }

    /** Quem começa empilha 4 peças na coluna 0 e vence; o outro joga na coluna 1. */
    private void vence(String partidaId, long quemComeca, long outro) {
        assertEquals(201, jogar(partidaId, quemComeca, 0).status());
        assertEquals(201, jogar(partidaId, outro, 1).status());
        assertEquals(201, jogar(partidaId, quemComeca, 0).status());
        assertEquals(201, jogar(partidaId, outro, 1).status());
        assertEquals(201, jogar(partidaId, quemComeca, 0).status());
        assertEquals(201, jogar(partidaId, outro, 1).status());
        assertEquals(201, jogar(partidaId, quemComeca, 0).status());
    }

    private String placar(long dono, int vitoriasDono, long convidado, int vitoriasConvidado) {
        return "\"placar\":{\"%d\":%d,\"%d\":%d}".formatted(dono, vitoriasDono, convidado, vitoriasConvidado);
    }

    @Test
    void salaNovaComecaComPlacarZeradoESerieAberta() {
        long dono = criarJogador("PlacarA");
        Resposta criada = enviar("POST", "/salas", "{\"jogadorId\":%d,\"serie\":3,\"cor\":\"blue\"}".formatted(dono));

        assertTrue(criada.corpo().contains("\"placar\":{\"" + dono + "\":0}"), criada.corpo());
        assertTrue(criada.corpo().contains("\"serieEncerrada\":false"), criada.corpo());
        assertTrue(criada.corpo().contains("\"campeaoId\":null"), criada.corpo());
    }

    @Test
    void umaVitoriaNaSerieDe3SomaNoPlacarSemEncerrarASerie() {
        long dono = criarJogador("PlacarB");
        long convidado = criarJogador("PlacarC");
        String[] sala = salaIniciada(3, dono, convidado);

        vence(sala[1], dono, convidado);

        Resposta r = get("/salas/" + sala[0]);
        assertTrue(r.corpo().contains(placar(dono, 1, convidado, 0)), r.corpo());
        assertTrue(r.corpo().contains("\"serieEncerrada\":false"), r.corpo());
        assertTrue(r.corpo().contains("\"campeaoId\":null"), r.corpo());
    }

    @Test
    void serieDe3TerminaQuandoAlguemFazDuasVitoriasEARevancheComecaSerieNova() {
        long dono = criarJogador("SerieA");
        long convidado = criarJogador("SerieB");
        String[] sala = salaIniciada(3, dono, convidado);

        // 1ª: o dono começa e vence
        vence(sala[1], dono, convidado);
        revanche(sala[0], dono);
        String segunda = partidaIdDe(revanche(sala[0], convidado));

        // 2ª: o convidado começa e vence (1 x 1)
        vence(segunda, convidado, dono);
        assertTrue(get("/salas/" + sala[0]).corpo().contains(placar(dono, 1, convidado, 1)));
        revanche(sala[0], dono);
        String terceira = partidaIdDe(revanche(sala[0], convidado));

        // 3ª: o dono começa de novo e vence (2 x 1): a série acaba
        vence(terceira, dono, convidado);

        Resposta fim = get("/salas/" + sala[0]);
        assertTrue(fim.corpo().contains(placar(dono, 2, convidado, 1)), fim.corpo());
        assertTrue(fim.corpo().contains("\"serieEncerrada\":true"), fim.corpo());
        assertTrue(fim.corpo().contains("\"campeaoId\":" + dono), fim.corpo());

        // a revanche depois da série decidida abre uma série nova
        revanche(sala[0], dono);
        Resposta nova = revanche(sala[0], convidado);
        assertEquals(200, nova.status(), nova.corpo());
        assertTrue(nova.corpo().contains("\"fase\":\"JOGANDO\""), nova.corpo());
        assertTrue(nova.corpo().contains(placar(dono, 0, convidado, 0)), nova.corpo());
        assertTrue(nova.corpo().contains("\"serieEncerrada\":false"), nova.corpo());
    }

    @Test
    void serieDe1TerminaNaPrimeiraVitoria() {
        long dono = criarJogador("UmaA");
        long convidado = criarJogador("UmaB");
        String[] sala = salaIniciada(1, dono, convidado);

        vence(sala[1], dono, convidado);

        Resposta r = get("/salas/" + sala[0]);
        assertTrue(r.corpo().contains("\"serieEncerrada\":true"), r.corpo());
        assertTrue(r.corpo().contains("\"campeaoId\":" + dono), r.corpo());
    }

    @Test
    void oEstadoDaPartidaTrazOPlacar() {
        long dono = criarJogador("EstadoA");
        long convidado = criarJogador("EstadoB");
        String[] sala = salaIniciada(3, dono, convidado);

        vence(sala[1], dono, convidado);

        Resposta estado = get("/partidas/" + sala[1] + "/estado");
        assertEquals(200, estado.status(), estado.corpo());
        assertTrue(estado.corpo().contains(placar(dono, 1, convidado, 0)), estado.corpo());
        assertTrue(estado.corpo().contains("\"serieEncerrada\":false"), estado.corpo());
    }
}