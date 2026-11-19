package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalaProntoTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private String pinDe(Resposta r) {
        Matcher m = Pattern.compile("\"pin\"\\s*:\\s*\"(\\d{6})\"").matcher(r.corpo());
        if (!m.find()) {
            throw new AssertionError("Resposta sem pin: " + r);
        }
        return m.group(1);
    }

    private String criarSala(long dono) {
        Resposta r = enviar("POST", "/salas", "{\"jogadorId\":%d,\"cor\":\"blue\"}".formatted(dono));
        assertEquals(201, r.status(), r.corpo());
        return pinDe(r);
    }

    private Resposta entrar(String pin, long jogador) {
        return enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(jogador, pin));
    }

    private Resposta pronto(String pin, long jogador, boolean valor) {
        return enviar("PUT", "/salas/" + pin + "/pronto",
                "{\"jogadorId\":%d,\"pronto\":%s}".formatted(jogador, valor));
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    /** Sala com dono (blue) e convidado (red) já dentro. */
    private String salaCompleta(long dono, long convidado) {
        String pin = criarSala(dono);
        assertEquals(200, entrar(pin, convidado).status());
        return pin;
    }

    @Test
    void salaNovaComecaComNinguemPronto() {
        long dono = criarJogador("ProntoA");
        Resposta sala = get("/salas/" + criarSala(dono));

        assertTrue(sala.corpo().contains("\"prontoDono\":false"), sala.corpo());
        assertTrue(sala.corpo().contains("\"prontoConvidado\":false"), sala.corpo());
    }

    @Test
    void umSoProntoNaoComecaAPartida() {
        long dono = criarJogador("ProntoB");
        long convidado = criarJogador("ProntoC");
        String pin = salaCompleta(dono, convidado);

        Resposta r = pronto(pin, dono, true);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"fase\":\"AGUARDANDO\""), r.corpo());
        assertTrue(r.corpo().contains("\"prontoDono\":true"), r.corpo());
        assertTrue(r.corpo().contains("\"prontoConvidado\":false"), r.corpo());
        assertTrue(r.corpo().contains("\"partidaId\":null"), r.corpo());
    }

    @Test
    void osDoisProntosComecamAPartidaPeloDono() {
        long dono = criarJogador("ProntoD");
        long convidado = criarJogador("ProntoE");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, pronto(pin, convidado, true).status());

        Resposta r = pronto(pin, dono, true);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"fase\":\"JOGANDO\""), r.corpo());
        // as marcas voltam a falso quando a partida começa
        assertTrue(r.corpo().contains("\"prontoDono\":false"), r.corpo());
        assertTrue(r.corpo().contains("\"prontoConvidado\":false"), r.corpo());
        Matcher m = Pattern.compile("\"partidaId\"\\s*:\\s*(\\d+)").matcher(r.corpo());
        assertTrue(m.find(), r.corpo());

        Resposta estado = get("/partidas/" + m.group(1) + "/estado");
        assertEquals(200, estado.status(), estado.corpo());
        assertTrue(estado.corpo().contains("\"proximoJogadorId\":" + dono), estado.corpo());
    }

    @Test
    void donoPodeMarcarProntoAntesDoConvidadoEntrar() {
        long dono = criarJogador("ProntoF");
        long convidado = criarJogador("ProntoG");
        String pin = criarSala(dono);

        Resposta r = pronto(pin, dono, true);
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"fase\":\"AGUARDANDO\""), r.corpo());

        assertEquals(200, entrar(pin, convidado).status());
        Resposta comecou = pronto(pin, convidado, true);
        assertTrue(comecou.corpo().contains("\"fase\":\"JOGANDO\""), comecou.corpo());
    }

    @Test
    void desmarcarOProntoImpedeOInicio() {
        long dono = criarJogador("ProntoH");
        long convidado = criarJogador("ProntoI");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, pronto(pin, dono, true).status());

        Resposta desmarcou = pronto(pin, dono, false);
        assertTrue(desmarcou.corpo().contains("\"prontoDono\":false"), desmarcou.corpo());

        Resposta r = pronto(pin, convidado, true);
        assertTrue(r.corpo().contains("\"fase\":\"AGUARDANDO\""), r.corpo());
    }

    @Test
    void quemNaoEstaNaSalaNaoMarcaPronto() {
        long dono = criarJogador("ProntoJ");
        long estranho = criarJogador("ProntoK");
        String pin = criarSala(dono);

        esperar(pronto(pin, estranho, true), 403, "SEM_PERMISSAO");
    }

    @Test
    void naoMarcaProntoDepoisQueAPartidaComecou() {
        long dono = criarJogador("ProntoL");
        long convidado = criarJogador("ProntoM");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, marcarProntos(pin, dono, convidado).status());

        esperar(pronto(pin, dono, true), 409, "FASE_INVALIDA");
    }

    @Test
    void trocarDeCorDesmarcaOProntoDeQuemTrocou() {
        long dono = criarJogador("ProntoN");
        long convidado = criarJogador("ProntoO");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, pronto(pin, convidado, true).status());

        Resposta r = enviar("PUT", "/salas/" + pin + "/cor",
                "{\"jogadorId\":%d,\"cor\":\"green\"}".formatted(convidado));

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"prontoConvidado\":false"), r.corpo());
    }

    @Test
    void convidadoQueSaiLimpaOsProntosEONovoConvidadoComecaDoZero() {
        long dono = criarJogador("ProntoP");
        long convidado = criarJogador("ProntoQ");
        long outro = criarJogador("ProntoR");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, pronto(pin, dono, true).status());

        assertEquals(204, enviar("POST", "/salas/" + pin + "/sair",
                "{\"jogadorId\":%d}".formatted(convidado)).status());

        Resposta sala = get("/salas/" + pin);
        assertTrue(sala.corpo().contains("\"prontoDono\":false"), sala.corpo());
        assertEquals(200, entrar(pin, outro).status());
        Resposta r = pronto(pin, outro, true);
        assertTrue(r.corpo().contains("\"fase\":\"AGUARDANDO\""), r.corpo());
    }

    @Test
    void prontoSemOCampoEhRecusado() {
        long dono = criarJogador("ProntoS");
        String pin = criarSala(dono);

        Resposta r = enviar("PUT", "/salas/" + pin + "/pronto", "{\"jogadorId\":%d}".formatted(dono));

        assertEquals(400, r.status(), r.corpo());
        assertFalse(r.corpo().contains("\"prontoDono\""), r.corpo());
    }
}
