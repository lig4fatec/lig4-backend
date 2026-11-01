package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SalaRevancheTest extends ApiTestSupport {

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

    private Resposta revanche(String pin, long jogador) {
        return enviar("POST", "/salas/" + pin + "/revanche", "{\"jogadorId\":%d}".formatted(jogador));
    }

    private Resposta jogar(String partidaId, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partidaId + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    /** Sala iniciada (dono blue, convidado red). Devolve {pin, partidaId}. */
    private String[] salaIniciada(long dono, long convidado) {
        Resposta criada = enviar("POST", "/salas",
                "{\"jogadorId\":%d,\"serie\":3,\"cor\":\"blue\"}".formatted(dono));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = enviar("POST", "/salas/" + pin + "/iniciar", "{\"jogadorId\":%d}".formatted(dono));
        assertEquals(200, iniciada.status(), iniciada.corpo());
        return new String[]{pin, partidaIdDe(iniciada)};
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
    void naoPodePedirRevancheComAPartidaEmAndamento() {
        long dono = criarJogador("Bia");
        long convidado = criarJogador("Caue");
        String[] sala = salaIniciada(dono, convidado);

        esperar(revanche(sala[0], dono), 409, "FASE_INVALIDA");
    }

    @Test
    void umPedidoAindaNaoComecaANovaPartida() {
        long dono = criarJogador("Dani");
        long convidado = criarJogador("Edu");
        String[] sala = salaIniciada(dono, convidado);
        donoVence(sala[1], dono, convidado);

        Resposta r = revanche(sala[0], convidado);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"fase\":\"ENCERRADA\""), r.corpo());
        assertTrue(r.corpo().contains("\"revancheDe\":[" + convidado + "]"), r.corpo());
    }

    @Test
    void osDoisPedidosComecamNovaPartidaEAlternamQuemComeca() {
        long dono = criarJogador("Flavia");
        long convidado = criarJogador("Gil");
        String[] sala = salaIniciada(dono, convidado);
        donoVence(sala[1], dono, convidado);

        assertEquals(200, revanche(sala[0], dono).status());
        Resposta segundo = revanche(sala[0], convidado);

        assertEquals(200, segundo.status(), segundo.corpo());
        assertTrue(segundo.corpo().contains("\"fase\":\"JOGANDO\""), segundo.corpo());
        String novaPartida = partidaIdDe(segundo);
        assertNotEquals(sala[1], novaPartida);

        Resposta estado = get("/partidas/" + novaPartida + "/estado");
        assertEquals(200, estado.status(), estado.corpo());
        // quem começa agora é o convidado, e as cores da sala acompanham cada jogador
        assertTrue(estado.corpo().contains("\"proximoJogadorId\":" + convidado), estado.corpo());
        assertTrue(estado.corpo().contains("\"id\":" + convidado + ",\"nome\":\"Gil\",\"cor\":\"red\""),
                estado.corpo());
        assertTrue(estado.corpo().contains("\"id\":" + dono + ",\"nome\":\"Flavia\",\"cor\":\"blue\""),
                estado.corpo());
    }

    @Test
    void quemNaoEstaNaSalaNaoPedeRevanche() {
        long dono = criarJogador("Hugo");
        long convidado = criarJogador("Ines");
        long estranho = criarJogador("Joel");
        String[] sala = salaIniciada(dono, convidado);
        donoVence(sala[1], dono, convidado);

        esperar(revanche(sala[0], estranho), 403, "SEM_PERMISSAO");
    }

    @Test
    void quemSaiDepoisDoFimFechaASala() {
        long dono = criarJogador("Kely");
        long convidado = criarJogador("Léo");
        String[] sala = salaIniciada(dono, convidado);
        donoVence(sala[1], dono, convidado);

        assertEquals(204, enviar("POST", "/salas/" + sala[0] + "/sair",
                "{\"jogadorId\":%d}".formatted(convidado)).status());

        esperar(get("/salas/" + sala[0]), 404, "PIN_NAO_ENCONTRADO");
    }
}
