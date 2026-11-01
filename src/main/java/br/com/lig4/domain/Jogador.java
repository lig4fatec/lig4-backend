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

    /** Também é o nick do perfil: é o nome que aparece nas partidas. */
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

    @Column(length = 7)
    public String cor;

    /** Hash BCrypt da senha. Nunca sai em nenhuma resposta da API. */
    @Column(name = "senha_hash", length = 100)
    public String senhaHash;

    @Column(name = "nome_completo", length = 100)
    public String nomeCompleto;

    @Column(length = 280)
    public String bio;

    /** Saldo da loja. Cresce ao fim de cada partida (ver RecompensaService). */
    @Column(nullable = false)
    public int moedas = 500;

    // itens equipados na loja (um por categoria)
    @Column(name = "avatar_equipado", nullable = false, length = 40)
    public String avatarEquipado = "avatar-classico";

    @Column(name = "tabuleiro_equipado", nullable = false, length = 40)
    public String tabuleiroEquipado = "board-minimalista";

    @Column(name = "emote_equipado", nullable = false, length = 40)
    public String emoteEquipado = "emote-saudacao";
}