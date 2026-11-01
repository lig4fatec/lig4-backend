package br.com.lig4.repository;

import br.com.lig4.domain.Partida;
import br.com.lig4.domain.StatusPartida;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    int LIMITE_HISTORICO = 50;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Partida p where p.id = :id")
    Optional<Partida> buscarComLock(@Param("id") Long id);

    /** Partidas vencidas pelo jogador (o vencedor só é gravado quando a partida termina). */
    long countByVencedorId(Long jogadorId);

    @Query("""
            select count(p) from Partida p
            where p.status = :status
              and p.vencedor is not null
              and p.vencedor.id <> :jogadorId
              and (p.jogadorUm.id = :jogadorId or p.jogadorDois.id = :jogadorId)
            """)
    long contarDerrotas(@Param("jogadorId") Long jogadorId, @Param("status") StatusPartida status);

    default long derrotasDe(Long jogadorId) {
        return contarDerrotas(jogadorId, StatusPartida.FINALIZADA);
    }

    @Query("""
            select p from Partida p
            where p.status = :status
              and (p.jogadorUm.id = :jogadorId or p.jogadorDois.id = :jogadorId)
            order by p.finalizadoEm desc, p.id desc
            """)
    List<Partida> buscarFinalizadasDoJogador(@Param("jogadorId") Long jogadorId,
                                             @Param("status") StatusPartida status,
                                             Pageable pagina);

    /** As partidas finalizadas mais recentes do jogador, da mais nova para a mais antiga. */
    default List<Partida> finalizadasDoJogador(Long jogadorId) {
        return buscarFinalizadasDoJogador(jogadorId, StatusPartida.FINALIZADA,
                PageRequest.of(0, LIMITE_HISTORICO));
    }
}