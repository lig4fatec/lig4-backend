# PRD-0023. Compra simulada de moedas e poderes na loja

- **Data:** 2026-11-21
- **Estado:** Implementado
- **Desenvolvedor** Thamires

## Objetivo

Atender a decisão de 12/11 (compra de moedas em pacotes, simulada) e a conversa de 18/11 sobre ter os poderes à venda na loja.

## Requisitos

- `POST /jogadores/{id}/loja/moedas` com `{pacoteId}` soma as moedas do pacote ao saldo e devolve a loja atualizada. A compra é simulada: não há cobrança nem gateway de pagamento.
- Pacotes: `pacote-100` (100 moedas, R$ 5), `pacote-500` (500 moedas, R$ 20) e `pacote-1000` (1000 moedas, R$ 40). A loja (`GET /jogadores/{id}/loja`) lista os pacotes em `pacotes`.
- Pacote desconhecido devolve `404 PACOTE_INEXISTENTE`. Sem `pacoteId`, `400 DADOS_INVALIDOS`.
- A loja ganha a categoria `poder` com três itens de 100 moedas: `poder-queimar` (Queimar Peça), `poder-congelar` (Congelar Coluna) e `poder-extra` (Peça Extra). Eles são comprados pelo `POST .../loja/comprar` e ficam para sempre.
- Poderes não se equipam: `PUT .../loja/equipar` com um poder devolve `404 ITEM_INEXISTENTE`.
- O saldo inicial continua 500, o que dá para comprar os três poderes e ainda sobra.

## Fora do escopo

- O uso dos poderes na partida (PRD futuro): este PRD só os coloca à venda.
- Pagamento real (exige gateway, conta de recebimento e questões legais). A simulação precisa ser confirmada com o professor.
- Histórico das compras de moedas.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas.
- Comprar cada pacote soma 100, 500 e 1000 ao saldo, e o perfil mostra o mesmo valor.
- A loja lista 15 itens (12 anteriores e 3 poderes) e 3 pacotes.