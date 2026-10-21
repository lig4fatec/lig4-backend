package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.JogadorRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.exception.EmailJaCadastradoException;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JogadorService {

    private final JogadorRepository repository;

    public JogadorService(JogadorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<JogadorResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(JogadorResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JogadorResponseDTO buscarPorId(Long id) {
        Jogador jogador = repository.findById(id)
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
        repository.save(jogador);

        return JogadorResponseDTO.from(jogador);
    }

    @Transactional
    public JogadorResponseDTO atualizar(Long id, JogadorRequestDTO dto) {
        Jogador jogador = repository.findById(id)
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
        if (!repository.existsById(id)) {
            throw new JogadorNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
