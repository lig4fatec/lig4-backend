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
class SalaControllerTest extends ApiTestSupport {

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

    private Resposta criarSala(long jogador, String cor) {
        return enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"cor\":\"%s\"}".formatted(jogador, cor));
    }

    private Resposta entrar(String pin, long jogador, String cor) {
        return enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"%s\"}".formatted(jogador, pin, cor));
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    @Test
    void deveCriarEConsultarSala() {
        long dono = criarJogador("Sara");
        Resposta criada = criarSala(dono, "blue");

        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"fase\":\"AGUARDANDO\""));
        assertTrue(criada.corpo().contains("\"modo\":\"CLASSICO\""));
        assertTrue(criada.corpo().contains("\"cor\":\"blue\""));

        Resposta consulta = get("/salas/" + pinDe(criada));
        assertEquals(200, consulta.status(), consulta.corpo());
        assertTrue(consulta.corpo().contains("Sara"));
    }

    @Test
    void deveEntrarNaSalaComOutraCor() {
        long dono = criarJogador("Dora");
        long convidado = criarJogador("Caio");
        String pin = pinDe(criarSala(dono, "blue"));

        Resposta r = entrar(pin, convidado, "red");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("Caio"));
        assertTrue(r.corpo().contains("\"cor\":\"red\""));
    }

    @Test
    void entrarComAMesmaCorDoDonoEhRecusado() {
        long dono = criarJogador("Eva");
        long convidado = criarJogador("Davi");
        String pin = pinDe(criarSala(dono, "blue"));

        esperar(entrar(pin, convidado, "blue"), 409, "COR_JA_ESCOLHIDA");
    }

    @Test
    void salaComConvidadoNaoAceitaUmTerceiro() {
        long dono = criarJogador("Fabio");
        long convidado = criarJogador("Gabi");
        long terceiro = criarJogador("Hugo");
        String pin = pinDe(criarSala(dono, "blue"));
        assertEquals(200, entrar(pin, convidado, "red").status());

        esperar(entrar(pin, terceiro, "green"), 409, "SALA_CHEIA");
    }

    @Test
    void convidadoPodeEntrarDeNovoSemMudarNada() {
        long dono = criarJogador("Iris");
        long convidado = criarJogador("Jose");
        String pin = pinDe(criarSala(dono, "blue"));
        assertEquals(200, entrar(pin, convidado, "red").status());

        Resposta de_novo = entrar(pin, convidado, "red");

        assertEquals(200, de_novo.status(), de_novo.corpo());
        assertTrue(de_novo.corpo().contains("Jose"));
    }

    @Test
    void pinInexistenteDevolve404() {
        long jogador = criarJogador("Lia");
        esperar(entrar("000000", jogador, "red"), 404, "PIN_NAO_ENCONTRADO");
        esperar(get("/salas/000000"), 404, "PIN_NAO_ENCONTRADO");
    }

    @Test
    void pinMalFormadoDevolvePinInvalido() {
        long jogador = criarJogador("Mara");
        esperar(entrar("12ab", jogador, "red"), 400, "PIN_INVALIDO");
        esperar(get("/salas/12ab"), 400, "PIN_INVALIDO");
    }

    @Test
    void modoCaosEhAceito() {
        long jogador = criarJogador("Nico");
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"modo\":\"CAOS\",\"cor\":\"blue\"}".formatted(jogador));

        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"modo\":\"CAOS\""), criada.corpo());
        assertTrue(get("/salas/" + pinDe(criada)).corpo().contains("\"modo\":\"CAOS\""));
    }

    @Test
    void modoInvalidoEhRecusado() {
        long jogador = criarJogador("Nina");
        esperar(enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"modo\":\"SERIE\",\"cor\":\"blue\"}".formatted(jogador)),
                400, "MODO_INVALIDO");
    }

    @Test
    void serieEnviadaPeloFrontAntigoEhIgnoradaENaoVoltaNaResposta() {
        long jogador = criarJogador("Nuno");
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"serie\":3,\"cor\":\"blue\"}".formatted(jogador));

        assertEquals(201, criada.status(), criada.corpo());
        assertTrue(criada.corpo().contains("\"modo\":\"CLASSICO\""), criada.corpo());
        assertFalse(criada.corpo().contains("\"serie\""), criada.corpo());
        assertFalse(criada.corpo().contains("\"placar\""), criada.corpo());
    }

    @Test
    void corForaDaListaEhRecusada() {
        long jogador = criarJogador("Olga");
        esperar(criarSala(jogador, "pink"), 400, "COR_INVALIDA");
    }

    @Test
    void jogadorInexistenteDevolve404() {
        esperar(criarSala(999999999L, "blue"), 404, "JOGADOR_NAO_ENCONTRADO");
    }
}