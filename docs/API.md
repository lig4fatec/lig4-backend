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
| `GET /jogadores/{id}/loja` | | `{moedas, equipados:{avatar,board,emote}, categorias:[{id, nome, itens:[{id, nome, preco, possuido, equipado}]}], pacotes:[{id, moedas, precoReais}]}` |
| `POST /jogadores/{id}/loja/comprar` | `{itemId}` | loja atualizada |
| `POST /jogadores/{id}/loja/moedas` | `{pacoteId}` | loja atualizada (compra simulada de moedas) |
| `PUT /jogadores/{id}/loja/equipar` | `{categoria, itemId}` | loja atualizada |

Também existem `GET /jogadores`, `POST /jogadores`, `PUT /jogadores/{id}`, `DELETE /jogadores/{id}` e `PUT /jogadores/{id}/cor` (cor em `#RRGGBB`, formato antigo; o front usa o perfil).

**Categorias da loja:** `avatar`, `board`, `emote` e `poder`. Os poderes (`poder-queimar` "Queimar Peça", `poder-congelar` "Congelar Coluna" e `poder-extra` "Peça Extra") custam 100 moedas cada, são comprados uma vez e ficam para sempre. Eles não se equipam (`PUT .../equipar` devolve `ITEM_INEXISTENTE`) e só são usados no modo Caos.

**Compra de moedas (simulada):** não existe pagamento de verdade, o pacote escolhido só soma ao saldo. Pacotes: `pacote-100` (100 moedas, R$ 5), `pacote-500` (500 moedas, R$ 20) e `pacote-1000` (1000 moedas, R$ 40). Pacote desconhecido devolve `404 PACOTE_INEXISTENTE`.

Moedas: cada jogador começa com 500. Ao fim de cada partida: no Clássico, vitória +15, empate +5 e derrota +2; no Caos, vitória +30, empate +10 e derrota +5.

## Salas

| Método e rota | Corpo | Resposta |
|---|---|---|
| `POST /salas` | `{jogadorId, modo ("CLASSICO" ou "CAOS"; vazio vale CLASSICO), cor}` | sala |
| `POST /salas/entrar` | `{jogadorId, pin, cor}` | sala (quem já está nela recebe a situação atual) |
| `GET /salas/{pin}` | | sala (o front consulta a cada 2 s) |
| `PUT /salas/{pin}/cor` | `{jogadorId, cor}` | sala |
| `PUT /salas/{pin}/pronto` | `{jogadorId, pronto (true ou false)}` | sala (quando os dois estão prontos, já vem em `JOGANDO`) |
| `POST /salas/{pin}/revanche` | `{jogadorId}` | sala |
| `POST /salas/{pin}/sair` | `{jogadorId}` | 204 |

Sala: `{pin, modo, fase, dono:{id,nome,cor}, convidado, partidaId, revancheDe, prontoDono, prontoConvidado}`.

- `fase`: `AGUARDANDO`, `JOGANDO` ou `ENCERRADA`.
- A partida só começa quando o dono e o convidado marcam "pronto" (`PUT /salas/{pin}/pronto`): quando o segundo marca, o back cria a partida sozinho, a sala vira `JOGANDO` e `partidaId` vem preenchido. Não existe mais `POST /salas/{pin}/iniciar`.
- O "pronto" só vale enquanto a sala está em `AGUARDANDO` (depois, `FASE_INVALIDA`). Quem troca de cor perde o próprio "pronto", e quando o convidado sai os dois voltam a falso.
- A primeira partida começa pelo dono. A cada revanche, quem começa alterna.
- Modo: `CLASSICO` ou `CAOS` (aceita também minúsculas e sem acento). Outro valor devolve `MODO_INVALIDO`. A série e o placar foram removidos; um `serie` enviado por um front antigo é ignorado.
- A revanche mantém o modo da sala.
- Sair: no lobby o convidado libera a vaga e, se o dono sair, a sala some; com a partida em andamento a sala é encerrada; depois do fim, quem sai fecha a sala.

## Partidas

| Método e rota | Corpo | Resposta |
|---|---|---|
| `GET /partidas/{id}/estado` | | `{partidaId, jogadorUm, jogadorDois, statusPartida, resultado, vencedorId, proximoJogadorId, totalJogadas, tabuleiro, pin, modo, motivoFim, empatePropostoPor, empatesUsados, relogioDe, tempoJogadorUmMs, tempoJogadorDoisMs, poderesUsados, poderesDisponiveis, congelamentos, pecaExtraAtiva}` |
| `POST /partidas/{id}/jogadas` | `{jogadorId, coluna (0 a 6)}` | 201, jogada com o tabuleiro novo, `relogioDe`, os tempos, `congelamentos` e `pecaExtraAtiva` |
| `GET /partidas/{id}/jogadas` | | jogadas em ordem, cada uma com `tipo` (`PECA`, `QUEIMAR`, `CONGELAR` ou `EXTRA`) |
| `POST /partidas/{id}/poderes` | `{jogadorId, poder (QUEIMAR, CONGELAR ou EXTRA), coluna, linha}` | estado (usa um poder do Caos) |
| `POST /partidas/{id}/desistir` | `{jogadorId}` | estado (o adversário vence) |
| `POST /partidas/{id}/empate` | `{jogadorId}` | estado (propõe empate) |
| `POST /partidas/{id}/empate/aceitar` | `{jogadorId}` | estado (partida empatada) |
| `POST /partidas/{id}/empate/recusar` | `{jogadorId}` | estado (a partida continua) |

`pin` só vem quando a partida nasceu de uma sala. `POST /partidas` aceita `modo` (`CLASSICO` por padrão). Também existem `GET/POST /partidas`, `GET /partidas/{id}`, `PUT /partidas/{id}` (finalizar) e `DELETE /partidas/{id}`.

**Como a partida terminou (`motivoFim`):** nulo enquanto está em andamento; depois `VITORIA` (quatro peças), `TABULEIRO_CHEIO` (empate sem vencedor), `DESISTENCIA`, `EMPATE_ACORDADO` ou `TEMPO` (o tempo de um jogador acabou).

**Relógio:** como no xadrez, cada jogador tem um tempo total (5 minutos por padrão) e só corre o de quem está na vez. O tempo não reinicia a cada jogada. O estado e a resposta de cada jogada trazem `tempoJogadorUmMs` e `tempoJogadorDoisMs` (o que cada um ainda tem, em milissegundos) e `relogioDe` (o id de quem está na vez, ou nulo se a partida terminou). O front mostra a contagem a partir desses valores e os atualiza a cada jogada. Se o tempo de quem está na vez acaba, o servidor termina a partida em até 1 segundo: o adversário vence, `motivoFim` vira `TEMPO`, as moedas são pagas como numa vitória e o `ESTADO` é enviado aos dois pelo WebSocket. Jogar sem tempo devolve `JOGO_ENCERRADO`. O tempo por jogador pode ser mudado no servidor com `LIG4_RELOGIO_TEMPO_POR_JOGADOR_SEGUNDOS`.

**Desistência:** quem desiste perde; o adversário vence e cada um recebe as moedas de vitória e de derrota do modo. Depois disso a partida e a sala ficam `FINALIZADA` e `ENCERRADA`.

**Proposta de empate:** cada jogador pode propor uma vez por partida (`EMPATE_JA_PROPOSTO` na segunda) e só uma proposta fica aberta por vez (`EMPATE_PENDENTE`). Enquanto aberta, o estado traz `empatePropostoPor` com o id de quem propôs. O adversário aceita (a partida termina em `EMPATE` com `motivoFim` `EMPATE_ACORDADO` e moedas de empate) ou recusa (a proposta some e a partida continua). Se o adversário jogar sem responder, vale como recusa. Aceitar ou recusar sem proposta do adversário devolve `SEM_PROPOSTA_DE_EMPATE`. `empatesUsados` lista os ids que já gastaram a proposta, para o front desabilitar o botão.

**Poderes do Caos:** só existem no modo Caos e precisam ter sido comprados na loja (`poder-queimar`, `poder-congelar`, `poder-extra`). Cada jogador usa cada poder **uma vez por partida**, só na sua vez e **antes de soltar peça** nessa vez. O estado traz `poderesUsados` e `poderesDisponiveis` (lista de `{jogadorId, poder}`; os disponíveis são os comprados e ainda não usados, e vêm vazios no Clássico e depois do fim), `congelamentos` (lista de `{coluna, jogadorId, turnosRestantes}`) e `pecaExtraAtiva`.

- `QUEIMAR`: informe `coluna` e `linha` (linha 0 é a **base** da coluna, a mesma numeração de `linha` nas jogadas) de uma peça do **adversário**. A peça some e a vez passa ao adversário. As peças de cima não descem; a próxima peça jogada na coluna cai na casa vazia. Alvo vazio, peça própria ou fora do tabuleiro devolve `ALVO_INVALIDO` (por isso não funciona antes de existir peça do adversário).
- `CONGELAR`: informe a `coluna`. O adversário não pode jogar nela nas **próximas 3 vezes dele**, e a vez passa a ele. Coluna cheia devolve `COLUNA_CHEIA`. Jogar numa coluna congelada devolve `COLUNA_CONGELADA`. Se o adversário só tiver colunas congeladas para jogar, ele perde a vez (conta como uma das 3).
- `EXTRA` (o "Jogar 2x" do front): sem alvo. Não gasta a vez: `pecaExtraAtiva` fica `true`, o mesmo jogador joga duas peças seguidas e depois a vez passa. Não vale com menos de 2 casas livres (`PECA_EXTRA_SEM_ESPACO`). Se a primeira peça já ganha ou enche o tabuleiro, a partida termina ali.
- Usar poder depois de receber uma proposta de empate vale como recusá-la, como jogar. O relógio do jogador continua correndo enquanto ele usa o poder.
- Erros: `PODER_INVALIDO` (400, poder desconhecido ou alvo faltando), `MODO_SEM_PODERES`, `PODER_NAO_POSSUIDO`, `PODER_JA_USADO`, `PODER_FORA_DE_HORA`, `ALVO_INVALIDO`, `COLUNA_CONGELADA`, `PECA_EXTRA_SEM_ESPACO` (409), além de `FORA_DO_TURNO` e `JOGO_ENCERRADO`.
- `totalJogadas` e o histórico do perfil contam só as peças soltas, não os poderes. Em `GET /partidas/{id}/jogadas`, nos poderes `coluna` e `linha` guardam o alvo (`CONGELAR` só tem coluna; `EXTRA` vem com 0 e 0) e `numero` conta peças e poderes juntos.

Durante a partida o front usa os comandos do WebSocket abaixo; os endpoints HTTP fazem o mesmo e também avisam quem está conectado.

## WebSocket da partida

`ws://localhost:8080/ws/partidas/{partidaId}?jogadorId={id}`. As origens permitidas são as de `lig4.origens-permitidas` (variável `LIG4_ORIGENS_PERMITIDAS`).

| Sentido | Mensagem |
|---|---|
| Servidor → cliente, ao conectar | `{"tipo":"ESTADO","dados":{...estado...}}` |
| Cliente → servidor | `{"tipo":"JOGAR","coluna":3}` ou `{"tipo":"ESTADO"}` |
| Cliente → servidor | `{"tipo":"DESISTIR"}`, `{"tipo":"PROPOR_EMPATE"}`, `{"tipo":"ACEITAR_EMPATE"}` ou `{"tipo":"RECUSAR_EMPATE"}` |
| Cliente → servidor | `{"tipo":"PODER","poder":"QUEIMAR","coluna":3,"linha":0}`, `{"tipo":"PODER","poder":"CONGELAR","coluna":3}` ou `{"tipo":"PODER","poder":"EXTRA"}` |
| Servidor → os dois, depois de desistir, de uma ação de empate, de um poder ou do fim por tempo | `{"tipo":"ESTADO","dados":{...estado...}}` |
| Servidor → os dois | `{"tipo":"JOGADA","dados":{...jogada...}}` |
| Servidor → quem errou | `{"tipo":"ERRO","codigo":"...","mensagem":"..."}` |
| Servidor → os conectados, ao conectar ou desconectar alguém | `{"tipo":"PRESENCA","dados":{"conectados":[12,15]}}` |

## Códigos de erro

| Status | Códigos |
|---|---|
| 400 | `DADOS_INVALIDOS`, `REQUISICAO_INVALIDA`, `EMAIL_INVALIDO`, `SENHA_INVALIDA`, `NICK_INVALIDO`, `COR_INVALIDA`, `FILTRO_INVALIDO`, `PIN_INVALIDO`, `MODO_INVALIDO`, `PARTIDA_INVALIDA`, `COLUNA_INVALIDA`, `COLUNA_CHEIA`, `PODER_INVALIDO` |
| 401 | `CREDENCIAIS_INVALIDAS` |
| 403 | `SEM_PERMISSAO` |
| 404 | `JOGADOR_NAO_ENCONTRADO`, `PARTIDA_NAO_ENCONTRADA`, `PIN_NAO_ENCONTRADO`, `ITEM_INEXISTENTE`, `PACOTE_INEXISTENTE` |
| 409 | `EMAIL_JA_CADASTRADO`, `SALA_CHEIA`, `COR_JA_ESCOLHIDA`, `SEM_CONVIDADO`, `FASE_INVALIDA`, `JOGO_ENCERRADO`, `FORA_DO_TURNO`, `EMPATE_JA_PROPOSTO`, `EMPATE_PENDENTE`, `SEM_PROPOSTA_DE_EMPATE`, `MODO_SEM_PODERES`, `PODER_NAO_POSSUIDO`, `PODER_JA_USADO`, `PODER_FORA_DE_HORA`, `ALVO_INVALIDO`, `COLUNA_CONGELADA`, `PECA_EXTRA_SEM_ESPACO`, `ITEM_NAO_POSSUIDO`, `ITEM_JA_POSSUIDO`, `SALDO_INSUFICIENTE` |
| 500 | `ERRO_INTERNO` |

## Limitações conhecidas

- Não há token: qualquer cliente pode enviar o `jogadorId` de outro jogador.
- Partida abandonada (sem relógio, criada antes do relógio) não é finalizada e não conta nas estatísticas nem dá moedas. Partidas novas terminam sozinhas por tempo.
- Queimar Peça não tem gravidade: a casa queimada fica vazia e as peças de cima continuam no lugar.
- O desafio diário não existe no back-end (`filtro=diario` devolve lista vazia).
- A sala é consultada por polling; o WebSocket existe só para a partida.