package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HistoricoControllerTest extends ApiTestSupport {

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

    private long novaPartida(long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d}".formatted(um, dois)).id();
    }

    private void finalizar(long partida, Long vencedor) {
        String corpo = vencedor == null ? "{}" : "{\"vencedorId\":%d}".formatted(vencedor);
        assertEquals(200, enviar("PUT", "/partidas/" + partida, corpo).status());
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private Resposta historico(long jogador, String consulta) {
        return get("/jogadores/" + jogador + "/historico" + consulta);
    }

    @Test
    void jogadorSemPartidasTemHistoricoVazio() {
        long id = criarJogador("Novato");

        Resposta r = historico(id, "");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"partidas\":[]"), r.corpo());
    }

    @Test
    void mostraVitoriaParaUmEDerrotaParaOOutro() {
        long j1 = criarJogador("Vence");
        long j2 = criarJogador("Perde");
        long partida = novaPartida(j1, j2);
        finalizar(partida, j1);

        Resposta doVencedor = historico(j1, "");
        assertTrue(doVencedor.corpo().contains("\"resultado\":\"VITORIA\""), doVencedor.corpo());
        assertTrue(doVencedor.corpo().contains("\"adversario\":\"Perde\""), doVencedor.corpo());
        assertTrue(doVencedor.corpo().contains("\"modo\":\"online\""), doVencedor.corpo());

        Resposta doPerdedor = historico(j2, "");
        assertTrue(doPerdedor.corpo().contains("\"resultado\":\"DERROTA\""), doPerdedor.corpo());
        assertTrue(doPerdedor.corpo().contains("\"adversario\":\"Vence\""), doPerdedor.corpo());
    }

    @Test
    void mostraEmpate() {
        long j1 = criarJogador("EmpA");
        long j2 = criarJogador("EmpB");
        finalizar(novaPartida(j1, j2), null);

        assertTrue(historico(j1, "").corpo().contains("\"resultado\":\"EMPATE\""));
    }

    @Test
    void contaAsJogadasDaPartida() {
        long j1 = criarJogador("Conta1");
        long j2 = criarJogador("Conta2");
        long partida = novaPartida(j1, j2);
        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        jogar(partida, j1, 0);
        jogar(partida, j2, 1);
        assertEquals(201, jogar(partida, j1, 0).status());

        Resposta r = historico(j1, "");

        assertTrue(r.corpo().contains("\"jogadas\":7"), r.corpo());
    }

    @Test
    void listaDaMaisRecenteParaAMaisAntiga() {
        long j1 = criarJogador("Ordem1");
        long j2 = criarJogador("Ordem2");
        long primeira = novaPartida(j1, j2);
        finalizar(primeira, j1);
        long segunda = novaPartida(j1, j2);
        finalizar(segunda, j2);

        String corpo = historico(j1, "").corpo();

        assertTrue(corpo.indexOf("\"id\":" + segunda) < corpo.indexOf("\"id\":" + primeira), corpo);
    }

    @Test
    void partidaEmAndamentoNaoApareceNoHistorico() {
        long j1 = criarJogador("Andando1");
        long j2 = criarJogador("Andando2");
        novaPartida(j1, j2);

        assertTrue(historico(j1, "").corpo().contains("\"partidas\":[]"));
    }

    @Test
    void filtraPorModo() {
        long j1 = criarJogador("Filtro1");
        long j2 = criarJogador("Filtro2");
        finalizar(novaPartida(j1, j2), j1);

        assertFalse(historico(j1, "?filtro=online").corpo().contains("\"partidas\":[]"));
        assertFalse(historico(j1, "?filtro=todos").corpo().contains("\"partidas\":[]"));
        assertTrue(historico(j1, "?filtro=diario").corpo().contains("\"partidas\":[]"));
        esperar(historico(j1, "?filtro=qualquer"), 400, "FILTRO_INVALIDO");
    }

    @Test
    void jogadorInexistenteDevolve404() {
        esperar(historico(999999999L, ""), 404, "JOGADOR_NAO_ENCONTRADO");
    }
}
