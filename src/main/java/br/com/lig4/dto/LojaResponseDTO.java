package br.com.lig4.dto;

import java.util.List;
import java.util.Map;

public record LojaResponseDTO(
        int moedas,
        Map<String, String> equipados,
        List<Categoria> categorias,
        List<Pacote> pacotes
) {
    public record Categoria(String id, String nome, List<Item> itens) {
    }

    public record Item(String id, String nome, int preco, boolean possuido, boolean equipado) {
    }

    /** Pacote de moedas à venda (compra simulada); o preço é em reais. */
    public record Pacote(String id, int moedas, int precoReais) {
    }
}