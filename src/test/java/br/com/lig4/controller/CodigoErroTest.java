package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CodigoErroTest extends ApiTestSupport {

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

    @Test
    void jogadorInexistenteDevolveCodigo() {
        esperar(get("/jogadores/999999999"), 404, "JOGADOR_NAO_ENCONTRADO");
    }

    @Test
    void partidaInexistenteDevolveCodigo() {
        esperar(get("/partidas/999999999/estado"), 404, "PARTIDA_NAO_ENCONTRADA");
    }

    @Test
    void emailRepetidoDevolveCodigo() {
        String corpo = "{\"nome\":\"Repetido\",\"email\":\"%s\"}".formatted(emailUnico("rep"));
        assertEquals(201, enviar("POST", "/jogadores", corpo).status());
        esperar(enviar("POST", "/jogadores", corpo), 409, "EMAIL_JA_CADASTRADO");
    }

    @Test
    void emailInvalidoDevolveCodigo() {
        esperar(enviar("POST", "/jogadores", "{\"nome\":\"Ana\",\"email\":\"invalido\"}"),
                400, "EMAIL_INVALIDO");
    }

    @Test
    void corInvalidaDevolveCodigo() {
        long id = criarJogador("Cora");
        esperar(enviar("PUT", "/jogadores/" + id + "/cor", "{\"cor\":\"azul\"}"),
                400, "COR_INVALIDA");
    }

    @Test
    void corpoMalformadoDevolveCodigo() {
        esperar(enviar("POST", "/jogadores", "{"), 400, "REQUISICAO_INVALIDA");
    }

    @Test
    void jogadaForaDoTurnoDevolveCodigo() {
        long j1 = criarJogador("Turno1");
        long j2 = criarJogador("Turno2");
        long partida = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(j1, j2)).id();

        esperar(enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":0}".formatted(j2)), 409, "FORA_DO_TURNO");
    }
}
