package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Relógio por jogador. Aqui cada jogador tem 3 segundos, para o fim por tempo caber no teste. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "lig4.relogio.tempo-por-jogador-segundos=3")
class RelogioTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private long numero(Resposta r, String campo) {
        Matcher m = Pattern.compile("\"" + campo + "\"\\s*:\\s*(\\d+)").matcher(r.corpo());
        if (!m.find()) {
            throw new AssertionError("Não encontrei " + campo + " em: " + r);
        }
        return Long.parseLong(m.group(1));
    }

    private String extrair(Resposta r, String regex) {
        Matcher m = Pattern.compile(regex).matcher(r.corpo());
        if (!m.find()) {
            throw new AssertionError("Não encontrei " + regex + " em: " + r);
        }
        return m.group(1);
    }

    private long novaPartida(long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois)).id();
    }

    private Resposta estado(long partida) {
        return get("/partidas/" + partida + "/estado");
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    /** Espera (até 10 segundos) o agendador terminar a partida por tempo e devolve o estado final. */
    private Resposta esperarFim(long partida) throws InterruptedException {
        long limite = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < limite) {
            Resposta r = estado(partida);
            if (r.corpo().contains("\"statusPartida\":\"FINALIZADA\"")) {
                return r;
            }
            Thread.sleep(250);
        }
        throw new AssertionError("A partida " + partida + " não terminou por tempo em 10 segundos");
    }

    private void esperarMoedas(long jogador, int moedas) {
        Resposta r = get("/jogadores/" + jogador + "/perfil");
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":" + moedas), r.corpo());
    }

    @Test
    void partidaNovaComecaComOTempoTotalECorrendoParaQuemJogaPrimeiro() {
        long j1 = criarJogador("RelogioA");
        long j2 = criarJogador("RelogioB");
        long partida = novaPartida(j1, j2);

        Resposta r = estado(partida);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"relogioDe\":" + j1), r.corpo());
        assertTrue(numero(r, "tempoJogadorUmMs") <= 3000, r.corpo());
        assertEquals(3000, numero(r, "tempoJogadorDoisMs"), r.corpo());
    }

    @Test
    void aJogadaPassaORelogioParaOAdversarioEMostraOsTempos() {
        long j1 = criarJogador("RelogioC");
        long j2 = criarJogador("RelogioD");
        long partida = novaPartida(j1, j2);

        Resposta jogada = jogar(partida, j1, 3);

        assertEquals(201, jogada.status(), jogada.corpo());
        assertTrue(jogada.corpo().contains("\"relogioDe\":" + j2), jogada.corpo());
        assertTrue(numero(jogada, "tempoJogadorUmMs") <= 3000, jogada.corpo());
        assertTrue(numero(jogada, "tempoJogadorDoisMs") <= 3000, jogada.corpo());

        Resposta r = estado(partida);
        assertTrue(r.corpo().contains("\"relogioDe\":" + j2), r.corpo());
    }

    @Test
    void quemFicaSemTempoPerdeEOAdversarioVence() throws InterruptedException {
        long j1 = criarJogador("RelogioE");
        long j2 = criarJogador("RelogioF");
        long partida = novaPartida(j1, j2); // o relógio do jogador 1 corre desde o início

        Resposta fim = esperarFim(partida);

        assertTrue(fim.corpo().contains("\"motivoFim\":\"TEMPO\""), fim.corpo());
        assertTrue(fim.corpo().contains("\"vencedorId\":" + j2), fim.corpo());
        assertTrue(fim.corpo().contains("\"resultado\":\"VENCEDOR\""), fim.corpo());
        assertTrue(fim.corpo().contains("\"relogioDe\":null"), fim.corpo());
        assertEquals(0, numero(fim, "tempoJogadorUmMs"), fim.corpo());
        // Clássico: vitória +15, derrota +2
        esperarMoedas(j2, 515);
        esperarMoedas(j1, 502);
    }

    @Test
    void depoisDoFimPorTempoNaoSeJoga() throws InterruptedException {
        long j1 = criarJogador("RelogioG");
        long j2 = criarJogador("RelogioH");
        long partida = novaPartida(j1, j2);
        esperarFim(partida);

        Resposta r = jogar(partida, j1, 0);

        assertEquals(409, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"JOGO_ENCERRADO\""), r.corpo());
    }

    @Test
    void oFimPorTempoEncerraASalaEPagaAsMoedasDoCaos() throws InterruptedException {
        long dono = criarJogador("RelogioI");
        long convidado = criarJogador("RelogioJ");
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"modo\":\"CAOS\",\"cor\":\"blue\"}".formatted(dono));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = marcarProntos(pin, dono, convidado);
        long partida = Long.parseLong(extrair(iniciada, "\"partidaId\"\\s*:\\s*(\\d+)"));

        esperarFim(partida); // o dono começa e não joga: perde por tempo

        assertTrue(get("/salas/" + pin).corpo().contains("\"fase\":\"ENCERRADA\""));
        // Caos: vitória +30, derrota +5
        esperarMoedas(convidado, 530);
        esperarMoedas(dono, 505);
    }

    @Test
    void desistirParaORelogio() throws InterruptedException {
        long j1 = criarJogador("RelogioK");
        long j2 = criarJogador("RelogioL");
        long partida = novaPartida(j1, j2);

        Resposta r = enviar("POST", "/partidas/" + partida + "/desistir", "{\"jogadorId\":%d}".formatted(j1));

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"relogioDe\":null"), r.corpo());
        long antes = numero(r, "tempoJogadorUmMs");
        Thread.sleep(500);
        assertEquals(antes, numero(estado(partida), "tempoJogadorUmMs"));
    }

    @Test
    void partidaTerminadaNaJogadaTambemParaORelogio() {
        long j1 = criarJogador("RelogioM");
        long j2 = criarJogador("RelogioN");
        long partida = novaPartida(j1, j2);
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());

        Resposta ultima = jogar(partida, j1, 0);

        assertEquals(201, ultima.status(), ultima.corpo());
        assertTrue(ultima.corpo().contains("\"relogioDe\":null"), ultima.corpo());
        assertTrue(estado(partida).corpo().contains("\"motivoFim\":\"VITORIA\""));
    }
}
