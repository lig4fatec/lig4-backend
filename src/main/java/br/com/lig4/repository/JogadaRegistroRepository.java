package br.com.lig4.repository;

import br.com.lig4.domain.JogadaRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface JogadaRegistroRepository extends JpaRepository<JogadaRegistro, Long> {

    List<JogadaRegistro> findByPartidaIdOrderByNumeroAsc(Long partidaId);

    /** Cada linha é {partidaId, total de jogadas}. Partidas sem jogadas não aparecem. */
    @Query("""
            select j.partida.id, count(j) from JogadaRegistro j
            where j.partida.id in :ids
            group by j.partida.id
            """)
    List<Object[]> contarPorPartida(@Param("ids") Collection<Long> ids);
}