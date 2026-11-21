# PRD-0027. Catálogo da loja alinhado ao front

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Ao integrar o front ao back, a loja só funciona se os dois usarem os mesmos ids de item (o front monta as imagens e descrições a partir do id). O catálogo do back estava com ids de rascunho que não existiam no front. Este PRD adota no back os ids, nomes e itens do catálogo do front.

## Requisitos

- Categorias e itens com os ids do front:
  - avatares: `avatar-padrao` (grátis), `-samurai`, `-robo`, `-rei`, `-rainha`, `-mago`;
  - tabuleiros: `board-classico` (grátis), `-espacial`, `-floresta`, `-medieval`, `-praia`, `-inverno`;
  - emotes: `emote-risadinha` (grátis), `-aplauso`, `-choro`, `-surpresa`, `-bravo`, `-cool`, `-coracao`, `-confuso`;
  - poderes (categoria `poderes`): `congelar-coluna`, `queimar-peca`, `peca-extra`.
- Itens equipados por padrão passam a ser `avatar-padrao`, `board-classico`, `emote-risadinha`.
- O id do item de cada poder (`Poder.itemId()`) passa a ser o id do front (`queimar-peca`, `congelar-coluna`, `peca-extra`).
- Migration V16: muda o padrão das colunas de itens equipados, atualiza os jogadores que estavam no padrão antigo e remove itens comprados com ids que saíram do catálogo.

## Decisões (e o que ficou para o grupo decidir)

- **Economia mantida como no back:** saldo inicial 500, poderes a 100 moedas e recompensas (Clássico 15/5/2, Caos 30/10/5) continuam iguais. O back é a fonte da verdade dos preços: depois de integrado, o front mostra os preços que vierem do back. Os preços e o saldo inicial que o front tem hoje no mock (1250 de saldo, poderes a 700/850/1000) **não** foram adotados — isso é uma decisão de economia do jogo, que o grupo deve bater. Se quiserem os números do front, é um ajuste rápido de preços (e das recompensas, se for o caso).
- Preços dos itens novos: mantidos em faixas que cabem no saldo de 500 (avatares 180/360/520, tabuleiros 160/340/480, emotes 100/230).

## Fora do escopo

- Integração em si (o front ainda chama serviços simulados em `localStorage`).
- Imagens e descrições dos itens (ficam no front).

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- `GET /jogadores/{id}/loja` devolve as 4 categorias com os ids do front e os 3 pacotes.
- Comprar e equipar funcionam com os ids novos; poder não se equipa.