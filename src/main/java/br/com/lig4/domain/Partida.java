package br.com.lig4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jogador_um_id", nullable = false)
    public Jogador jogadorUm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jogador_dois_id", nullable = false)
    public Jogador jogadorDois;

    @ManyToOne
    @JoinColumn(name = "vencedor_id")
    public Jogador vencedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPartida status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public ModoJogo modo = ModoJogo.CLASSICO;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_fim", length = 20)
    public MotivoFim motivoFim;

    /** Quem propôs um empate que o adversário ainda não respondeu. */
    @ManyToOne
    @JoinColumn(name = "empate_pendente_de_id")
    public Jogador empatePendenteDe;

    /** Cada jogador pode propor empate uma vez por partida. */
    @Column(name = "empate_usado_um", nullable = false)
    public boolean empateUsadoUm;

    @Column(name = "empate_usado_dois", nullable = false)
    public boolean empateUsadoDois;

    /** Tempo que cada jogador ainda tem, em milissegundos, na última vez em que o relógio foi marcado. */
    @Column(name = "tempo_um_ms", nullable = false)
    public long tempoUmMs = 300_000L;

    @Column(name = "tempo_dois_ms", nullable = false)
    public long tempoDoisMs = 300_000L;

    /** De quem é o relógio que está correndo (o jogador da vez). Vazio em partida sem relógio ou terminada. */
    @ManyToOne
    @JoinColumn(name = "relogio_de_id")
    public Jogador relogioDe;

    @Column(name = "relogio_marcado_em")
    public Instant relogioMarcadoEm;

    /** Quando o tempo de quem está na vez acaba, se ele não jogar. */
    @Column(name = "prazo_em")
    public Instant prazoEm;

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