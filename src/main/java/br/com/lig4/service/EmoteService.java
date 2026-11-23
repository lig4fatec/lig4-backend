package br.com.lig4.service;

import br.com.lig4.domain.CatalogoLoja;
import br.com.lig4.dto.EmoteDTO;
import br.com.lig4.exception.AcaoPartidaInvalidaException;
import br.com.lig4.exception.CodigoErro;
import org.springframework.stereotype.Service;

/**
 * Emotes durante a partida. O emote é só visual: qualquer participante pode mandar um emote válido
 * da loja, e ele é transmitido aos dois jogadores. Não custa nem dá nada, então não exige posse.
 */
@Service
public class EmoteService {

    private final JogadaService jogadaService;

    public EmoteService(JogadaService jogadaService) {
        this.jogadaService = jogadaService;
    }

    /** Confere que o jogador participa da partida e que o emote existe; devolve o que será transmitido. */
    public EmoteDTO validar(Long partidaId, Long jogadorId, String emoteId) {
        jogadaService.validarParticipante(partidaId, jogadorId);
        CatalogoLoja.buscar(emoteId)
                .filter(item -> item.categoria().equals("emote"))
                .orElseThrow(() -> new AcaoPartidaInvalidaException(CodigoErro.EMOTE_INVALIDO,
                        "Emote inválido: " + emoteId));
        return new EmoteDTO(jogadorId, emoteId);
    }
}