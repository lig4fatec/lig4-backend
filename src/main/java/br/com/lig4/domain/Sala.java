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
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "sala")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 6)
    public String pin;

    @Column(nullable = false)
    public int serie;

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

    /** Vitórias na série atual. Empate não pontua. */
    @Column(name = "vitorias_dono", nullable = false)
    public int vitoriasDono;

    @Column(name = "vitorias_convidado", nullable = false)
    public int vitoriasConvidado;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
        if (this.fase == null) {
            this.fase = FaseSala.AGUARDANDO;
        }
    }

    /** Melhor de N: vence quem chegar primeiro à maioria (série 1 → 1, série 3 → 2, série 5 → 3). */
    public int vitoriasParaVencer() {
        return serie / 2 + 1;
    }

    public void registrarVitoria(Long jogadorId) {
        if (dono.id.equals(jogadorId)) {
            vitoriasDono++;
        } else if (convidado != null && convidado.id.equals(jogadorId)) {
            vitoriasConvidado++;
        }
    }

    /** Id do jogador que venceu a série, ou nulo se ela ainda não foi decidida. */
    public Long campeaoId() {
        if (vitoriasDono >= vitoriasParaVencer()) {
            return dono.id;
        }
        if (convidado != null && vitoriasConvidado >= vitoriasParaVencer()) {
            return convidado.id;
        }
        return null;
    }

    public boolean serieDecidida() {
        return campeaoId() != null;
    }

    public void zerarPlacar() {
        vitoriasDono = 0;
        vitoriasConvidado = 0;
    }

    /** Vitórias por id de jogador, com o dono primeiro. */
    public Map<Long, Integer> placar() {
        Map<Long, Integer> placar = new LinkedHashMap<>();
        placar.put(dono.id, vitoriasDono);
        if (convidado != null) {
            placar.put(convidado.id, vitoriasConvidado);
        }
        return placar;
    }
}