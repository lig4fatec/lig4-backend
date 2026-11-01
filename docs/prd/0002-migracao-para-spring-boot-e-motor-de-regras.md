# PRD-0002. Migração para Spring Boot e motor de regras do jogo

- **Data:** 2026-10-21
- **Estado:** Proposta
- **Desenvolvedor** Thamires

## Objetivo

Alinhar o back-end à stack escolhida pela equipe (Spring Boot, em vez do Quarkus usado no esqueleto inicial) e entregar a base do motor de regras do LIG 4, para que o front-end possa integrar o jogo em tempo real até 31/10/2026.

## Requisitos

- Back-end em Spring Boot 4 com Java 25 e Maven, preservando os endpoints e o comportamento do CRUD de Jogador e Partida entregue no [PRD-0001](0001-estrutura-de-documentacao-e-padronizacao-de-commits.md) e no commit de configuração inicial do projeto.
- Mesmo contrato HTTP de antes: `/jogadores` e `/partidas`, com os códigos 201, 200, 204, 400, 404 e 409 e corpo de erro `{"mensagem": "..."}`.
- Banco PostgreSQL com schema versionado por Flyway (migrations V1 e V2 mantidas sem alteração).
- Documentação interativa da API em `/swagger-ui`.
- Dependência de WebSocket disponível no projeto, para a comunicação em tempo real com o front-end (WBS 1.5.2.1, a ser implementado em PRD próprio).
- Motor de regras em pacote próprio (`br.com.lig4.engine`), em Java puro e sem dependência de framework, cobrindo tabuleiro 6x7, alternância de turnos, validação de jogadas (turno, coluna inválida, coluna cheia), vitória em 4 direções, empate e encerramento (WBS 1.3.1 a 1.3.3).
- Testes automatizados do CRUD e do motor.

## Critérios de aceite

- `.\mvnw.cmd clean test` executa sem falhas (21 testes: 13 do motor, 4 de Jogador e 4 de Partida).
- A aplicação sobe com `.\mvnw.cmd spring-boot:run` e responde em `/jogadores`, `/partidas` e `/swagger-ui`.
- Os testes do CRUD podem ser executados várias vezes no mesmo banco, sem conflito de dados.
- Nenhum código do pacote `engine` importa classes do Spring ou do JPA.
