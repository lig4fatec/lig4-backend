package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.dto.PartidaFinalizarDTO;
import br.com.lig4.dto.PartidaRequestDTO;
import br.com.lig4.dto.PartidaResponseDTO;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.PartidaInvalidaException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PartidaService {

    @Inject
    PartidaRepository partidaRepository;

    @Inject
    JogadorRepository jogadorRepository;

    public List<PartidaResponseDTO> listarTodas() {
        return partidaRepository.listAll().stream()
                .map(PartidaResponseDTO::from)
                .toList();
    }

    public PartidaResponseDTO buscarPorId(Long id) {
        return PartidaResponseDTO.from(buscarOuFalhar(id));
    }

    @Transactional
    public PartidaResponseDTO criar(PartidaRequestDTO dto) {
        if (dto.jogadorUmId().equals(dto.jogadorDoisId())) {
            throw new PartidaInvalidaException("Os dois jogadores da partida devem ser diferentes");
        }

        Jogador jogadorUm = buscarJogadorOuFalhar(dto.jogadorUmId());
        Jogador jogadorDois = buscarJogadorOuFalhar(dto.jogadorDoisId());

        Partida partida = new Partida();
        partida.jogadorUm = jogadorUm;
        partida.jogadorDois = jogadorDois;
        partida.status = StatusPartida.EM_ANDAMENTO;
        partidaRepository.persist(partida);

        return PartidaResponseDTO.from(partida);
    }

    @Transactional
    public PartidaResponseDTO finalizar(Long id, PartidaFinalizarDTO dto) {
        Partida partida = buscarOuFalhar(id);

        if (partida.status == StatusPartida.FINALIZADA) {
            throw new PartidaInvalidaException("Esta partida já está finalizada");
        }

        if (dto.vencedorId() != null) {
            boolean vencedorValido = dto.vencedorId().equals(partida.jogadorUm.id)
                    || dto.vencedorId().equals(partida.jogadorDois.id);
            if (!vencedorValido) {
                throw new PartidaInvalidaException("O vencedor precisa ser um dos jogadores desta partida");
            }
            partida.vencedor = buscarJogadorOuFalhar(dto.vencedorId());
        }

        partida.status = StatusPartida.FINALIZADA;
        partida.finalizadoEm = Instant.now();

        return PartidaResponseDTO.from(partida);
    }

    @Transactional
    public void remover(Long id) {
        boolean deletado = partidaRepository.deleteById(id);
        if (!deletado) {
            throw new PartidaNotFoundException(id);
        }
    }

    private Partida buscarOuFalhar(Long id) {
        return partidaRepository.findByIdOptional(id)
                .orElseThrow(() -> new PartidaNotFoundException(id));
    }

    private Jogador buscarJogadorOuFalhar(Long id) {
        return jogadorRepository.findByIdOptional(id)
                .orElseThrow(() -> new JogadorNotFoundException(id));
    }
}
