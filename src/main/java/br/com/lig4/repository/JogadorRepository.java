package br.com.lig4.repository;

import br.com.lig4.domain.Jogador;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class JogadorRepository implements PanacheRepository<Jogador> {

    public Optional<Jogador> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
