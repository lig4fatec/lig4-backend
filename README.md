# lig4-backend

Backend do projeto LIG 4.

> **Status:** back-end em Java 25 com Spring Boot, conforme o [ADR 0003](docs/adr/0003-adocao-de-spring-boot-como-framework-do-back-end.md). Cobre salas por PIN, partida em tempo real, revanche e série, login, perfil, histórico e loja.

## Como rodar

1. Tenha o Java 25 e um PostgreSQL local com um banco chamado `lig4` (usuário `postgres`; a senha vem da variável `DB_PASSWORD`, padrão `postgres`).
2. Rode `.\mvnw.cmd spring-boot:run` (Windows) ou `./mvnw spring-boot:run`. O Flyway cria as tabelas sozinho.
3. Abra `http://localhost:8080/swagger-ui` para testar a API.
4. Os testes: `.\mvnw.cmd clean test`.

## Documentação

| Documento | Conteúdo |
|---|---|
| [docs/API.md](docs/API.md) | Referência da API REST, do WebSocket e dos códigos de erro |
| [AGENTS.md](AGENTS.md) | Ponto de entrada para agentes de IA e política de uso de IA pela equipe |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Arquitetura do sistema e decisões técnicas |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Como contribuir com o projeto |
| [docs/prd/](docs/prd/) | Documentos de requisitos de produto (PRDs) |
| [docs/adr/](docs/adr/) | Registros de decisões de arquitetura (ADRs) |

## Como contribuir

Consulte o [CONTRIBUTING.md](CONTRIBUTING.md).