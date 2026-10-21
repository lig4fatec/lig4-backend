package br.com.lig4.repository;

import br.com.lig4.domain.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JogadorRepository extends JpaRepository<Jogador, Long> {

    Optional<Jogador> findByEmail(String email);
}
