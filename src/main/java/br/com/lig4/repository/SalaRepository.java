package br.com.lig4.repository;

import br.com.lig4.domain.Sala;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    Optional<Sala> findByPin(String pin);

    /** A sala que originou a partida. Fica vazio para partidas criadas direto por POST /partidas. */
    Optional<Sala> findByPartidaId(Long partidaId);

    boolean existsByPin(String pin);

    /** Trava a sala enquanto alguém entra, para dois convidados não ocuparem a mesma vaga. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.pin = :pin")
    Optional<Sala> buscarPorPinComLock(@Param("pin") String pin);
}