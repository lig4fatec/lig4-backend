package br.com.lig4.repository;

import br.com.lig4.domain.JogadaRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JogadaRegistroRepository extends JpaRepository<JogadaRegistro, Long> {

    List<JogadaRegistro> findByPartidaIdOrderByNumeroAsc(Long partidaId);
}
