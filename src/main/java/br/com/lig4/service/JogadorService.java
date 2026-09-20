package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.JogadorRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.exception.EmailJaCadastradoException;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class JogadorService {

    @Inject
    JogadorRepository repository;

    public List<JogadorResponseDTO> listarTodos() {
        return repository.listAll().stream()
                .map(JogadorResponseDTO::from)
                .toList();
    }

    public JogadorResponseDTO buscarPorId(Long id) {
        Jogador jogador = repository.findByIdOptional(id)
                .orElseThrow(() -> new JogadorNotFoundException(id));
        return JogadorResponseDTO.from(jogador);
    }

    @Transactional
    public JogadorResponseDTO criar(JogadorRequestDTO dto) {
        repository.findByEmail(dto.email()).ifPresent(j -> {
            throw new EmailJaCadastradoException(dto.email());
        });

        Jogador jogador = new Jogador();
        jogador.nome = dto.nome();
        jogador.email = dto.email();
        repository.persist(jogador);

        return JogadorResponseDTO.from(jogador);
    }

    @Transactional
    public JogadorResponseDTO atualizar(Long id, JogadorRequestDTO dto) {
        Jogador jogador = repository.findByIdOptional(id)
                .orElseThrow(() -> new JogadorNotFoundException(id));

        repository.findByEmail(dto.email())
                .filter(outro -> !outro.id.equals(id))
                .ifPresent(j -> {
                    throw new EmailJaCadastradoException(dto.email());
                });

        jogador.nome = dto.nome();
        jogador.email = dto.email();

        return JogadorResponseDTO.from(jogador);
    }

    @Transactional
    public void remover(Long id) {
        boolean deletado = repository.deleteById(id);
        if (!deletado) {
            throw new JogadorNotFoundException(id);
        }
    }
}
