# PRD-0015. Perfil, estatísticas e moedas do jogador

- **Data:** 2026-11-01
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a tela de perfil do front-end (nick, nome, bio, cor, vitórias e derrotas) e preparar o saldo de moedas usado pela loja.

## Requisitos

- `GET /jogadores/{id}/perfil` devolve `id`, `email`, `nick`, `nomeCompleto`, `bio`, `cor`, `vitorias`, `derrotas` e `moedas`. Sem cor salva, a cor é `blue`.
- `PUT /jogadores/{id}/perfil` atualiza `nick` (obrigatório, até 20), `nomeCompleto` (até 100), `bio` (até 280) e `cor` (um dos 7 nomes das salas). Erros: `NICK_INVALIDO`, `COR_INVALIDA`, `DADOS_INVALIDOS`, `JOGADOR_NAO_ENCONTRADO`.
- O nick é gravado em `nome`, o nome mostrado nas partidas e nas salas.
- Vitórias e derrotas são contadas das partidas finalizadas com vencedor. Empate não conta como vitória nem derrota.
- Cada jogador começa com 500 moedas. Ao fim da partida, por vitória +50 e por derrota ou empate +10. O crédito acontece uma única vez, tanto pelas jogadas quanto por `PUT /partidas/{id}`.
- Migration V8 com `nome_completo`, `bio` e `moedas`.

## Fora do escopo

- Partida abandonada continua sem ser finalizada, então não conta nem dá moedas.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (88 testes, ou 89 se o PRD-0013 já foi aplicado).
- Após uma vitória, o vencedor tem 1 vitória e 550 moedas, e o perdedor tem 1 derrota e 510 moedas.