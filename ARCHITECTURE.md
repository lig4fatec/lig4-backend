# ARCHITECTURE.md

Arquitetura do sistema LIG 4.

> **Status:** documento em construção. A stack do back-end está definida no [ADR 0003](docs/adr/0003-adocao-de-spring-boot-como-framework-do-back-end.md); as demais seções serão preenchidas conforme novas decisões forem registradas como ADRs em [`docs/adr/`](docs/adr/).

## Visão geral

**[a definir]** — descrição do sistema, objetivos e contexto de negócio.

## Decisões técnicas

Toda decisão relevante está registrada como ADR. Consulte [`docs/adr/`](docs/adr/) para a lista completa e o processo de registro.

| ADR | Decisão | Status |
|---|---|---|
| [0001](docs/adr/0001-estrutura-de-documentacao-e-uso-de-ia.md) | Estrutura de documentação com AGENTS.md como ponto de entrada para IA | Aceita |
| [0002](docs/adr/0002-padronizacao-de-commits-com-rastreio-por-prd.md) | Padronização de commits com rastreio por PRD | Aceita |
| [0003](docs/adr/0003-adocao-de-spring-boot-como-framework-do-back-end.md) | Adoção de Spring Boot como framework do back-end | Proposta |

## Stack tecnológico

Java 25, Maven, Spring Boot 4 (Spring MVC e Spring Data JPA), PostgreSQL com Flyway, springdoc-openapi e WebSocket. Detalhes e justificativa no [ADR 0003](docs/adr/0003-adocao-de-spring-boot-como-framework-do-back-end.md).

## Estrutura do código

Pacote base `br.com.lig4`: `controller` (API REST), `service` (regras de aplicação), `repository` (acesso a dados), `domain` (entidades), `dto`, `exception` (tratamento de erros) e `engine` (motor de regras do jogo, Java puro).

## Segurança

**[a definir]** — autenticação, autorização, proteção de dados.

## Observabilidade

**[a definir]** — logging, métricas, tracing.
