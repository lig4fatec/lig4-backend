# Documentos de Requisitos de Produto (PRDs)

PRDs registram os requisitos do produto e são a fonte dos identificadores usados nas mensagens de commit (ver [ADR 0002](../adr/0002-padronizacao-de-commits-com-rastreio-por-prd.md)).

## Convenções

- Um arquivo por requisito, nomeado `PRD-XXXX-titulo-curto.md` (ex.: `PRD-0001-autenticacao-de-usuarios.md`).
- Numeração sequencial em quatro dígitos, começando em `0001`, única por repositório.
- Novos PRDs seguem o [template](template.md) e entram via PR, como os ADRs.

## Fluxo

1. Escreva o PRD descrevendo objetivo, requisitos e critérios de aceite.
2. Obtenha aprovação da equipe via PR.
3. Referencie o número (`PRD-XXXX`) nos branches, issues, PRs e commits relacionados ao requisito.

## Índice

| PRD | Título | Estado |
|---|---|---|
| [0001](0001-estrutura-de-documentacao-e-padronizacao-de-commits.md) | Estrutura de documentação e padronização de commits | Implementado |
| [0002](0002-migracao-para-spring-boot-e-motor-de-regras.md) | Migração para Spring Boot e motor de regras do jogo | Proposta |
| [0003](0003-persistencia-de-jogadas.md) | Persistência e registro das jogadas | Proposta |
