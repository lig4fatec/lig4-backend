package br.com.lig4.repository;

import br.com.lig4.domain.JogadaRegistro;
import br.com.lig4.domain.TipoJogada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface JogadaRegistroRepository extends JpaRepository<JogadaRegistro, Long> {

    List<JogadaRegistro> findByPartidaIdOrderByNumeroAsc(Long partidaId);

    @Query("""
            select j.partida.id, count(j) from JogadaRegistro j
            where j.partida.id in :ids and j.tipo = :tipo
            group by j.partida.id
            """)
    List<Object[]> contarPorPartidaETipo(@Param("ids") Collection<Long> ids, @Param("tipo") TipoJogada tipo);

    /** Cada linha é {partidaId, total de peças jogadas} (poderes não contam). Partidas sem jogadas não aparecem. */
    default List<Object[]> contarPorPartida(Collection<Long> ids) {
        return contarPorPartidaETipo(ids, TipoJogada.PECA);
    }
}