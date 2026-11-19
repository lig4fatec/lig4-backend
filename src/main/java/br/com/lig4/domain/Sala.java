package br.com.lig4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "sala")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 6)
    public String pin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public ModoJogo modo = ModoJogo.CLASSICO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public FaseSala fase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dono_id", nullable = false)
    public Jogador dono;

    @Column(name = "cor_dono", nullable = false, length = 20)
    public String corDono;

    @ManyToOne
    @JoinColumn(name = "convidado_id")
    public Jogador convidado;

    @Column(name = "cor_convidado", length = 20)
    public String corConvidado;

    /** A partida em andamento ou a última que terminou. */
    @ManyToOne
    @JoinColumn(name = "partida_id")
    public Partida partida;

    @Column(name = "revanche_dono", nullable = false)
    public boolean revancheDono;

    @Column(name = "revanche_convidado", nullable = false)
    public boolean revancheConvidado;

    /** "Pronto" no lobby. Quando os dois marcam, a partida começa e as marcas voltam a falso. */
    @Column(name = "pronto_dono", nullable = false)
    public boolean prontoDono;

    @Column(name = "pronto_convidado", nullable = false)
    public boolean prontoConvidado;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
        if (this.fase == null) {
            this.fase = FaseSala.AGUARDANDO;
        }
    }
}