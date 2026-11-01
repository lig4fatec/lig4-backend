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
class SalaLobbyTest extends ApiTestSupport {

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

    private String criarSala(long dono, String cor) {
        Resposta r = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"serie\":1,\"cor\":\"%s\"}".formatted(dono, cor));
        assertEquals(201, r.status(), r.corpo());
        return pinDe(r);
    }

    private Resposta entrar(String pin, long jogador, String cor) {
        return enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"%s\"}".formatted(jogador, pin, cor));
    }

    private Resposta trocarCor(String pin, long jogador, String cor) {
        return enviar("PUT", "/salas/" + pin + "/cor",
                "{\"jogadorId\":%d,\"cor\":\"%s\"}".formatted(jogador, cor));
    }

    private Resposta iniciar(String pin, long jogador) {
        return enviar("POST", "/salas/" + pin + "/iniciar", "{\"jogadorId\":%d}".formatted(jogador));
    }

    private Resposta sair(String pin, long jogador) {
        return enviar("POST", "/salas/" + pin + "/sair", "{\"jogadorId\":%d}".formatted(jogador));
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    /** Sala com dono (blue) e convidado (red) já dentro. */
    private String salaCompleta(long dono, long convidado) {
        String pin = criarSala(dono, "blue");
        assertEquals(200, entrar(pin, convidado, "red").status());
        return pin;
    }

    @Test
    void deveTrocarDeCorNoLobby() {
        long dono = criarJogador("Ada");
        long convidado = criarJogador("Bruno");
        String pin = salaCompleta(dono, convidado);

        Resposta r = trocarCor(pin, convidado, "green");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"cor\":\"green\""));
    }

    @Test
    void naoPodeTrocarParaACorDoOutroJogador() {
        long dono = criarJogador("Clara");
        long convidado = criarJogador("Dino");
        String pin = salaCompleta(dono, convidado);

        esperar(trocarCor(pin, convidado, "blue"), 409, "COR_JA_ESCOLHIDA");
        esperar(trocarCor(pin, dono, "red"), 409, "COR_JA_ESCOLHIDA");
    }

    @Test
    void quemNaoEstaNaSalaNaoTrocaCor() {
        long dono = criarJogador("Elis");
        long estranho = criarJogador("Fred");
        String pin = criarSala(dono, "blue");

        esperar(trocarCor(pin, estranho, "green"), 403, "SEM_PERMISSAO");
    }

    @Test
    void soODonoPodeIniciar() {
        long dono = criarJogador("Gina");
        long convidado = criarJogador("Hector");
        String pin = salaCompleta(dono, convidado);

        esperar(iniciar(pin, convidado), 403, "SEM_PERMISSAO");
    }

    @Test
    void naoIniciaSemConvidado() {
        long dono = criarJogador("Ivo");
        String pin = criarSala(dono, "blue");

        esperar(iniciar(pin, dono), 409, "SEM_CONVIDADO");
    }

    @Test
    void deveIniciarACriarAPartida() {
        long dono = criarJogador("Joana");
        long convidado = criarJogador("Kleber");
        String pin = salaCompleta(dono, convidado);

        Resposta r = iniciar(pin, dono);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"fase\":\"JOGANDO\""));
        Matcher m = Pattern.compile("\"partidaId\"\\s*:\\s*(\\d+)").matcher(r.corpo());
        assertTrue(m.find(), r.corpo());

        Resposta estado = get("/partidas/" + m.group(1) + "/estado");
        assertEquals(200, estado.status(), estado.corpo());
    }

    @Test
    void naoIniciaDuasVezes() {
        long dono = criarJogador("Lara");
        long convidado = criarJogador("Mauro");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, iniciar(pin, dono).status());

        esperar(iniciar(pin, dono), 409, "FASE_INVALIDA");
    }

    @Test
    void naoTrocaCorDepoisDeIniciar() {
        long dono = criarJogador("Nina");
        long convidado = criarJogador("Otto");
        String pin = salaCompleta(dono, convidado);
        assertEquals(200, iniciar(pin, dono).status());

        esperar(trocarCor(pin, dono, "green"), 409, "FASE_INVALIDA");
    }

    @Test
    void convidadoSaiEAVagaFicaLivre() {
        long dono = criarJogador("Paula");
        long convidado = criarJogador("Caio");
        long outro = criarJogador("Rui");
        String pin = salaCompleta(dono, convidado);

        assertEquals(204, sair(pin, convidado).status());

        Resposta sala = get("/salas/" + pin);
        assertEquals(200, sala.status(), sala.corpo());
        assertFalse(sala.corpo().contains("Caio"));
        assertEquals(200, entrar(pin, outro, "red").status());
    }

    @Test
    void quandoODonoSaiNoLobbyASalaDeixaDeExistir() {
        long dono = criarJogador("Sonia");
        String pin = criarSala(dono, "blue");

        assertEquals(204, sair(pin, dono).status());

        esperar(get("/salas/" + pin), 404, "PIN_NAO_ENCONTRADO");
    }

    @Test
    void quemNaoEstaNaSalaNaoPodeSair() {
        long dono = criarJogador("Tais");
        long estranho = criarJogador("Ugo");
        String pin = criarSala(dono, "blue");

        esperar(sair(pin, estranho), 403, "SEM_PERMISSAO");
    }
}
