package br.com.lig4.repository;

import br.com.lig4.domain.ItemJogador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemJogadorRepository extends JpaRepository<ItemJogador, Long> {

    List<ItemJogador> findByJogadorId(Long jogadorId);

    boolean existsByJogadorIdAndItemId(Long jogadorId, String itemId);
}