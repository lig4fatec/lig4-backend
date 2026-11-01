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
    void lojaNovaTem12ItensSaldoInicialEItensGratisEquipados() {
        long id = criarJogador("Loja");

        Resposta r = loja(id);

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":500"), r.corpo());
        assertEquals(12, contar(r.corpo(), "\"preco\":"), r.corpo());
        assertTrue(r.corpo().contains(
                "\"equipados\":{\"avatar\":\"avatar-classico\",\"board\":\"board-minimalista\",\"emote\":\"emote-saudacao\"}"),
                r.corpo());
        assertEquals(3, contar(r.corpo(), "\"preco\":0,\"possuido\":true,\"equipado\":true"), r.corpo());
    }

    @Test
    void deveComprarUmItemDescontandoAsMoedas() {
        long id = criarJogador("Compra");

        Resposta r = comprar(id, "avatar-folhas");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"moedas\":320"), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"avatar-folhas\",\"nome\":\"Folhas\",\"preco\":180,\"possuido\":true,\"equipado\":false"),
                r.corpo());
        // o perfil mostra o mesmo saldo
        assertTrue(get("/jogadores/" + id + "/perfil").corpo().contains("\"moedas\":320"));
    }

    @Test
    void naoDeveComprarOMesmoItemDuasVezes() {
        long id = criarJogador("Repete");
        assertEquals(200, comprar(id, "emote-boa-partida").status());

        esperar(comprar(id, "emote-boa-partida"), 409, "ITEM_JA_POSSUIDO");
        esperar(comprar(id, "avatar-classico"), 409, "ITEM_JA_POSSUIDO"); // grátis já é de todos
    }

    @Test
    void naoDeveComprarSemSaldoENemCobrar() {
        long id = criarJogador("Pobre");

        esperar(comprar(id, "avatar-dourado"), 409, "SALDO_INSUFICIENTE"); // custa 520, tem 500

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
        comprar(id, "avatar-folhas");

        Resposta r = equipar(id, "avatar", "avatar-folhas");

        assertEquals(200, r.status(), r.corpo());
        assertTrue(r.corpo().contains("\"avatar\":\"avatar-folhas\""), r.corpo());
        assertTrue(r.corpo().contains(
                "\"id\":\"avatar-folhas\",\"nome\":\"Folhas\",\"preco\":180,\"possuido\":true,\"equipado\":true"),
                r.corpo());
    }

    @Test
    void naoDeveEquiparItemNaoComprado() {
        long id = criarJogador("SemItem");

        esperar(equipar(id, "avatar", "avatar-orbita"), 409, "ITEM_NAO_POSSUIDO");
    }

    @Test
    void naoDeveEquiparItemDeOutraCategoria() {
        long id = criarJogador("Categoria");

        esperar(equipar(id, "board", "avatar-classico"), 404, "ITEM_INEXISTENTE");
        esperar(equipar(id, "categoria-falsa", "avatar-classico"), 404, "ITEM_INEXISTENTE");
    }

    @Test
    void jogadorInexistenteDevolve404() {
        esperar(loja(999999999L), 404, "JOGADOR_NAO_ENCONTRADO");
        esperar(comprar(999999999L, "avatar-folhas"), 404, "JOGADOR_NAO_ENCONTRADO");
    }
}
