# Registros de Decisões de Arquitetura (ADRs)

ADRs (Architecture Decision Records) documentam decisões importantes sobre a arquitetura e o processo do projeto, incluindo o **contexto** que motivou cada escolha.

## Por que usar ADRs?

- Preservam o **porquê** das decisões, não apenas o resultado final.
- Permitem que novos membros (humanos ou agentes de IA) entendam o projeto sem reabrir discussões já encerradas.
- Deixam explícito quando uma decisão foi **substituída** por outra.

## Como registrar uma nova decisão

1. Copie [`template.md`](template.md) para um novo arquivo numerado sequencialmente:
   - Formato: `NNNN-titulo-curto.md` (ex.: `0002-escolha-do-banco-de-dados.md`).
   - Numeração em quatro dígitos, começando em `0001`.
2. Preencha todas as seções do template.
3. Abra um PR com o novo ADR para revisão da equipe.
4. Após aprovação, atualize a tabela de decisões em [ARCHITECTURE.md](../../ARCHITECTURE.md).

## Estados possíveis

- **Proposta:** em discussão, ainda não aceita.
- **Aceita:** decisão vigente.
- **Substituída:** foi revogada por outro ADR (indicar qual).
- **Rejeitada:** discutida e não aprovada (mantida como registro histórico).

## Índice

| ADR | Título | Estado |
|---|---|---|
| [0001](0001-estrutura-de-documentacao-e-uso-de-ia.md) | Estrutura de documentação e uso de IA | Aceita |
| [0002](0002-padronizacao-de-commits-com-rastreio-por-prd.md) | Padronização de commits com rastreio por PRD | Aceita |
