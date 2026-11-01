package br.com.lig4.service;

import br.com.lig4.domain.FaseSala;
import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.Sala;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.dto.SalaAcaoRequestDTO;
import br.com.lig4.dto.SalaCorRequestDTO;
import br.com.lig4.dto.SalaCriarRequestDTO;
import br.com.lig4.dto.SalaEntrarRequestDTO;
import br.com.lig4.dto.SalaResponseDTO;
import br.com.lig4.exception.CodigoErro;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.SalaInvalidaException;
import br.com.lig4.exception.SalaNotFoundException;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import br.com.lig4.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SalaService {

    private static final Pattern PIN = Pattern.compile("^\\d{6}$");
    private static final Set<Integer> SERIES_VALIDAS = Set.of(1, 3, 5);
    private static final int TENTATIVAS_DE_PIN = 20;

    private final SalaRepository salaRepository;
    private final JogadorRepository jogadorRepository;
    private final PartidaRepository partidaRepository;
    private final SecureRandom random = new SecureRandom();

    public SalaService(SalaRepository salaRepository,
                       JogadorRepository jogadorRepository,
                       PartidaRepository partidaRepository) {
        this.salaRepository = salaRepository;
        this.jogadorRepository = jogadorRepository;
        this.partidaRepository = partidaRepository;
    }

    @Transactional
    public SalaResponseDTO criar(SalaCriarRequestDTO dto) {
        int serie = dto.serie() == null ? 1 : dto.serie();
        if (!SERIES_VALIDAS.contains(serie)) {
            throw new SalaInvalidaException(CodigoErro.SERIE_INVALIDA, "A série deve ser 1, 3 ou 5");
        }

        Sala sala = new Sala();
        sala.dono = buscarJogadorOuFalhar(dto.jogadorId());
        sala.corDono = dto.cor();
        sala.serie = serie;
        sala.fase = FaseSala.AGUARDANDO;
        sala.pin = gerarPin();
        salaRepository.save(sala);

        return SalaResponseDTO.from(sala);
    }

    /**
     * Entra na sala pelo PIN. Quem já está na sala (dono ou convidado) recebe a situação atual,
     * o que também serve para voltar à sala depois de recarregar a página.
     */
    @Transactional
    public SalaResponseDTO entrar(SalaEntrarRequestDTO dto) {
        Sala sala = buscarComLock(dto.pin());
        Jogador jogador = buscarJogadorOuFalhar(dto.jogadorId());

        if (ehDono(sala, jogador.id) || ehConvidado(sala, jogador.id)) {
            return SalaResponseDTO.from(sala);
        }

        if (sala.fase != FaseSala.AGUARDANDO || sala.convidado != null) {
            throw new SalaInvalidaException(CodigoErro.SALA_CHEIA, "A sala já está cheia ou em andamento");
        }
        if (dto.cor().equals(sala.corDono)) {
            throw new SalaInvalidaException(CodigoErro.COR_JA_ESCOLHIDA,
                    "Essa cor já foi escolhida pelo dono da sala");
        }

        sala.convidado = jogador;
        sala.corConvidado = dto.cor();

        return SalaResponseDTO.from(sala);
    }

    @Transactional(readOnly = true)
    public SalaResponseDTO consultar(String pin) {
        String valido = validarPin(pin);
        return SalaResponseDTO.from(salaRepository.findByPin(valido)
                .orElseThrow(() -> new SalaNotFoundException(valido)));
    }

    /** Troca a cor de quem está na sala, enquanto a partida não começou. */
    @Transactional
    public SalaResponseDTO trocarCor(String pin, SalaCorRequestDTO dto) {
        Sala sala = buscarComLock(pin);
        boolean dono = ehDono(sala, dto.jogadorId());
        if (!dono && !ehConvidado(sala, dto.jogadorId())) {
            throw new SalaInvalidaException(CodigoErro.SEM_PERMISSAO, "Este jogador não está na sala");
        }
        exigirAguardando(sala);

        String corDoOutro = dono ? sala.corConvidado : sala.corDono;
        if (dto.cor().equals(corDoOutro)) {
            throw new SalaInvalidaException(CodigoErro.COR_JA_ESCOLHIDA,
                    "Essa cor já foi escolhida pelo outro jogador");
        }

        if (dono) {
            sala.corDono = dto.cor();
        } else {
            sala.corConvidado = dto.cor();
        }
        return SalaResponseDTO.from(sala);
    }

    /** Só o dono inicia, e só com o convidado na sala. O dono joga como jogador 1 e começa. */
    @Transactional
    public SalaResponseDTO iniciar(String pin, SalaAcaoRequestDTO dto) {
        Sala sala = buscarComLock(pin);
        if (!ehDono(sala, dto.jogadorId())) {
            throw new SalaInvalidaException(CodigoErro.SEM_PERMISSAO, "Só o dono da sala pode iniciar a partida");
        }
        exigirAguardando(sala);
        if (sala.convidado == null) {
            throw new SalaInvalidaException(CodigoErro.SEM_CONVIDADO, "Aguarde o segundo jogador entrar na sala");
        }

        Partida partida = new Partida();
        partida.jogadorUm = sala.dono;
        partida.jogadorDois = sala.convidado;
        partida.status = StatusPartida.EM_ANDAMENTO;
        partidaRepository.save(partida);

        sala.partida = partida;
        sala.fase = FaseSala.JOGANDO;
        return SalaResponseDTO.from(sala);
    }

    /**
     * Sai da sala. No lobby, o convidado libera a vaga e, se o dono sair, a sala deixa de existir.
     * Com a partida já iniciada, a sala é encerrada.
     */
    @Transactional
    public void sair(String pin, SalaAcaoRequestDTO dto) {
        Sala sala = buscarComLock(pin);
        boolean dono = ehDono(sala, dto.jogadorId());
        if (!dono && !ehConvidado(sala, dto.jogadorId())) {
            throw new SalaInvalidaException(CodigoErro.SEM_PERMISSAO, "Este jogador não está na sala");
        }

        if (sala.fase == FaseSala.AGUARDANDO) {
            if (dono) {
                salaRepository.delete(sala);
            } else {
                sala.convidado = null;
                sala.corConvidado = null;
            }
        } else {
            sala.fase = FaseSala.ENCERRADA;
        }
    }

    private Sala buscarComLock(String pin) {
        String valido = validarPin(pin);
        return salaRepository.buscarPorPinComLock(valido)
                .orElseThrow(() -> new SalaNotFoundException(valido));
    }

    private void exigirAguardando(Sala sala) {
        if (sala.fase != FaseSala.AGUARDANDO) {
            throw new SalaInvalidaException(CodigoErro.FASE_INVALIDA,
                    "Esta ação só é possível enquanto a sala aguarda o início da partida");
        }
    }

    private boolean ehDono(Sala sala, Long jogadorId) {
        return sala.dono.id.equals(jogadorId);
    }

    private boolean ehConvidado(Sala sala, Long jogadorId) {
        return sala.convidado != null && sala.convidado.id.equals(jogadorId);
    }

    private String validarPin(String pin) {
        if (pin == null || !PIN.matcher(pin).matches()) {
            throw new SalaInvalidaException(CodigoErro.PIN_INVALIDO, "O PIN deve ter exatamente 6 dígitos");
        }
        return pin;
    }

    private String gerarPin() {
        for (int i = 0; i < TENTATIVAS_DE_PIN; i++) {
            String pin = String.valueOf(100000 + random.nextInt(900000));
            if (!salaRepository.existsByPin(pin)) {
                return pin;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um PIN único");
    }

    private Jogador buscarJogadorOuFalhar(Long id) {
        return jogadorRepository.findById(id)
                .orElseThrow(() -> new JogadorNotFoundException(id));
    }
}