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

/** Um item pago da loja que o jogador comprou. */
@Entity
@Table(name = "jogador_item")
public class ItemJogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jogador_id", nullable = false)
    public Jogador jogador;

    @Column(name = "item_id", nullable = false, length = 40)
    public String itemId;

    @Column(name = "comprado_em", nullable = false)
    public Instant compradoEm;

    @PrePersist
    public void prePersist() {
        this.compradoEm = Instant.now();
    }
}
