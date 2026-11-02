package br.com.lig4.dto;

import br.com.lig4.domain.FaseSala;
import br.com.lig4.domain.Sala;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record SalaResponseDTO(
        String pin,
        int serie,
        FaseSala fase,
        SalaJogadorDTO dono,
        SalaJogadorDTO convidado,
        Long partidaId,
        /** Ids dos jogadores que já pediram a revanche. */
        List<Long> revancheDe,
        /** Vitórias na série atual, por id de jogador. */
        Map<Long, Integer> placar,
        /** Verdadeiro quando alguém já chegou à maioria de vitórias da série. */
        boolean serieEncerrada,
        /** Quem venceu a série; nulo enquanto ela não foi decidida. */
        Long campeaoId
) {
    public static SalaResponseDTO from(Sala sala) {
        SalaJogadorDTO dono = new SalaJogadorDTO(sala.dono.id, sala.dono.nome, sala.corDono);
        SalaJogadorDTO convidado = sala.convidado == null ? null
                : new SalaJogadorDTO(sala.convidado.id, sala.convidado.nome, sala.corConvidado);
        Long partidaId = sala.partida == null ? null : sala.partida.id;

        List<Long> revancheDe = new ArrayList<>();
        if (sala.revancheDono) {
            revancheDe.add(sala.dono.id);
        }
        if (sala.revancheConvidado && sala.convidado != null) {
            revancheDe.add(sala.convidado.id);
        }
        return new SalaResponseDTO(sala.pin, sala.serie, sala.fase, dono, convidado, partidaId, revancheDe,
                sala.placar(), sala.serieDecidida(), sala.campeaoId());
    }
}