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
class PartidaDaSalaTest extends ApiTestSupport {

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

    /** Cria a sala, coloca o convidado e inicia. Devolve {pin, partidaId}. */
    private String[] salaIniciada(long dono, long convidado) {
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"serie\":3,\"cor\":\"blue\"}".formatted(dono));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = enviar("POST", "/salas/" + pin + "/iniciar", "{\"jogadorId\":%d}".formatted(dono));
        assertEquals(200, iniciada.status(), iniciada.corpo());
        return new String[]{pin, extrair(iniciada, "\"partidaId\"\\s*:\\s*(\\d+)")};
    }

    private Resposta jogar(String partidaId, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partidaId + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    @Test
    void estadoDaPartidaDaSalaTrazPinSerieECoresDaSala() {
        long dono = criarJogador("Vera");
        long convidado = criarJogador("Wagner");
        String[] sala = salaIniciada(dono, convidado);

        Resposta estado = get("/partidas/" + sala[1] + "/estado");

        assertEquals(200, estado.status(), estado.corpo());
        assertTrue(estado.corpo().contains("\"pin\":\"" + sala[0] + "\""), estado.corpo());
        assertTrue(estado.corpo().contains("\"serie\":3"), estado.corpo());
        assertTrue(estado.corpo().contains("\"cor\":\"blue\""), estado.corpo());
        assertTrue(estado.corpo().contains("\"cor\":\"red\""), estado.corpo());
    }

    @Test
    void partidaCriadaSemSalaNaoTemPin() {
        long j1 = criarJogador("Xuxa");
        long j2 = criarJogador("Yuri");
        long partida = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(j1, j2)).id();

        Resposta estado = get("/partidas/" + partida + "/estado");

        assertEquals(200, estado.status(), estado.corpo());
        assertFalse(estado.corpo().contains("\"pin\":\""), estado.corpo());
    }

    @Test
    void salaEncerraQuandoAPartidaTermina() {
        long dono = criarJogador("Zeca");
        long convidado = criarJogador("Alice");
        String[] sala = salaIniciada(dono, convidado);

        // o dono empilha 4 peças na coluna 0 e vence
        assertEquals(201, jogar(sala[1], dono, 0).status());
        assertEquals(201, jogar(sala[1], convidado, 1).status());
        assertEquals(201, jogar(sala[1], dono, 0).status());
        assertEquals(201, jogar(sala[1], convidado, 1).status());
        assertEquals(201, jogar(sala[1], dono, 0).status());
        assertEquals(201, jogar(sala[1], convidado, 1).status());
        assertEquals(201, jogar(sala[1], dono, 0).status());

        Resposta consulta = get("/salas/" + sala[0]);
        assertEquals(200, consulta.status(), consulta.corpo());
        assertTrue(consulta.corpo().contains("\"fase\":\"ENCERRADA\""), consulta.corpo());
    }
}
