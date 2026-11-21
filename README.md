# Caixa Eletrônico: Cliente/Servidor

Projeto de Engenharia de Software: caixa eletrônico com saque, depósito, saldo e extrato, em arquitetura cliente/servidor e MVC.

Equipe: Augusto Barbosa (github: https://github.com/AugustoBarbosa-Zotake ), Jéssica Soares (github: https://github.com/JeehSilphis ), Keteli Lino (github: https://github.com/KeteliLino ) e Nicolas Borges (github: https://github.com/nicolasferr06 ).


- **Cliente:** Java + JavaFX, com telas FXML editáveis no Scene Builder
- **Servidor:** Java, com API HTTP/JSON
- **Banco de dados:** MySQL

## Requisitos

- JDK 25 ou mais recente
- Apache NetBeans (já traz o Maven)
- MySQL 8
- Scene Builder, para editar as telas

## Como executar

**1. Criar o banco.** Execute [banco/schema.sql](banco/schema.sql) no MySQL Workbench. O script não apaga dados existentes. Se o banco foi criado com uma versão anterior do script, execute-o de novo: ele acrescenta o que faltar. O servidor avisa ao iniciar quando o banco está desatualizado.

**2. Configurar a senha do MySQL.** Copie `atm-servidor/servidor.local.properties.exemplo` para `atm-servidor/servidor.local.properties` e preencha a senha. Esse arquivo não vai para o Git.

**3. Abrir no NetBeans.** Vá em *File > Open Project*, selecione a pasta do repositório e abra também os módulos `ATM Servidor` e `ATM Cliente`.

**4. Rodar.** Sempre nesta ordem:

1. Botão direito em **ATM Servidor** > *Run*. Espere aparecer `Servidor ATM em http://127.0.0.1:8080`.
2. Botão direito em **ATM Cliente** > *Run*.

Use o *Run* do projeto. O *Run File* em uma classe do cliente falha com "JavaFX runtime components are missing".

### Usuários de teste

| Cliente | Cartão | PIN | Contas |
|---|---|---|---|
| Maria Silva | `1111222233334444` | `1234` | `0001-1` (R$ 1.000,00), `0001-2` (R$ 250,00) |
| João Souza | `5555666677778888` | `4321` | `0002-1` (R$ 500,00) |

Após 3 senhas erradas seguidas, o cartão é bloqueado. Para desbloquear um cartão de teste, execute no Workbench:

```sql
UPDATE caixa_eletronico.cliente SET tentativas_pin = 0 WHERE cartao = '1111222233334444';
```

## Estrutura

```
banco/schema.sql            Tabelas e dados de teste
atm-servidor/               Servidor
  Configuracao              Configuração do servidor (Singleton)
  model/                    Conta, Cliente, Transacao, TipoTransacao
  command/                  Operações bancárias (Command)
  dao/                      Acesso ao MySQL; ConexaoBD (Singleton)
  service/                  ContaService (invocador), SessaoService (Singleton), Senhas (hash do PIN)
  controller/               ApiController (rotas HTTP)
atm-cliente/                Cliente
  resources/.../view/       Telas FXML e atm.css (View)
  controller/               Controllers das telas (Controller)
  model/                    Dados recebidos do servidor (Model); Sessao (Singleton)
  service/                  ContaBancariaService e ContaServiceProxy (Proxy)
atm-testes-sistema/         Testes de sistema (servidor + terminal + banco de teste)
```

Cliente e servidor são independentes e só se comunicam por HTTP. O Model com as regras de negócio fica no servidor. A View fica no cliente.

## Padrões de projeto (GoF)

| Padrão | Categoria | Onde | Problema que resolve |
|---|---|---|---|
| **Singleton** | Criacional | `Configuracao`, `ConexaoBD` e `SessaoService` (servidor); `Sessao` (cliente) | Garante uma única instância compartilhada. Ex.: o token criado no login precisa ser encontrado pelo mesmo `SessaoService` nas operações seguintes. |
| **Command** | Comportamental | `OperacaoCommand`, `SaqueCommand`, `DepositoCommand`, `SaldoCommand`, `ExtratoCommand`; invocador: `ContaService.executar()` | Cada operação vira um objeto. O invocador executa todas do mesmo jeito, numa transação do banco, e grava cada uma em `operacao_log`. Nova operação = nova classe. |
| **Proxy** | Estrutural | `ContaBancariaService` (interface) e `ContaServiceProxy` (cliente) | As telas chamam o serviço como se fosse local. O proxy esconde o HTTP (proxy remoto) e controla o acesso com a sessão e o token (proxy de proteção). O objeto real é o `ContaService` do servidor. |

## Editar as telas

No NetBeans, configure o Scene Builder em *Tools > Options > Java > JavaFX*. Depois, clique com o botão direito em um `.fxml` > *Open*. Os arquivos ficam em `atm-cliente/src/main/resources/com/atm/cliente/view/`.

## API

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/api/autenticar` | `{cartao, pin}` | 200 `{token, nome, contas}`; 401 |
| POST | `/api/sair` | | 200 |
| GET | `/api/contas/{numero}/saldo` | | 200 `{saldo}` |
| POST | `/api/contas/{numero}/saque` | `{valor}` | 200 comprovante; 409 saldo insuficiente |
| POST | `/api/contas/{numero}/deposito` | `{valor}` | 200 comprovante |
| GET | `/api/contas/{numero}/extrato` | | 200 `{conta, saldo, transacoes}` |

As rotas de conta exigem o cabeçalho `Authorization: Bearer <token>`. Os erros vêm como `{"erro": "mensagem"}`.

Cada operação de conta executada é registrada em `operacao_log` com o resultado `SUCESSO`, `RECUSADA` (ex.: saldo insuficiente) ou `ERRO`. Para consultar no Workbench: `SELECT * FROM operacao_log ORDER BY id DESC;`

## Rodar em duas máquinas

- **Servidor:** em `servidor.local.properties`, defina `servidor.host=0.0.0.0`.
- **Cliente:** em `atm-cliente/src/main/resources/cliente.properties`, troque `servidor.url` pelo IP do servidor.

A comunicação é HTTP sem criptografia, adequada apenas para demonstração em rede local.

## Diferenças em relação aos diagramas

| Diagrama | Implementação | Motivo |
|---|---|---|
| `saldo` e `valor` como `double` | `BigDecimal` / `DECIMAL(15,2)` | `double` acumula erro de arredondamento em dinheiro |
| `Conta` e `Transacao` acessam o MySQL direto (mensagens 14 e 17) | Passam pelos DAOs, chamados pelo `ContaService` | Tira o SQL do Model e permite gravar saldo e transação em uma única transação do banco |
| A sequência começa o saque em `getSaldo()` (mensagem 11) | Antes, a conta é lida do banco com trava (`FOR UPDATE`) | Evita saque duplo simultâneo |
| `sacarValor()` (mensagem 13) | `Conta.sacar()` | Nome usado no diagrama de classes |
| Sem operação de depósito no diagrama de sequência | Depósito segue o mesmo fluxo do saque | Requisito do projeto |
| Cliente com várias contas, sem escolha na sequência | Seleção de conta no menu | Multiplicidade 1..* do diagrama de classes |
| `pin` em texto | Guarda o hash PBKDF2 do PIN | Não armazenar a senha em texto |

## Testes

Os testes cobrem três classes do sistema — `Conta`, `Cliente` e `SessaoService` — em três etapas:

| Etapa | Técnica | Conta | Cliente | SessaoService |
|---|---|---|---|---|
| Unidade | Caixa branca | `ContaTest` | `ClienteTest` | `SessaoServiceTest` |
| Integração | Caixa cinza | `ContaIntegracaoIT` | `ClienteIntegracaoIT` | `SessaoIntegracaoIT` |
| Sistema | Caixa preta | `ContaSistemaIT` | `ClienteSistemaIT` | `SessaoSistemaIT` |

São 24 casos: 2 por classe de teste, mais os que cobrem os casos de teste CT-04, CT-07, CT-08 e CT-12 na classe `Conta`, e o `ConfiguracaoTest`, escrito com TDD.

- **Unidade** (`atm-servidor/src/test/.../model` e `service`): cada classe sozinha, sem banco nem rede.
- **Integração** (`atm-servidor/src/test/.../integracao`): a classe junto com `ContaService`, comandos, DAOs, `ApiController` e o MySQL.
- **Sistema** (módulo `atm-testes-sistema`): o terminal JavaFX abre e o teste usa as telas como o cliente (digita, clica e lê o que aparece), com o servidor de verdade por trás.

Só os testes de unidade (rápidos, sem MySQL):

```bash
mvn test
```

Todos os testes (unidade, integração e sistema):

```bash
mvn verify
```

No NetBeans: botão direito no projeto **Caixa Eletronico** > **Run Maven** > **Testes completos (verify)**.

Os testes de integração e de sistema precisam do MySQL ligado. Eles usam a senha de `atm-servidor/servidor.local.properties` e criam (e recriam a cada teste) o banco **`caixa_eletronico_teste`**, a partir de `banco/schema.sql`. O banco `caixa_eletronico` nunca é alterado. Durante os testes de sistema, a janela do terminal abre e fecha sozinha.

Para usar outro MySQL: `mvn verify -Ddb.servidor=host:porta -Ddb.usuario=... -Ddb.senha=...`
