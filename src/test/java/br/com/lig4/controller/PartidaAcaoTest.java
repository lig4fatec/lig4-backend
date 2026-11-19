package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartidaAcaoTest extends ApiTestSupport {

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

    /** Partida direta (sem sala) no modo pedido; o primeiro jogador começa. */
    private long novaPartida(String modo, long um, long dois) {
        return enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d,\"modo\":\"%s\"}".formatted(um, dois, modo)).id();
    }

    private Resposta acao(long partida, String caminho, long jogador) {
        return enviar("POST", "/partidas/" + partida + caminho, "{\"jogadorId\":%d}".formatted(jogador));
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    private void esperarMoedas(long jogador, int moedas) {
        Resposta r = get("/jogadores/" + jogador + "/perfil");
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":" + moedas), r.corpo());
    }

    @Test
    void desistirDaAVitoriaAoAdversarioEAsMoedasDoClassico() {
        long j1 = criarJogador("DesistA");
        long j2 = criarJogador("DesistB");
        long partida = novaPartida("CLASSICO", j1, j2);

        Resposta r = acao(partida, "/desistir", j1);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"statusPartida\":\"FINALIZADA\""), r.corpo());
        assertTrue(r.corpo().contains("\"resultado\":\"VENCEDOR\""), r.corpo());
        assertTrue(r.corpo().contains("\"vencedorId\":" + j2), r.corpo());
        assertTrue(r.corpo().contains("\"motivoFim\":\"DESISTENCIA\""), r.corpo());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":null"), r.corpo());
        // Clássico: vitória +15, derrota +2
        esperarMoedas(j2, 515);
        esperarMoedas(j1, 502);
    }

    @Test
    void desistirNoModoCaosDaMoedasDoCaos() {
        long j1 = criarJogador("DesistC");
        long j2 = criarJogador("DesistD");
        long partida = novaPartida("CAOS", j1, j2);

        assertEquals(200, acao(partida, "/desistir", j2).status());

        // Caos: vitória +30, derrota +5
        esperarMoedas(j1, 530);
        esperarMoedas(j2, 505);
    }

    @Test
    void oEstadoDepoisDeDesistirContinuaFinalizado() {
        long j1 = criarJogador("DesistE");
        long j2 = criarJogador("DesistF");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/desistir", j1).status());

        Resposta estado = get("/partidas/" + partida + "/estado");

        assertEquals(200, estado.status(), estado.corpo());
        assertTrue(estado.corpo().contains("\"statusPartida\":\"FINALIZADA\""), estado.corpo());
        assertTrue(estado.corpo().contains("\"vencedorId\":" + j2), estado.corpo());
        assertTrue(estado.corpo().contains("\"motivoFim\":\"DESISTENCIA\""), estado.corpo());
    }

    @Test
    void naoDesisteDepoisQueAPartidaTerminou() {
        long j1 = criarJogador("DesistG");
        long j2 = criarJogador("DesistH");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/desistir", j1).status());

        esperar(acao(partida, "/desistir", j2), 409, "JOGO_ENCERRADO");
        // as moedas são pagas uma vez só
        esperarMoedas(j2, 515);
    }

    @Test
    void depoisDeDesistirNaoSeJoga() {
        long j1 = criarJogador("DesistI");
        long j2 = criarJogador("DesistJ");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/desistir", j2).status());

        esperar(jogar(partida, j1, 0), 409, "JOGO_ENCERRADO");
    }

    @Test
    void quemNaoParticipaNaoDesiste() {
        long j1 = criarJogador("DesistK");
        long j2 = criarJogador("DesistL");
        long estranho = criarJogador("DesistM");
        long partida = novaPartida("CLASSICO", j1, j2);

        esperar(acao(partida, "/desistir", estranho), 400, "PARTIDA_INVALIDA");
    }

    @Test
    void desistirEncerraASalaDaPartida() {
        long dono = criarJogador("DesistN");
        long convidado = criarJogador("DesistO");
        Resposta criada = enviar("POST", "/salas", "{\"jogadorId\":%d,\"cor\":\"blue\"}".formatted(dono));
        String pin = extrair(criada, "\"pin\"\\s*:\\s*\"(\\d{6})\"");
        assertEquals(200, enviar("POST", "/salas/entrar",
                "{\"jogadorId\":%d,\"pin\":\"%s\",\"cor\":\"red\"}".formatted(convidado, pin)).status());
        Resposta iniciada = marcarProntos(pin, dono, convidado);
        long partida = Long.parseLong(extrair(iniciada, "\"partidaId\"\\s*:\\s*(\\d+)"));

        assertEquals(200, acao(partida, "/desistir", convidado).status());

        Resposta sala = get("/salas/" + pin);
        assertTrue(sala.corpo().contains("\"fase\":\"ENCERRADA\""), sala.corpo());
    }

    @Test
    void empateAceitoTerminaEmEmpateComMoedasDeEmpate() {
        long j1 = criarJogador("EmpateA");
        long j2 = criarJogador("EmpateB");
        long partida = novaPartida("CLASSICO", j1, j2);

        Resposta proposta = acao(partida, "/empate", j1);
        assertEquals(200, proposta.status(), proposta.corpo());
        assertTrue(proposta.corpo().contains("\"empatePropostoPor\":" + j1), proposta.corpo());
        assertTrue(proposta.corpo().contains("\"statusPartida\":\"EM_ANDAMENTO\""), proposta.corpo());

        Resposta aceite = acao(partida, "/empate/aceitar", j2);

        assertEquals(200, aceite.status(), aceite.corpo());
        assertTrue(aceite.corpo().contains("\"statusPartida\":\"FINALIZADA\""), aceite.corpo());
        assertTrue(aceite.corpo().contains("\"resultado\":\"EMPATE\""), aceite.corpo());
        assertTrue(aceite.corpo().contains("\"vencedorId\":null"), aceite.corpo());
        assertTrue(aceite.corpo().contains("\"motivoFim\":\"EMPATE_ACORDADO\""), aceite.corpo());
        assertTrue(aceite.corpo().contains("\"empatePropostoPor\":null"), aceite.corpo());
        // Clássico: empate +5 para os dois
        esperarMoedas(j1, 505);
        esperarMoedas(j2, 505);
    }

    @Test
    void empateRecusadoMantemAPartidaEGastaAProposta() {
        long j1 = criarJogador("EmpateC");
        long j2 = criarJogador("EmpateD");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/empate", j1).status());

        Resposta recusa = acao(partida, "/empate/recusar", j2);

        assertEquals(200, recusa.status(), recusa.corpo());
        assertTrue(recusa.corpo().contains("\"statusPartida\":\"EM_ANDAMENTO\""), recusa.corpo());
        assertTrue(recusa.corpo().contains("\"empatePropostoPor\":null"), recusa.corpo());
        assertTrue(recusa.corpo().contains("\"empatesUsados\":[" + j1 + "]"), recusa.corpo());
        assertTrue(recusa.corpo().contains("\"motivoFim\":null"), recusa.corpo());
    }

    @Test
    void cadaJogadorSoPropoeEmpateUmaVez() {
        long j1 = criarJogador("EmpateE");
        long j2 = criarJogador("EmpateF");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/empate", j1).status());
        assertEquals(200, acao(partida, "/empate/recusar", j2).status());

        esperar(acao(partida, "/empate", j1), 409, "EMPATE_JA_PROPOSTO");

        // o adversário ainda tem a própria proposta
        assertEquals(200, acao(partida, "/empate", j2).status());
    }

    @Test
    void naoPropoeEmpateComPropostaAguardandoResposta() {
        long j1 = criarJogador("EmpateG");
        long j2 = criarJogador("EmpateH");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/empate", j1).status());

        esperar(acao(partida, "/empate", j2), 409, "EMPATE_PENDENTE");
    }

    @Test
    void quemPropoeNaoAceitaAPropriaProposta() {
        long j1 = criarJogador("EmpateI");
        long j2 = criarJogador("EmpateJ");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/empate", j1).status());

        esperar(acao(partida, "/empate/aceitar", j1), 409, "SEM_PROPOSTA_DE_EMPATE");
        esperar(acao(partida, "/empate/recusar", j1), 409, "SEM_PROPOSTA_DE_EMPATE");
    }

    @Test
    void semPropostaNaoHaOQueAceitarNemRecusar() {
        long j1 = criarJogador("EmpateK");
        long j2 = criarJogador("EmpateL");
        long partida = novaPartida("CLASSICO", j1, j2);

        esperar(acao(partida, "/empate/aceitar", j2), 409, "SEM_PROPOSTA_DE_EMPATE");
        esperar(acao(partida, "/empate/recusar", j2), 409, "SEM_PROPOSTA_DE_EMPATE");
    }

    @Test
    void jogadaDoAdversarioRecusaAPropostaMasADeQuemPropoeNao() {
        long j1 = criarJogador("EmpateM");
        long j2 = criarJogador("EmpateN");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(200, acao(partida, "/empate", j1).status());

        // quem propôs continua jogando e a proposta segue aberta
        assertEquals(201, jogar(partida, j1, 0).status());
        Resposta aberta = get("/partidas/" + partida + "/estado");
        assertTrue(aberta.corpo().contains("\"empatePropostoPor\":" + j1), aberta.corpo());

        // o adversário jogar é o mesmo que recusar
        assertEquals(201, jogar(partida, j2, 1).status());
        Resposta fechada = get("/partidas/" + partida + "/estado");
        assertTrue(fechada.corpo().contains("\"empatePropostoPor\":null"), fechada.corpo());
        assertTrue(fechada.corpo().contains("\"empatesUsados\":[" + j1 + "]"), fechada.corpo());
    }

    @Test
    void partidaTerminadaNaJogadaTemMotivoDeVitoria() {
        long j1 = criarJogador("EmpateO");
        long j2 = criarJogador("EmpateP");
        long partida = novaPartida("CLASSICO", j1, j2);
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        assertEquals(201, jogar(partida, j1, 0).status());

        Resposta estado = get("/partidas/" + partida + "/estado");

        assertTrue(estado.corpo().contains("\"motivoFim\":\"VITORIA\""), estado.corpo());
        assertTrue(estado.corpo().contains("\"vencedorId\":" + j1), estado.corpo());
    }
}