package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.dto.CadastroRequestDTO;
import br.com.lig4.dto.JogadorResponseDTO;
import br.com.lig4.dto.LoginRequestDTO;
import br.com.lig4.dto.TrocarEmailRequestDTO;
import br.com.lig4.dto.TrocarSenhaRequestDTO;
import br.com.lig4.exception.CredenciaisInvalidasException;
import br.com.lig4.exception.EmailJaCadastradoException;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final int TAMANHO_MAXIMO_NICK = 20;

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
        jogador.nome = apelidoEscolhido(dto.nick(), email);
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

     /** Sem apelido no cadastro, o nick começa como a parte antes do @ (o perfil muda depois). */
    private String nomeInicial(String email) {
        int arroba = email.indexOf('@');
        String base = arroba > 0 ? email.substring(0, arroba) : email;
        return base.length() > TAMANHO_MAXIMO_NICK ? base.substring(0, TAMANHO_MAXIMO_NICK) : base;
    }

    /** Usa o apelido escolhido no cadastro, se veio; senão, o nome inicial derivado do e-mail. */
    private String apelidoEscolhido(String nick, String email) {
        if (nick != null && !nick.isBlank()) {
            return nick.trim();
        }
        return nomeInicial(email);
    }

        /** Troca a senha, conferindo a senha atual. Mesma resposta de login para senha errada. */
    @Transactional
    public void trocarSenha(Long jogadorId, TrocarSenhaRequestDTO dto) {
        Jogador jogador = repository.findById(jogadorId)
                .orElseThrow(() -> new JogadorNotFoundException(jogadorId));
        if (jogador.senhaHash == null || !encoder.matches(dto.senhaAtual(), jogador.senhaHash)) {
            throw new CredenciaisInvalidasException();
        }
        jogador.senhaHash = encoder.encode(dto.senhaNova());
    }

    /** Troca o e-mail, conferindo a senha e garantindo que o novo e-mail não está em uso por outro jogador. */
    @Transactional
    public JogadorResponseDTO trocarEmail(Long jogadorId, TrocarEmailRequestDTO dto) {
        Jogador jogador = repository.findById(jogadorId)
                .orElseThrow(() -> new JogadorNotFoundException(jogadorId));
        if (jogador.senhaHash == null || !encoder.matches(dto.senha(), jogador.senhaHash)) {
            throw new CredenciaisInvalidasException();
        }
        String email = normalizar(dto.email());
        repository.findByEmail(email)
                .filter(outro -> !outro.id.equals(jogadorId))
                .ifPresent(outro -> {
                    throw new EmailJaCadastradoException(email);
                });
        jogador.email = email;
        return JogadorResponseDTO.from(jogador);
    }
}