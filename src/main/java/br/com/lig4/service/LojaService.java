package br.com.lig4.service;

import br.com.lig4.domain.CatalogoLoja;
import br.com.lig4.domain.ItemJogador;
import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.LojaCompraRequestDTO;
import br.com.lig4.dto.LojaEquiparRequestDTO;
import br.com.lig4.dto.LojaResponseDTO;
import br.com.lig4.exception.CodigoErro;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.LojaInvalidaException;
import br.com.lig4.repository.ItemJogadorRepository;
import br.com.lig4.repository.JogadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LojaService {

    private final JogadorRepository jogadores;
    private final ItemJogadorRepository itens;

    public LojaService(JogadorRepository jogadores, ItemJogadorRepository itens) {
        this.jogadores = jogadores;
        this.itens = itens;
    }

    @Transactional(readOnly = true)
    public LojaResponseDTO ver(Long jogadorId) {
        Jogador jogador = jogadores.findById(jogadorId)
                .orElseThrow(() -> new JogadorNotFoundException(jogadorId));
        return montar(jogador);
    }

    @Transactional
    public LojaResponseDTO comprar(Long jogadorId, LojaCompraRequestDTO dto) {
        Jogador jogador = jogadores.buscarComLock(jogadorId)
                .orElseThrow(() -> new JogadorNotFoundException(jogadorId));
        CatalogoLoja.Item item = CatalogoLoja.buscar(dto.itemId())
                .orElseThrow(() -> itemInexistente(dto.itemId()));

        if (possui(jogador, item)) {
            throw new LojaInvalidaException(CodigoErro.ITEM_JA_POSSUIDO, "Você já possui este item");
        }
        if (jogador.moedas < item.preco()) {
            throw new LojaInvalidaException(CodigoErro.SALDO_INSUFICIENTE,
                    "Moedas insuficientes: o item custa " + item.preco() + " e você tem " + jogador.moedas);
        }

        jogador.moedas -= item.preco();
        ItemJogador comprado = new ItemJogador();
        comprado.jogador = jogador;
        comprado.itemId = item.id();
        itens.save(comprado);

        return montar(jogador);
    }

    @Transactional
    public LojaResponseDTO equipar(Long jogadorId, LojaEquiparRequestDTO dto) {
        Jogador jogador = jogadores.buscarComLock(jogadorId)
                .orElseThrow(() -> new JogadorNotFoundException(jogadorId));
        CatalogoLoja.Item item = CatalogoLoja.buscar(dto.itemId())
                .filter(encontrado -> encontrado.categoria().equals(dto.categoria()))
                .orElseThrow(() -> itemInexistente(dto.itemId()));

        if (!possui(jogador, item)) {
            throw new LojaInvalidaException(CodigoErro.ITEM_NAO_POSSUIDO,
                    "Este item não está na sua coleção");
        }

        switch (item.categoria()) {
            case "avatar" -> jogador.avatarEquipado = item.id();
            case "board" -> jogador.tabuleiroEquipado = item.id();
            default -> jogador.emoteEquipado = item.id();
        }

        return montar(jogador);
    }

    private boolean possui(Jogador jogador, CatalogoLoja.Item item) {
        return item.preco() == 0 || itens.existsByJogadorIdAndItemId(jogador.id, item.id());
    }

    private LojaInvalidaException itemInexistente(String itemId) {
        return new LojaInvalidaException(CodigoErro.ITEM_INEXISTENTE, "Item inexistente: " + itemId);
    }

    private LojaResponseDTO montar(Jogador jogador) {
        Set<String> comprados = itens.findByJogadorId(jogador.id).stream()
                .map(comprado -> comprado.itemId)
                .collect(Collectors.toSet());

        Map<String, String> equipados = new LinkedHashMap<>();
        equipados.put("avatar", jogador.avatarEquipado);
        equipados.put("board", jogador.tabuleiroEquipado);
        equipados.put("emote", jogador.emoteEquipado);

        List<LojaResponseDTO.Categoria> categorias = CatalogoLoja.CATEGORIAS.stream()
                .map(categoria -> new LojaResponseDTO.Categoria(
                        categoria.id(),
                        categoria.nome(),
                        categoria.itens().stream()
                                .map(item -> new LojaResponseDTO.Item(
                                        item.id(),
                                        item.nome(),
                                        item.preco(),
                                        item.preco() == 0 || comprados.contains(item.id()),
                                        item.id().equals(equipados.get(categoria.id()))))
                                .toList()))
                .toList();

        return new LojaResponseDTO(jogador.moedas, equipados, categorias);
    }
}