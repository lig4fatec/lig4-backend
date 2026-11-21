package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Poderes do modo Caos pela API: Queimar Peça, Congelar Coluna e Peça Extra. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PoderesTest extends ApiTestSupport {

    @LocalServerPort
    int porta;

    @Override
    protected int porta() {
        return porta;
    }

    private void comprar(long jogador, String item) {
        Resposta r = enviar("POST", "/jogadores/" + jogador + "/loja/comprar", "{\"itemId\":\"" + item + "\"}");
        assertEquals(200, r.status(), r.corpo());
    }

    private long partida(long um, long dois, String modo) {
        Resposta r = enviar("POST", "/partidas",
                "{\"jogadorUmId\":%d,\"jogadorDoisId\":%d,\"modo\":\"%s\"}".formatted(um, dois, modo));
        assertEquals(201, r.status(), r.corpo());
        return r.id();
    }

    private Resposta jogar(long partida, long jogador, int coluna) {
        return enviar("POST", "/partidas/" + partida + "/jogadas",
                "{\"jogadorId\":%d,\"coluna\":%d}".formatted(jogador, coluna));
    }

    private Resposta poder(long partida, long jogador, String poder, Integer coluna, Integer linha) {
        StringBuilder corpo = new StringBuilder("{\"jogadorId\":%d,\"poder\":\"%s\"".formatted(jogador, poder));
        if (coluna != null) {
            corpo.append(",\"coluna\":").append(coluna);
        }
        if (linha != null) {
            corpo.append(",\"linha\":").append(linha);
        }
        return enviar("POST", "/partidas/" + partida + "/poderes", corpo.append("}").toString());
    }

    private void esperar(Resposta r, int status, String codigo) {
        assertEquals(status, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"codigo\":\"" + codigo + "\""), r.corpo());
    }

    @Test
    void poderNoModoClassicoNaoExiste() {
        long j1 = criarJogador("PoderA");
        long j2 = criarJogador("PoderB");
        comprar(j1, "congelar-coluna");
        long partida = partida(j1, j2, "CLASSICO");

        esperar(poder(partida, j1, "CONGELAR", 3, null), 409, "MODO_SEM_PODERES");
    }

    @Test
    void semComprarNaoSePodeUsar() {
        long j1 = criarJogador("PoderC");
        long j2 = criarJogador("PoderD");
        long partida = partida(j1, j2, "CAOS");

        esperar(poder(partida, j1, "CONGELAR", 3, null), 409, "PODER_NAO_POSSUIDO");
    }

    @Test
    void poderDesconhecidoOuSemAlvoDevolve400() {
        long j1 = criarJogador("PoderE");
        long j2 = criarJogador("PoderF");
        comprar(j1, "queimar-peca");
        comprar(j1, "congelar-coluna");
        long partida = partida(j1, j2, "CAOS");

        esperar(poder(partida, j1, "VOAR", null, null), 400, "PODER_INVALIDO");
        esperar(poder(partida, j1, "QUEIMAR", 0, null), 400, "PODER_INVALIDO");
        esperar(poder(partida, j1, "CONGELAR", null, null), 400, "PODER_INVALIDO");
    }

    @Test
    void congelarBloqueiaAColunaDoAdversarioEPassaAVez() {
        long j1 = criarJogador("PoderG");
        long j2 = criarJogador("PoderH");
        comprar(j1, "congelar-coluna");
        long partida = partida(j1, j2, "CAOS");

        Resposta r = poder(partida, j1, "CONGELAR", 3, null);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"congelamentos\":[{\"coluna\":3,\"jogadorId\":" + j2
                + ",\"turnosRestantes\":3}]"), r.corpo());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j2), r.corpo());
        assertTrue(r.corpo().contains("\"poder\":\"CONGELAR\""), r.corpo());

        esperar(jogar(partida, j2, 3), 409, "COLUNA_CONGELADA");
        Resposta jogada = jogar(partida, j2, 0);
        assertEquals(201, jogada.status(), jogada.corpo());
        assertTrue(jogada.corpo().contains("\"turnosRestantes\":2"), jogada.corpo());
        assertEquals(201, jogar(partida, j1, 3).status()); // quem congelou joga na coluna normalmente
    }

    @Test
    void pecaExtraDaDuasJogadasSeguidas() {
        long j1 = criarJogador("PoderI");
        long j2 = criarJogador("PoderJ");
        comprar(j1, "peca-extra");
        long partida = partida(j1, j2, "CAOS");

        Resposta r = poder(partida, j1, "EXTRA", null, null);
        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"pecaExtraAtiva\":true"), r.corpo());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j1), r.corpo());

        Resposta primeira = jogar(partida, j1, 0);
        assertEquals(201, primeira.status(), primeira.corpo());
        assertTrue(primeira.corpo().contains("\"proximoJogadorId\":" + j1), primeira.corpo());
        assertTrue(primeira.corpo().contains("\"pecaExtraAtiva\":true"), primeira.corpo());

        Resposta segunda = jogar(partida, j1, 1);
        assertEquals(201, segunda.status(), segunda.corpo());
        assertTrue(segunda.corpo().contains("\"proximoJogadorId\":" + j2), segunda.corpo());
        assertTrue(segunda.corpo().contains("\"pecaExtraAtiva\":false"), segunda.corpo());

        esperar(jogar(partida, j1, 2), 409, "FORA_DO_TURNO");
    }

    @Test
    void queimarRemoveAPecaDoAdversarioEPassaAVez() {
        long j1 = criarJogador("PoderK");
        long j2 = criarJogador("PoderL");
        comprar(j2, "queimar-peca");
        long partida = partida(j1, j2, "CAOS");
        assertEquals(201, jogar(partida, j1, 0).status());

        Resposta r = poder(partida, j2, "QUEIMAR", 0, 0);

        assertEquals(200, r.status(), r.corpo());
        assertFalse(r.corpo().contains("VERMELHO"), r.corpo());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j1), r.corpo());
        assertTrue(r.corpo().contains("\"totalJogadas\":1"), r.corpo());
    }

    @Test
    void queimarSoVaiEmPecaDoAdversario() {
        long j1 = criarJogador("PoderM");
        long j2 = criarJogador("PoderN");
        comprar(j1, "queimar-peca");
        long partida = partida(j1, j2, "CAOS");

        esperar(poder(partida, j1, "QUEIMAR", 0, 0), 409, "ALVO_INVALIDO"); // primeiro lance: não há peça
        assertEquals(201, jogar(partida, j1, 0).status());
        assertEquals(201, jogar(partida, j2, 1).status());
        esperar(poder(partida, j1, "QUEIMAR", 0, 0), 409, "ALVO_INVALIDO"); // peça do próprio jogador
    }

    @Test
    void poderForaDaVezEUmaVezSo() {
        long j1 = criarJogador("PoderO");
        long j2 = criarJogador("PoderP");
        comprar(j1, "congelar-coluna");
        comprar(j2, "congelar-coluna");
        long partida = partida(j1, j2, "CAOS");

        esperar(poder(partida, j2, "CONGELAR", 1, null), 409, "FORA_DO_TURNO");
        assertEquals(200, poder(partida, j1, "CONGELAR", 3, null).status());
        assertEquals(201, jogar(partida, j2, 0).status());
        esperar(poder(partida, j1, "CONGELAR", 4, null), 409, "PODER_JA_USADO");
    }

    @Test
    void estadoMostraPoderesDisponiveisEUsados() {
        long j1 = criarJogador("PoderQ");
        long j2 = criarJogador("PoderR");
        comprar(j1, "congelar-coluna");
        comprar(j1, "peca-extra");
        long partida = partida(j1, j2, "CAOS");

        String antes = get("/partidas/" + partida + "/estado").corpo();
        assertTrue(antes.contains("\"poderesDisponiveis\":[{\"jogadorId\":" + j1 + ",\"poder\":\"CONGELAR\"}"), antes);
        assertTrue(antes.contains("\"poderesUsados\":[]"), antes);

        poder(partida, j1, "CONGELAR", 2, null);

        String depois = get("/partidas/" + partida + "/estado").corpo();
        assertTrue(depois.contains("\"poderesUsados\":[{\"jogadorId\":" + j1 + ",\"poder\":\"CONGELAR\"}]"), depois);
        assertFalse(depois.contains("\"poderesDisponiveis\":[{\"jogadorId\":" + j1 + ",\"poder\":\"CONGELAR\""), depois);
        assertTrue(depois.contains("\"poder\":\"EXTRA\""), depois);
    }

    @Test
    void historicoRegistraOPoderUsado() {
        long j1 = criarJogador("PoderS");
        long j2 = criarJogador("PoderT");
        comprar(j1, "congelar-coluna");
        long partida = partida(j1, j2, "CAOS");
        poder(partida, j1, "CONGELAR", 4, null);
        jogar(partida, j2, 0);

        String historico = get("/partidas/" + partida + "/jogadas").corpo();

        assertTrue(historico.contains("\"tipo\":\"CONGELAR\""), historico);
        assertTrue(historico.contains("\"tipo\":\"PECA\""), historico);
    }

        @Test
    void queimarFazAsPecasDeCimaDescerem() {
        long j1 = criarJogador("PoderU");
        long j2 = criarJogador("PoderV");
        comprar(j1, "queimar-peca");
        long partida = partida(j1, j2, "CAOS");
        assertEquals(201, jogar(partida, j1, 0).status()); // V na base da col0
        assertEquals(201, jogar(partida, j2, 1).status()); // A na base da col1
        assertEquals(201, jogar(partida, j1, 1).status()); // V acima na col1
        assertEquals(201, jogar(partida, j2, 6).status()); // A em outra coluna; a vez volta a j1

        Resposta r = poder(partida, j1, "QUEIMAR", 1, 0); // queima o A da base da col1; o V desce

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"statusPartida\":\"EM_ANDAMENTO\""), r.corpo());
        assertTrue(r.corpo().contains("\"proximoJogadorId\":" + j2), r.corpo());

        Resposta drop = jogar(partida, j2, 1); // a próxima peça cai por cima do V que desceu
        assertEquals(201, drop.status(), drop.corpo());
        assertTrue(drop.corpo().contains("\"linha\":1"), drop.corpo());
    }
}
