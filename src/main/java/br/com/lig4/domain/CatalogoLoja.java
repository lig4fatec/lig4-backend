package br.com.lig4.domain;

import java.util.List;
import java.util.Optional;

/** Catálogo fixo da loja, com os mesmos ids do front-end. Itens de preço 0 são de todos. */
public final class CatalogoLoja {

    public record Item(String id, String categoria, String nome, int preco) {
    }

    public record Categoria(String id, String nome, List<Item> itens) {
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
                    new Item("emote-vitoria", "emote", "Vitória", 400))));

    private CatalogoLoja() {
    }

    public static Optional<Item> buscar(String itemId) {
        return CATEGORIAS.stream()
                .flatMap(categoria -> categoria.itens().stream())
                .filter(item -> item.id().equals(itemId))
                .findFirst();
    }
}
