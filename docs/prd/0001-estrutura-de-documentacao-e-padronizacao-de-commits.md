# PRD-0001. Estrutura de documentação e padronização de commits

- **Data:** 2026-08-25
- **Estado:** Implementado

## Objetivo

Estabelecer, antes da definição da stack tecnológica, a base de documentação e o rastreio de mudanças do projeto LIG 4 nos dois repositórios (`lig4-backend` e `lig4-frontend`).

## Requisitos

- Estrutura de documentação mínima com [AGENTS.md](../../AGENTS.md) como ponto de entrada para agentes de IA ([ADR 0001](../../docs/adr/0001-estrutura-de-documentacao-e-uso-de-ia.md)).
- Registros de decisão de arquitetura (ADRs) com template próprio.
- Mensagens de commit rastreadas por PRD, no formato `PRD-XXXX: descrição curta do que foi feito` ([ADR 0002](../../docs/adr/0002-padronizacao-de-commits-com-rastreio-por-prd.md)).
- Validação local das mensagens por hook nativo do Git (`githooks/commit-msg`).

## Critérios de aceite

- Documentos criados e espelhados nos dois repositórios.
- Hook ativação documentada em CONTRIBUTING.md (`git config core.hooksPath githooks`).
- Hook rejeita commits fora do padrão e isenta merge/revert/fixup/squash.
