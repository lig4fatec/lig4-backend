package br.com.lig4.repository;

import br.com.lig4.domain.Jogador;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JogadorRepository extends JpaRepository<Jogador, Long> {

    Optional<Jogador> findByEmail(String email);

    /** Trava a linha do jogador para duas compras ao mesmo tempo não gastarem as mesmas moedas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from Jogador j where j.id = :id")
    Optional<Jogador> buscarComLock(@Param("id") Long id);
}