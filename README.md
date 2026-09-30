# Finanças API — Controle de Movimentações Financeiras

API REST desenvolvida com **Java + Spring Boot** para cadastro e gerenciamento de movimentações financeiras pessoais (receitas e despesas), com relatório de **saldo mensal**.

Atividade da disciplina **Sistemas Distribuídos** — Unicuritiba.

## Tecnologias

- Java 17+
- Spring Boot 3.5 (Spring Web, Spring Data JPA, Bean Validation)
- Banco de dados H2
- Maven (com Maven Wrapper incluso)
- JUnit 5 + MockMvc (testes de integração)

## Arquitetura

O projeto é organizado em camadas:

```
Controller  →  Service (regras de negócio)  →  Repository  →  Banco de Dados (H2)
```

```
src/main/java/br/com/unicuritiba/financas
├── FinancasApiApplication.java      # Classe principal
├── controller/
│   └── MovimentacaoController.java  # Endpoints REST
├── service/
│   └── MovimentacaoService.java     # Regras de negócio e cálculo do saldo
├── repository/
│   └── MovimentacaoRepository.java  # Acesso ao banco (Spring Data JPA)
├── model/
│   ├── Movimentacao.java            # Entidade JPA
│   └── TipoMovimentacao.java        # Enum: RECEITA / DESPESA
├── dto/
│   └── RelatorioMensal.java         # Resposta do relatório mensal
└── exception/                       # Tratamento de erros padronizado
```

### Entidade `Movimentacao`

| Atributo    | Tipo                | Descrição                               |
|-------------|---------------------|-----------------------------------------|
| `id`        | `Long`              | Identificador único (gerado pelo banco) |
| `descricao` | `String`            | Descrição da movimentação               |
| `valor`     | `BigDecimal`        | Valor da movimentação                   |
| `tipo`      | `TipoMovimentacao`  | Enum: `RECEITA` ou `DESPESA`            |
| `data`      | `LocalDate`         | Data da movimentação (`yyyy-MM-dd`)     |
| `categoria` | `String`            | Categoria da movimentação               |

### Regras de negócio

1. O valor da movimentação deve ser **maior que zero**.
2. Toda movimentação deve possuir uma **descrição**.
3. Toda movimentação deve possuir uma **data**.
4. Toda movimentação deve possuir um **tipo**.
5. O tipo deve ser `RECEITA` ou `DESPESA` (garantido pelo `enum`).
6. O saldo mensal é **calculado** a partir das movimentações cadastradas (`receitas - despesas`).
7. O saldo **não** é armazenado como atributo da entidade.
8. Todo acesso ao banco é feito por meio do `MovimentacaoRepository`.

Quando alguma regra é violada, a API responde `400 Bad Request` com a mensagem do erro.

## Como executar

**Pré-requisito:** JDK 17 ou superior instalado (não é necessário instalar o Maven, o projeto usa o Maven Wrapper).

```bash
git clone https://github.com/zyonandriguetto/financas-api.git
cd financas-api
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em **http://localhost:8080**.

> **Windows com nome de usuário acentuado** (ex.: `C:\Users\Usuário`): o Java pode falhar ao iniciar o servidor com o erro `Unable to establish loopback connection`. Para contornar, execute:
>
> ```bash
> mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Djdk.net.unixdomain.tmpdir=C:\Users\Public"
> ```

### Rodar os testes

```bash
./mvnw test
```

### Console do H2

Os dados ficam salvos no arquivo `./data/financasdb`. Para visualizar o banco:

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/financasdb`
- Usuário: `sa` — Senha: *(vazia)*

## Endpoints

| Método   | Endpoint                                   | Descrição                                          |
|----------|--------------------------------------------|----------------------------------------------------|
| `POST`   | `/movimentacoes`                           | Cadastra uma nova movimentação                     |
| `GET`    | `/movimentacoes`                           | Lista todas as movimentações                       |
| `GET`    | `/movimentacoes?tipo=DESPESA`              | Filtra por tipo (parâmetro opcional)               |
| `GET`    | `/movimentacoes?categoria=Trabalho`        | Filtra por categoria (parâmetro opcional)          |
| `GET`    | `/movimentacoes/{id}`                      | Busca uma movimentação pelo id                     |
| `PUT`    | `/movimentacoes/{id}`                      | Altera uma movimentação existente                  |
| `DELETE` | `/movimentacoes/{id}`                      | Exclui uma movimentação                            |
| `GET`    | `/movimentacoes/relatorio?ano=2026&mes=9`  | Relatório com o saldo financeiro do mês informado  |

Uma coleção pronta para importar no **Postman** ou no **Insomnia** está em [`postman/financas-api.postman_collection.json`](postman/financas-api.postman_collection.json).

## Exemplos de requisições e respostas

### Cadastrar movimentação

`POST /movimentacoes`

```json
{
  "descricao": "Salário",
  "valor": 5000.00,
  "tipo": "RECEITA",
  "data": "2026-09-01",
  "categoria": "Trabalho"
}
```

Resposta `201 Created` (header `Location: http://localhost:8080/movimentacoes/1`):

```json
{
  "id": 1,
  "descricao": "Salário",
  "valor": 5000.00,
  "tipo": "RECEITA",
  "data": "2026-09-01",
  "categoria": "Trabalho"
}
```

### Listar movimentações

`GET /movimentacoes`

Resposta `200 OK`:

```json
[
  { "id": 1, "descricao": "Salário", "valor": 5000.00, "tipo": "RECEITA", "data": "2026-09-01", "categoria": "Trabalho" },
  { "id": 2, "descricao": "Aluguel", "valor": 1500.00, "tipo": "DESPESA", "data": "2026-09-05", "categoria": "Moradia" },
  { "id": 3, "descricao": "Mercado", "valor": 800.50, "tipo": "DESPESA", "data": "2026-09-10", "categoria": "Alimentação" }
]
```

### Filtrar por tipo

`GET /movimentacoes?tipo=DESPESA`

Resposta `200 OK`:

```json
[
  { "id": 2, "descricao": "Aluguel", "valor": 1500.00, "tipo": "DESPESA", "data": "2026-09-05", "categoria": "Moradia" },
  { "id": 3, "descricao": "Mercado", "valor": 800.50, "tipo": "DESPESA", "data": "2026-09-10", "categoria": "Alimentação" }
]
```

### Buscar por id

`GET /movimentacoes/1`

Resposta `200 OK`:

```json
{ "id": 1, "descricao": "Salário", "valor": 5000.00, "tipo": "RECEITA", "data": "2026-09-01", "categoria": "Trabalho" }
```

### Alterar movimentação

`PUT /movimentacoes/3`

```json
{
  "descricao": "Mercado do mês",
  "valor": 950.00,
  "tipo": "DESPESA",
  "data": "2026-09-10",
  "categoria": "Alimentação"
}
```

Resposta `200 OK`:

```json
{ "id": 3, "descricao": "Mercado do mês", "valor": 950.00, "tipo": "DESPESA", "data": "2026-09-10", "categoria": "Alimentação" }
```

### Excluir movimentação

`DELETE /movimentacoes/2`

Resposta: `204 No Content` (sem corpo).

### Relatório mensal (saldo)

`GET /movimentacoes/relatorio?ano=2026&mes=9`

Resposta `200 OK`:

```json
{
  "ano": 2026,
  "mes": 9,
  "totalReceitas": 5000.00,
  "totalDespesas": 2450.00,
  "saldo": 2550.00,
  "quantidadeMovimentacoes": 3,
  "movimentacoes": [
    { "id": 1, "descricao": "Salário", "valor": 5000.00, "tipo": "RECEITA", "data": "2026-09-01", "categoria": "Trabalho" },
    { "id": 2, "descricao": "Aluguel", "valor": 1500.00, "tipo": "DESPESA", "data": "2026-09-05", "categoria": "Moradia" },
    { "id": 3, "descricao": "Mercado do mês", "valor": 950.00, "tipo": "DESPESA", "data": "2026-09-10", "categoria": "Alimentação" }
  ]
}
```

### Erros

Movimentação inexistente — `GET /movimentacoes/99` → `404 Not Found`:

```json
{
  "timestamp": "2026-09-30T12:25:11.1263471",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Movimentação com id 99 não encontrada"
}
```

Dados inválidos — `POST /movimentacoes` com `{"descricao": "", "valor": -10, "tipo": "DESPESA"}` → `400 Bad Request`:

```json
{
  "timestamp": "2026-09-30T12:25:11.1723543",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "detalhes": [
    "descricao: A descrição é obrigatória",
    "data: A data é obrigatória",
    "valor: O valor deve ser maior que zero"
  ]
}
```

Tipo inválido — `"tipo": "INVESTIMENTO"` → `400 Bad Request`:

```json
{
  "timestamp": "2026-09-30T12:25:11.2188856",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "JSON inválido. O tipo deve ser RECEITA ou DESPESA e a data deve estar no formato yyyy-MM-dd"
}
```
