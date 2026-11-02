# Referência da API

Base local: `http://localhost:8080`. A API também está descrita no Swagger (`/swagger-ui`, JSON em `/openapi`). Todo corpo é JSON.

## Convenções

- **Sem token por enquanto.** O login devolve o jogador; o front guarda o `id` e o envia como `jogadorId` nas chamadas e na URL do WebSocket.
- **Erros** têm sempre o formato `{"codigo":"...","mensagem":"..."}`. O front decide o que mostrar pelo `codigo`.
- **Cores** dos jogadores: `blue`, `red`, `purple`, `green`, `orange`, `yellow` ou `brown`.
- **Tabuleiro:** matriz de 6 linhas (de cima para baixo) por 7 colunas, com `"VERMELHO"`, `"AMARELO"` ou `null`. VERMELHO é o `jogadorUm` da partida e AMARELO é o `jogadorDois`.

## Login

| Método e rota | Corpo | Resposta |
|---|---|---|
| `POST /auth/cadastro` | `{email, senha}` (senha de 6 a 72 caracteres) | 201, jogador |
| `POST /auth/login` | `{email, senha}` | 200, jogador; 401 `CREDENCIAIS_INVALIDAS` |

## Jogador, perfil, histórico e loja

| Método e rota | Corpo ou parâmetros | Resposta |
|---|---|---|
| `GET /jogadores/{id}` | | jogador `{id, nome, email, criadoEm, cor}` |
| `GET /jogadores/{id}/perfil` | | `{id, email, nick, nomeCompleto, bio, cor, vitorias, derrotas, moedas}` |
| `PUT /jogadores/{id}/perfil` | `{nick (até 20), nomeCompleto (até 100), bio (até 280), cor}` | perfil atualizado |
| `GET /jogadores/{id}/historico?filtro=todos\|online\|diario` | | `{partidas:[{id, data, adversario, resultado, jogadas, modo}]}`; `resultado` é `VITORIA`, `DERROTA` ou `EMPATE` |
| `GET /jogadores/{id}/loja` | | `{moedas, equipados:{avatar,board,emote}, categorias:[{id, nome, itens:[{id, nome, preco, possuido, equipado}]}]}` |
| `POST /jogadores/{id}/loja/comprar` | `{itemId}` | loja atualizada |
| `PUT /jogadores/{id}/loja/equipar` | `{categoria, itemId}` | loja atualizada |

Também existem `GET /jogadores`, `POST /jogadores`, `PUT /jogadores/{id}`, `DELETE /jogadores/{id}` e `PUT /jogadores/{id}/cor` (cor em `#RRGGBB`, formato antigo; o front usa o perfil).

Moedas: cada jogador começa com 500. Ao fim de cada partida, vitória dá +50 e derrota ou empate dá +10.

## Salas

| Método e rota | Corpo | Resposta |
|---|---|---|
| `POST /salas` | `{jogadorId, serie (1, 3 ou 5), cor}` | sala |
| `POST /salas/entrar` | `{jogadorId, pin, cor}` | sala (quem já está nela recebe a situação atual) |
| `GET /salas/{pin}` | | sala (o front consulta a cada 2 s) |
| `PUT /salas/{pin}/cor` | `{jogadorId, cor}` | sala |
| `POST /salas/{pin}/iniciar` | `{jogadorId}` (só o dono, com o convidado na sala) | sala em `JOGANDO` |
| `POST /salas/{pin}/revanche` | `{jogadorId}` | sala |
| `POST /salas/{pin}/sair` | `{jogadorId}` | 204 |

Sala: `{pin, serie, fase, dono:{id,nome,cor}, convidado, partidaId, revancheDe, placar, serieEncerrada, campeaoId}`.

- `fase`: `AGUARDANDO`, `JOGANDO` ou `ENCERRADA`.
- A primeira partida começa pelo dono. A cada revanche, quem começa alterna.
- Série "melhor de N": vence quem chega primeiro à maioria (1 → 1 vitória, 3 → 2, 5 → 3). `placar` traz as vitórias por id de jogador. Empate não pontua.
- Com a série decidida, a revanche abre uma série nova com o placar zerado.
- Sair: no lobby o convidado libera a vaga e, se o dono sair, a sala some; com a partida em andamento a sala é encerrada; depois do fim, quem sai fecha a sala.

## Partidas

| Método e rota | Corpo | Resposta |
|---|---|---|
| `GET /partidas/{id}/estado` | | `{partidaId, jogadorUm, jogadorDois, statusPartida, resultado, vencedorId, proximoJogadorId, totalJogadas, tabuleiro, pin, serie, placar, serieEncerrada}` |
| `POST /partidas/{id}/jogadas` | `{jogadorId, coluna (0 a 6)}` | 201, jogada com o tabuleiro novo |
| `GET /partidas/{id}/jogadas` | | jogadas em ordem |

`pin`, `serie` e `placar` só vêm quando a partida nasceu de uma sala. Também existem `GET/POST /partidas`, `GET /partidas/{id}`, `PUT /partidas/{id}` (finalizar) e `DELETE /partidas/{id}`.

## WebSocket da partida

`ws://localhost:8080/ws/partidas/{partidaId}?jogadorId={id}`. As origens permitidas são as de `lig4.origens-permitidas` (variável `LIG4_ORIGENS_PERMITIDAS`).

| Sentido | Mensagem |
|---|---|
| Servidor → cliente, ao conectar | `{"tipo":"ESTADO","dados":{...estado...}}` |
| Cliente → servidor | `{"tipo":"JOGAR","coluna":3}` ou `{"tipo":"ESTADO"}` |
| Servidor → os dois | `{"tipo":"JOGADA","dados":{...jogada...}}` |
| Servidor → quem errou | `{"tipo":"ERRO","codigo":"...","mensagem":"..."}` |
| Servidor → os conectados, ao conectar ou desconectar alguém | `{"tipo":"PRESENCA","dados":{"conectados":[12,15]}}` |

## Códigos de erro

| Status | Códigos |
|---|---|
| 400 | `DADOS_INVALIDOS`, `REQUISICAO_INVALIDA`, `EMAIL_INVALIDO`, `SENHA_INVALIDA`, `NICK_INVALIDO`, `COR_INVALIDA`, `FILTRO_INVALIDO`, `PIN_INVALIDO`, `SERIE_INVALIDA`, `PARTIDA_INVALIDA`, `COLUNA_INVALIDA`, `COLUNA_CHEIA` |
| 401 | `CREDENCIAIS_INVALIDAS` |
| 403 | `SEM_PERMISSAO` |
| 404 | `JOGADOR_NAO_ENCONTRADO`, `PARTIDA_NAO_ENCONTRADA`, `PIN_NAO_ENCONTRADO`, `ITEM_INEXISTENTE` |
| 409 | `EMAIL_JA_CADASTRADO`, `SALA_CHEIA`, `COR_JA_ESCOLHIDA`, `SEM_CONVIDADO`, `FASE_INVALIDA`, `JOGO_ENCERRADO`, `FORA_DO_TURNO`, `ITEM_NAO_POSSUIDO`, `ITEM_JA_POSSUIDO`, `SALDO_INSUFICIENTE` |
| 500 | `ERRO_INTERNO` |

## Limitações conhecidas

- Não há token: qualquer cliente pode enviar o `jogadorId` de outro jogador.
- Partida abandonada não é finalizada e não conta nas estatísticas nem dá moedas.
- O desafio diário não existe no back-end (`filtro=diario` devolve lista vazia).
- A sala é consultada por polling; o WebSocket existe só para a partida.