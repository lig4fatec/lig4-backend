package br.com.lig4.domain;

import java.util.List;
import java.util.Optional;

/** Catálogo fixo da loja, com os mesmos ids do front-end. Itens de preço 0 são de todos. */
public final class CatalogoLoja {

    public record Item(String id, String categoria, String nome, int preco) {
    }

    public record Categoria(String id, String nome, List<Item> itens) {
    }

    /** Pacote de moedas vendido na loja. A compra é simulada: não há cobrança real. */
    public record Pacote(String id, int moedas, int precoReais) {
    }

    public static final List<Categoria> CATEGORIAS = List.of(
            new Categoria("avatar", "Avatares", List.of(
                    new Item("avatar-padrao", "avatar", "Padrão", 0),
                    new Item("avatar-samurai", "avatar", "Samurai", 180),
                    new Item("avatar-robo", "avatar", "Robô", 180),
                    new Item("avatar-rei", "avatar", "Rei", 360),
                    new Item("avatar-rainha", "avatar", "Rainha", 360),
                    new Item("avatar-mago", "avatar", "Mago", 520))),
            new Categoria("board", "Tabuleiros", List.of(
                    new Item("board-classico", "board", "Clássico", 0),
                    new Item("board-espacial", "board", "Espacial", 160),
                    new Item("board-floresta", "board", "Floresta", 160),
                    new Item("board-medieval", "board", "Medieval", 340),
                    new Item("board-praia", "board", "Praia", 340),
                    new Item("board-inverno", "board", "Inverno", 480))),
            new Categoria("emote", "Emotes", List.of(
                    new Item("emote-risadinha", "emote", "Risadinha", 0),
                    new Item("emote-aplauso", "emote", "Aplauso", 100),
                    new Item("emote-choro", "emote", "Choro", 100),
                    new Item("emote-surpresa", "emote", "Surpresa", 100),
                    new Item("emote-bravo", "emote", "Bravo", 100),
                    new Item("emote-cool", "emote", "Cool", 230),
                    new Item("emote-coracao", "emote", "Coração", 230),
                    new Item("emote-confuso", "emote", "Confuso", 230))),
            // Poderes do modo Caos: comprados uma vez, ficam para sempre (cada um vale 1 uso por partida).
            new Categoria("poderes", "Poderes", List.of(
                    new Item("congelar-coluna", "poderes", "Congelar Coluna", 100),
                    new Item("queimar-peca", "poderes", "Queimar Peça", 100),
                    new Item("peca-extra", "poderes", "Peça Extra", 100))));

    public static final List<Pacote> PACOTES = List.of(
            new Pacote("pacote-100", 100, 5),
            new Pacote("pacote-500", 500, 20),
            new Pacote("pacote-1000", 1000, 40));

    private CatalogoLoja() {
    }

    public static Optional<Pacote> buscarPacote(String pacoteId) {
        return PACOTES.stream()
                .filter(pacote -> pacote.id().equals(pacoteId))
                .findFirst();
    }

    public static Optional<Item> buscar(String itemId) {
        return CATEGORIAS.stream()
                .flatMap(categoria -> categoria.itens().stream())
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }
}