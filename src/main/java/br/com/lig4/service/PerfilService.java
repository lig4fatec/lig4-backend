package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.PerfilRequestDTO;
import br.com.lig4.dto.PerfilResponseDTO;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final JogadorRepository jogadores;
    private final PartidaRepository partidas;

    public PerfilService(JogadorRepository jogadores, PartidaRepository partidas) {
        this.jogadores = jogadores;
        this.partidas = partidas;
    }

    @Transactional(readOnly = true)
    public PerfilResponseDTO buscar(Long id) {
        return montar(buscarOuFalhar(id));
    }

    @Transactional
    public PerfilResponseDTO atualizar(Long id, PerfilRequestDTO dto) {
        Jogador jogador = buscarOuFalhar(id);
        jogador.nome = dto.nick().trim(); // o nick é o nome mostrado nas partidas
        jogador.nomeCompleto = dto.nomeCompleto() == null ? null : dto.nomeCompleto().trim();
        jogador.bio = dto.bio() == null ? null : dto.bio().trim();
        jogador.cor = dto.cor();
        return montar(jogador);
    }

    private PerfilResponseDTO montar(Jogador jogador) {
        return PerfilResponseDTO.from(jogador,
                partidas.countByVencedorId(jogador.id),
                partidas.derrotasDe(jogador.id));
    }

    private Jogador buscarOuFalhar(Long id) {
        return jogadores.findById(id).orElseThrow(() -> new JogadorNotFoundException(id));
    }
}
