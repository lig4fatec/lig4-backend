package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.Partida;
import org.springframework.stereotype.Service;

/** Credita as moedas quando uma partida termina. Deve ser chamado uma única vez por partida. */
@Service
public class RecompensaService {

    public static final int MOEDAS_VITORIA = 50;
    public static final int MOEDAS_DERROTA = 10; // vale também para empate

    /** Chame dentro da transação que finaliza a partida; os jogadores são salvos junto com ela. */
    public void conceder(Partida partida) {
        if (partida.vencedor == null) {
            partida.jogadorUm.moedas += MOEDAS_DERROTA;
            partida.jogadorDois.moedas += MOEDAS_DERROTA;
            return;
        }
        Jogador perdedor = partida.vencedor.id.equals(partida.jogadorUm.id)
                ? partida.jogadorDois
                : partida.jogadorUm;
        partida.vencedor.moedas += MOEDAS_VITORIA;
        perdedor.moedas += MOEDAS_DERROTA;
    }
}