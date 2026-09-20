package br.com.lig4.repository;

import br.com.lig4.domain.Partida;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PartidaRepository implements PanacheRepository<Partida> {
}
