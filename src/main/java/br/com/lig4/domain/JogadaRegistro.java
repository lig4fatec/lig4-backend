package br.com.lig4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "jogada")
public class JogadaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    public Partida partida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jogador_id", nullable = false)
    public Jogador jogador;

    /** Ordem da jogada na partida: 1, 2, 3... */
    @Column(nullable = false)
    public int numero;

    @Column(nullable = false)
    public int coluna;

    @Column(nullable = false)
    public int linha;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
    }
}
