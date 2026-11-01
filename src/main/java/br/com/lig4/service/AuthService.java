package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.CadastroRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.dto.LoginRequestDTO;
import br.com.lig4.exception.CredenciaisInvalidasException;
import br.com.lig4.exception.EmailJaCadastradoException;
import br.com.lig4.repository.JogadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final JogadorRepository repository;
    private final PasswordEncoder encoder;

    public AuthService(JogadorRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public JogadorResponseDTO cadastrar(CadastroRequestDTO dto) {
        String email = normalizar(dto.email());
        if (repository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException(email);
        }

        Jogador jogador = new Jogador();
        jogador.email = email;
        jogador.nome = nomeInicial(email);
        jogador.senhaHash = encoder.encode(dto.senha());
        repository.save(jogador);

        return JogadorResponseDTO.from(jogador);
    }

    @Transactional(readOnly = true)
    public JogadorResponseDTO entrar(LoginRequestDTO dto) {
        Jogador jogador = repository.findByEmail(normalizar(dto.email())).orElse(null);
        if (jogador == null || jogador.senhaHash == null
                || !encoder.matches(dto.senha(), jogador.senhaHash)) {
            // mesma resposta para e-mail inexistente e senha errada
            throw new CredenciaisInvalidasException();
        }
        return JogadorResponseDTO.from(jogador);
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** O front só envia e-mail e senha; o nome começa como a parte antes do @ (o perfil muda depois). */
    private String nomeInicial(String email) {
        int arroba = email.indexOf('@');
        return arroba > 0 ? email.substring(0, arroba) : email;
    }
}
