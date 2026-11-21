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
                    new Item("avatar-classico", "avatar", "Clássico", 0),
                    new Item("avatar-folhas", "avatar", "Folhas", 180),
                    new Item("avatar-orbita", "avatar", "Órbita", 360),
                    new Item("avatar-dourado", "avatar", "Dourado", 520))),
            new Categoria("board", "Tabuleiros", List.of(
                    new Item("board-minimalista", "board", "Minimalista", 0),
                    new Item("board-cobalto", "board", "Cobalto", 160),
                    new Item("board-linhas", "board", "Linhas", 340),
                    new Item("board-noturno", "board", "Noturno", 480))),
            new Categoria("emote", "Emotes", List.of(
                    new Item("emote-saudacao", "emote", "Saudação", 0),
                    new Item("emote-boa-partida", "emote", "Boa partida", 100),
                    new Item("emote-concentracao", "emote", "Concentração", 230),
                    new Item("emote-vitoria", "emote", "Vitória", 400))),
            // Poderes do modo Caos: comprados uma vez, ficam para sempre (cada um vale 1 uso por partida).
            new Categoria("poder", "Poderes", List.of(
                    new Item("poder-queimar", "poder", "Queimar Peça", 100),
                    new Item("poder-congelar", "poder", "Congelar Coluna", 100),
                    new Item("poder-extra", "poder", "Peça Extra", 100))));

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