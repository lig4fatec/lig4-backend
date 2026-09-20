package br.com.lig4.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "partida")
public class Partida extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_um_id", nullable = false)
    public Jogador jogadorUm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogador_dois_id", nullable = false)
    public Jogador jogadorDois;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vencedor_id")
    public Jogador vencedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPartida status;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @Column(name = "finalizado_em")
    public Instant finalizadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
        if (this.status == null) {
            this.status = StatusPartida.EM_ANDAMENTO;
        }
    }
}