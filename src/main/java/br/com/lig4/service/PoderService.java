package br.com.lig4.service;

import br.com.lig4.domain.JogadaRegistro;
import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.Partida;
import br.com.lig4.domain.StatusPartida;
import br.com.lig4.domain.TipoJogada;
import br.com.lig4.dto.PartidaEstadoDTO;
import br.com.lig4.dto.PoderRequestDTO;
import br.com.lig4.engine.Disco;
import br.com.lig4.engine.JogadaInvalidaException;
import br.com.lig4.engine.Jogo;
import br.com.lig4.engine.Poder;
import br.com.lig4.exception.AcaoPartidaInvalidaException;
import br.com.lig4.exception.CodigoErro;
import br.com.lig4.exception.JogadorNotFoundException;
import br.com.lig4.exception.PartidaNotFoundException;
import br.com.lig4.repository.ItemJogadorRepository;
import br.com.lig4.repository.JogadaRegistroRepository;
import br.com.lig4.repository.JogadorRepository;
import br.com.lig4.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Poderes do modo Caos: Queimar Peça, Congelar Coluna e Peça Extra. O poder é gravado na mesma tabela
 * das jogadas, como um evento, e a partida continua sendo refeita jogada por jogada.
 */
@Service
public class PoderService {

    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadaRegistroRepository jogadaRepository;
    private final ItemJogadorRepository itensRepository;
    private final JogadaService jogadaService;
    private final RelogioPartida relogio;

    public PoderService(PartidaRepository partidaRepository,
                        JogadorRepository jogadorRepository,
                        JogadaRegistroRepository jogadaRepository,
                        ItemJogadorRepository itensRepository,
                        JogadaService jogadaService,
                        RelogioPartida relogio) {
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadaRepository = jogadaRepository;
        this.itensRepository = itensRepository;
        this.jogadaService = jogadaService;
        this.relogio = relogio;
    }

    @Transactional
    public PartidaEstadoDTO usar(Long partidaId, PoderRequestDTO dto) {
        Partida partida = partidaRepository.buscarComLock(partidaId)
                .orElseThrow(() -> new PartidaNotFoundException(partidaId));
        if (partida.status == StatusPartida.FINALIZADA) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "A partida já terminou.");
        }

        Disco disco = jogadaService.discoDe(partida, dto.jogadorId());
        Poder poder = lerPoder(dto.poder());
        if (partida.modo != ModoJogo.CAOS) {
            throw new AcaoPartidaInvalidaException(CodigoErro.MODO_SEM_PODERES,
                    "Os poderes só existem no modo Caos.");
        }
        if (!itensRepository.existsByJogadorIdAndItemId(dto.jogadorId(), poder.itemId())) {
            throw new AcaoPartidaInvalidaException(CodigoErro.PODER_NAO_POSSUIDO,
                    "Você ainda não comprou este poder na loja.");
        }
        exigirAlvo(poder, dto);

        Instant agora = Instant.now();
        if (relogio.ativo(partida) && relogio.restante(partida, dto.jogadorId(), agora) <= 0) {
            throw new JogadaInvalidaException(JogadaInvalidaException.Motivo.JOGO_ENCERRADO,
                    "O seu tempo acabou.");
        }

        List<JogadaRegistro> anteriores = jogadaRepository.findByPartidaIdOrderByNumeroAsc(partidaId);
        Jogo jogo = jogadaService.reproduzir(partida, anteriores);

        int coluna = 0;
        int linha = 0;
        switch (poder) {
            case QUEIMAR -> {
                coluna = dto.coluna();
                linha = dto.linha();
                jogo.queimar(disco, coluna, linha);
            }
            case CONGELAR -> {
                coluna = dto.coluna();
                jogo.congelar(disco, coluna);
            }
            case EXTRA -> jogo.pecaExtra(disco);
        }

        Jogador jogador = jogadorRepository.findById(dto.jogadorId())
                .orElseThrow(() -> new JogadorNotFoundException(dto.jogadorId()));
        JogadaRegistro registro = new JogadaRegistro();
        registro.partida = partida;
        registro.jogador = jogador;
        registro.numero = anteriores.size() + 1;
        registro.tipo = TipoJogada.valueOf(poder.name());
        registro.coluna = coluna;
        registro.linha = linha;
        jogadaRepository.save(registro);

        // usar um poder depois de receber uma proposta de empate é recusá-la, como jogar
        if (partida.empatePendenteDe != null && !partida.empatePendenteDe.id.equals(jogador.id)) {
            partida.empatePendenteDe = null;
        }

        // Queimar e Congelar gastam a vez: o relógio passa para quem joga agora. A Peça Extra não.
        if (poder != Poder.EXTRA) {
            Jogador proximo = jogo.getTurno() == Disco.VERMELHO ? partida.jogadorUm : partida.jogadorDois;
            relogio.passar(partida, jogador, proximo, agora);
        }

        return jogadaService.estado(partidaId);
    }

    private Poder lerPoder(String texto) {
        try {
            return Poder.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new AcaoPartidaInvalidaException(CodigoErro.PODER_INVALIDO,
                    "O poder deve ser QUEIMAR, CONGELAR ou EXTRA.");
        }
    }

    private void exigirAlvo(Poder poder, PoderRequestDTO dto) {
        boolean faltaColuna = dto.coluna() == null;
        boolean faltaLinha = dto.linha() == null;
        if ((poder == Poder.QUEIMAR && (faltaColuna || faltaLinha))) {
            throw new AcaoPartidaInvalidaException(CodigoErro.PODER_INVALIDO,
                    "Para queimar, informe a coluna e a linha da peça (linha 0 é a base).");
        }
        if (poder == Poder.CONGELAR && faltaColuna) {
            throw new AcaoPartidaInvalidaException(CodigoErro.PODER_INVALIDO,
                    "Para congelar, informe a coluna.");
        }
    }
}
