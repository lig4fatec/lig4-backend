package br.com.lig4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "jogador")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(nullable = false, unique = true, length = 100)
    public String email;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
    }
}
