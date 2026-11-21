package br.com.lig4.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LojaControllerTest extends ApiTestSupport {

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

    private Resposta loja(long jogador) {
        return get("/jogadores/" + jogador + "/loja");
    }

    private Resposta comprar(long jogador, String item) {
        return enviar("POST", "/jogadores/" + jogador + "/loja/comprar", "{\"itemId\":\"%s\"}".formatted(item));
    }

    private Resposta comprarMoedas(long jogador, String pacote) {
        return enviar("POST", "/jogadores/" + jogador + "/loja/moedas", "{\"pacoteId\":\"%s\"}".formatted(pacote));
    }

    private Resposta equipar(long jogador, String categoria, String item) {
        return enviar("PUT", "/jogadores/" + jogador + "/loja/equipar",
                "{\"categoria\":\"%s\",\"itemId\":\"%s\"}".formatted(categoria, item));
    }

    private int contar(String texto, String trecho) {
        int total = 0;
        for (int i = texto.indexOf(trecho); i >= 0; i = texto.indexOf(trecho, i + 1)) {
            total++;
        }
        return total;
    }

    @Test
    void lojaNovaTem23ItensSaldoInicialEItensGratisEquipados() {
        long id = criarJogador("Loja");

        Resposta r = loja(id);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":500"), r.corpo());
        assertEquals(23, contar(r.corpo(), "\"preco\":"), r.corpo());
        assertTrue(r.corpo().contains(
                "\"equipados\":{\"avatar\":\"avatar-padrao\",\"board\":\"board-classico\",\"emote\":\"emote-risadinha\"}"),
                r.corpo());
        assertEquals(3, contar(r.corpo(), "\"preco\":0,\"possuido\":true,\"equipado\":true"), r.corpo());
    }

    @Test
    void deveComprarUmItemDescontandoAsMoedas() {
        long id = criarJogador("Compra");

        Resposta r = comprar(id, "avatar-samurai");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":320"), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"avatar-samurai\",\"nome\":\"Samurai\",\"preco\":180,\"possuido\":true,\"equipado\":false"),
                r.corpo());
        // o perfil mostra o mesmo saldo
        assertTrue(get("/jogadores/" + id + "/perfil").corpo().contains("\"moedas\":320"));
    }

    @Test
    void naoDeveComprarOMesmoItemDuasVezes() {
        long id = criarJogador("Repete");
        assertEquals(200, comprar(id, "emote-aplauso").status());

        esperar(comprar(id, "emote-aplauso"), 409, "ITEM_JA_POSSUIDO");
        esperar(comprar(id, "avatar-padrao"), 409, "ITEM_JA_POSSUIDO"); // grátis já é de todos
    }

    @Test
    void naoDeveComprarSemSaldoENemCobrar() {
        long id = criarJogador("Pobre");

        esperar(comprar(id, "avatar-mago"), 409, "SALDO_INSUFICIENTE"); // custa 520, tem 500

        assertTrue(loja(id).corpo().contains("\"moedas\":500"));
    }

    @Test
    void naoDeveComprarItemInexistente() {
        long id = criarJogador("Fantasma");

        esperar(comprar(id, "item-que-nao-existe"), 404, "ITEM_INEXISTENTE");
    }

    @Test
    void deveEquiparUmItemComprado() {
        long id = criarJogador("Equipa");
        comprar(id, "avatar-samurai");

        Resposta r = equipar(id, "avatar", "avatar-samurai");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"avatar\":\"avatar-samurai\""), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"avatar-samurai\",\"nome\":\"Samurai\",\"preco\":180,\"possuido\":true,\"equipado\":true"),
                r.corpo());
    }

    @Test
    void naoDeveEquiparItemNaoComprado() {
        long id = criarJogador("SemItem");

        esperar(equipar(id, "avatar", "avatar-robo"), 409, "ITEM_NAO_POSSUIDO");
    }

    @Test
    void naoDeveEquiparItemDeOutraCategoria() {
        long id = criarJogador("Categoria");

        esperar(equipar(id, "board", "avatar-padrao"), 404, "ITEM_INEXISTENTE");
        esperar(equipar(id, "categoria-falsa", "avatar-padrao"), 404, "ITEM_INEXISTENTE");
    }

    @Test
    void lojaTemACategoriaDePoderesComTresItensDeCemMoedas() {
        long id = criarJogador("Poderes");

        Resposta r = loja(id);

        assertTrue(r.corpo().contains("\"id\":\"poderes\",\"nome\":\"Poderes\""), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"congelar-coluna\",\"nome\":\"Congelar Coluna\",\"preco\":100,\"possuido\":false,\"equipado\":false"),
                r.corpo());
        assertTrue(r.corpo().contains("\"id\":\"queimar-peca\",\"nome\":\"Queimar Peça\",\"preco\":100"),
                r.corpo());
        assertTrue(r.corpo().contains("\"id\":\"peca-extra\",\"nome\":\"Peça Extra\",\"preco\":100"),
                r.corpo());
    }

    @Test
    void deveComprarUmPoderEMantelo() {
        long id = criarJogador("CompraPoder");

        Resposta r = comprar(id, "queimar-peca");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":400"), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"queimar-peca\",\"nome\":\"Queimar Peça\",\"preco\":100,\"possuido\":true,\"equipado\":false"),
                r.corpo());
        esperar(comprar(id, "queimar-peca"), 409, "ITEM_JA_POSSUIDO");
    }

    @Test
    void poderNaoSeEquipa() {
        long id = criarJogador("EquipaPoder");
        assertEquals(200, comprar(id, "peca-extra").status());

        esperar(equipar(id, "poderes", "peca-extra"), 404, "ITEM_INEXISTENTE");
    }

    @Test
    void lojaListaOsTresPacotesDeMoedas() {
        long id = criarJogador("Pacotes");

        Resposta r = loja(id);

        assertTrue(r.corpo().contains("\"pacotes\":[{\"id\":\"pacote-100\",\"moedas\":100,\"precoReais\":5}"),
                r.corpo());
        assertTrue(r.corpo().contains("{\"id\":\"pacote-500\",\"moedas\":500,\"precoReais\":20}"), r.corpo());
        assertTrue(r.corpo().contains("{\"id\":\"pacote-1000\",\"moedas\":1000,\"precoReais\":40}"), r.corpo());
    }

    @Test
    void compraSimuladaDePacoteSomaAsMoedas() {
        long id = criarJogador("Moedas");

        assertTrue(comprarMoedas(id, "pacote-100").corpo().contains("\"moedas\":600"));
        assertTrue(comprarMoedas(id, "pacote-500").corpo().contains("\"moedas\":1100"));
        Resposta r = comprarMoedas(id, "pacote-1000");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":2100"), r.corpo());
        // o perfil mostra o mesmo saldo
        assertTrue(get("/jogadores/" + id + "/perfil").corpo().contains("\"moedas\":2100"));
    }

    @Test
    void moedasCompradasPermitemComprarOQueAntesNaoCabia() {
        long id = criarJogador("Rico");
        esperar(comprar(id, "avatar-mago"), 409, "SALDO_INSUFICIENTE"); // custa 520, tem 500

        assertEquals(200, comprarMoedas(id, "pacote-100").status());
        Resposta r = comprar(id, "avatar-mago");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":80"), r.corpo());
    }

    @Test
    void pacoteInexistenteEhRecusadoESemCobrarNada() {
        long id = criarJogador("SemPacote");

        esperar(comprarMoedas(id, "pacote-9999"), 404, "PACOTE_INEXISTENTE");

        assertTrue(loja(id).corpo().contains("\"moedas\":500"));
    }

    @Test
    void compraDePacoteSemOIdEhRecusada() {
        long id = criarJogador("SemId");

        esperar(enviar("POST", "/jogadores/" + id + "/loja/moedas", "{}"), 400, "DADOS_INVALIDOS");
    }

    @Test
    void jogadorInexistenteDevolve404() {
        esperar(loja(999999999L), 404, "JOGADOR_NAO_ENCONTRADO");
        esperar(comprar(999999999L, "avatar-samurai"), 404, "JOGADOR_NAO_ENCONTRADO");
        esperar(comprarMoedas(999999999L, "pacote-100"), 404, "JOGADOR_NAO_ENCONTRADO");
    }
}