package br.com.lig4.repository;

import br.com.lig4.domain.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaRepository extends JpaRepository<Partida, Long> {
}
