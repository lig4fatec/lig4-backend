package br.com.lig4.service;

import br.com.lig4.domain.Jogador;
import br.com.lig4.domain.ModoJogo;
import br.com.lig4.domain.Partida;
import org.springframework.stereotype.Service;

/** Credita as moedas quando uma partida termina. Deve ser chamado uma única vez por partida. */
@Service
public class RecompensaService {

    public static final int CLASSICO_VITORIA = 15;
    public static final int CLASSICO_EMPATE = 5;
    public static final int CLASSICO_DERROTA = 2;

    public static final int CAOS_VITORIA = 30;
    public static final int CAOS_EMPATE = 10;
    public static final int CAOS_DERROTA = 5;

    /** Chame dentro da transação que finaliza a partida; os jogadores são salvos junto com ela. */
    public void conceder(Partida partida) {
        boolean caos = partida.modo == ModoJogo.CAOS;
        if (partida.vencedor == null) {
            int empate = caos ? CAOS_EMPATE : CLASSICO_EMPATE;
            partida.jogadorUm.moedas += empate;
            partida.jogadorDois.moedas += empate;
            return;
        }
        Jogador perdedor = partida.vencedor.id.equals(partida.jogadorUm.id)
                ? partida.jogadorDois
                : partida.jogadorUm;
        partida.vencedor.moedas += caos ? CAOS_VITORIA : CLASSICO_VITORIA;
        perdedor.moedas += caos ? CAOS_DERROTA : CLASSICO_DERROTA;
    }
}