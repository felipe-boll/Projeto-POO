# API REST de Tarefas (TodoList) - Spring Boot

Projeto bimestral da disciplina de **Programação Orientada a Objetos (POO)**  
**Curso:** Técnico Integrado em Informática — IFPR Câmpus Cascavel  
**Docente:** Profª. M.a. Roberta Vanessa Rojo Parcianello  

---

## 📌 Sumário
1. [Sobre o Projeto](#-sobre-o-projeto)
2. [Arquitetura e Responsabilidade das Classes](#-arquitetura-e-responsabilidade-das-classes)
3. [Solução Adotada para os DTOs (Desafio dos DTOs)](#-solução-adotada-para-os-dtos-desafio-dos-dtos)
4. [Caminho de uma Requisição](#-caminho-de-uma-requisição)
5. [Documentação dos Endpoints e Exemplos de Requisição](#-documentação-dos-endpoints-e-exemplos-de-requisição)
6. [Roteiro de Testes no Thunder Client](#-roteiro-de-testes-no-thunder-client)
7. [Como Executar o Projeto e os Testes](#-como-executar-o-projeto-e-os-testes)

---

## 📖 Sobre o Projeto

Esta aplicação é uma API REST desenvolvida em **Spring Boot** para gerenciamento de tarefas (*todos*). O sistema permite cadastrar tarefas com prazo, marcar a data e hora de conclusão de uma tarefa, listar tarefas separadas por pendentes e concluídas, listar todas as tarefas e excluir uma tarefa existente.

### Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 4 / Spring Web / MVC**
- **Spring Data JPA & Hibernate**
- **MySQL** (Persistência em produção/desenvolvimento)
- **H2 Database** (Banco em memória para execução de testes automatizados isolados)
- **JUnit 5 & Mockito** (Testes unitários e testes de integração de controller via MockMvc)

---

## 🏛 Arquitetura e Responsabilidade das Classes

O projeto adota uma arquitetura em camadas bem definida, garantindo desacoplamento, testabilidade e separação clara de responsabilidades:

```
[Cliente HTTP / Thunder Client]
           │
           ▼
[Controller] (TodoApiController) ─── Recebe HTTP, valida e devolve ResponseEntity (Status HTTP)
           │
           ▼
[Service]    (TodoService)       ─── Regras de negócio e orquestração entre Entidade e DTOs
           │
           ▼
[Repository] (TodoRepository)    ─── Comunicação e consultas no banco de dados (Spring Data JPA)
           │
           ▼
[Banco de Dados MySQL]
```

### Justificativa de cada classe:
- **`TodoApiController` (Camada Controller):**
  - Responsabilidade: Porta de entrada da API. Mapeia os endpoints e verbos HTTP (`@PostMapping`, `@GetMapping`, `@PatchMapping`, `@DeleteMapping`). Recebe os dados em formato JSON (convertidos para DTOs com `@RequestBody`) e define a resposta HTTP correta (códigos de status como `201 Created`, `200 OK` e `204 No Content`).
  - **Importante:** O Controller **não** acessa o banco diretamente; ele delega todo o trabalho para a camada Service.

- **`TodoService` (Camada Service):**
  - Responsabilidade: Contém a lógica de negócio da aplicação. É onde instanciamos a entidade `Todo`, atribuímos campos, chamamos o repositório para salvar ou buscar registros e fazemos a conversão das entidades `Todo` para o DTO de resposta `TodoResponseDTO`.
  - Isola as regras de negócio de detalhes da web (HTTP).

- **`TodoRepository` (Camada Repository):**
  - Responsabilidade: Interface que estende `JpaRepository<Todo, Long>`, fornecendo métodos padrão de persistência (`save`, `findById`, `deleteById`, `findAll`).
  - Inclui consultas derivadas personalizadas por palavra-chave do Spring Data JPA:
    - `findByFinishedAtIsNotNull()`: busca todas as tarefas que possuem data de conclusão preenchida.
    - `findByFinishedAtIsNull()`: busca todas as tarefas cuja data de conclusão é `null` (pendentes).

- **`Todo` (Camada Model / Entidade JPA):**
  - Responsabilidade: Representa a tabela `todo` no banco de dados relacional. Contém as anotações `@Entity`, `@Id`, `@GeneratedValue` e `@Column(nullable = false)`.
  - Seu construtor padrão inicializa `createdAt = LocalDateTime.now()` e mantém `finishedAt` como `null`.

- **DTOs (`TodoCreateDTO`, `TodoConclusaoDTO`, `TodoResponseDTO`):**
  - Responsabilidade: Transferência de dados entre o cliente HTTP e a API, sem expor diretamente a entidade do banco de dados.

---

## 💡 Solução Adotada para os DTOs (Desafio dos DTOs)

> **O Desafio:** *"Como receber `finishedAt` se o DTO atual não possui esse atributo? Como permitir que o cliente confira a data registrada na resposta? Proponha e implemente uma organização dos DTOs que atenda a essas necessidades."*

### Diagnóstico do Problema:
Se utilizássemos um único DTO genérico tanto para entrada quanto para saída:
1. Ao criar uma tarefa via `POST`, o cliente poderia enviar um `finishedAt` ou `id` indevido, burlando a regra de que uma tarefa deve iniciar sem conclusão e com ID gerado pelo sistema.
2. Ao atualizar a conclusão via `PATCH`, o cliente precisa enviar apenas o campo `finishedAt`. Exigir que ele envie `title` ou `deadLine` seria uma violação do princípio do método HTTP PATCH (atualização parcial).
3. Na resposta, o cliente precisa visualizar todos os dados atualizados para confirmar a operação realizada.

### Solução Proposta e Implementada:
Separamos os DTOs de acordo com a direção do fluxo de dados e o caso de uso:

1. **`TodoCreateDTO` (Entrada - Criação):**
   - Utilizado no `POST /api/todos`.
   - Contém apenas `title` e `deadLine`.
   - **Garantia:** O cliente não consegue forçar valores para `id`, `createdAt` ou `finishedAt`.

2. **`TodoConclusaoDTO` (Entrada - Atualização de Conclusão):**
   - Utilizado no `PATCH /api/todos/{id}/conclusao`.
   - Contém estritamente o atributo `finishedAt`.
   - **Garantia:** Permite atualizar apenas a data de conclusão, preservando intocados o título, prazo, data de criação e identificador.

3. **`TodoResponseDTO` (Saída / Resposta):**
   - Utilizado em **todas as respostas da API** que devolvem dados de tarefas (`POST`, `PATCH`, `GET`, `GET /concluidas`, `GET /pendentes`).
   - Contém todos os atributos da tarefa: `id`, `title`, `createdAt`, `deadLine` e `finishedAt`.
   - **Garantia:** Permite ao cliente conferir instantaneamente na resposta HTTP a data de conclusão registrada e o estado completo da tarefa.

---

## 🔄 Caminho de uma Requisição

Exemplo do fluxo passo a passo de uma requisição `PATCH /api/todos/1/conclusao`:

```
1. Cliente HTTP (Thunder Client)
   Envia: PATCH http://localhost:8080/api/todos/1/conclusao
   Corpo JSON: {"finishedAt": "2026-10-22T10:30:00"}
              │
              ▼
2. TodoApiController
   - Captura a URL com @PatchMapping("/{id}/conclusao")
   - Extrai o id (1) via @PathVariable Long id
   - Converte o JSON para TodoConclusaoDTO via @RequestBody
   - Invoca: service.marcarConclusao(id, dto)
              │
              ▼
3. TodoService
   - Busca a entidade existente: repository.findById(id).orElseThrow()
   - Modifica somente o campo de conclusão: todo.setFinishedAt(dto.getFinishedAt())
   - Salva a alteração: repository.save(todo)
   - Instancia o DTO de resposta: new TodoResponseDTO(atualizado)
   - Retorna o DTO para o Controller
              │
              ▼
4. TodoRepository & Banco de Dados
   - O Spring Data JPA executa o comando SQL UPDATE na tabela todo
              │
              ▼
5. Resposta HTTP
   - O Controller empacota o DTO em ResponseEntity.ok(atualizado)
   - O Spring converte o objeto para JSON e envia ao cliente com Status 200 OK
```

---

## 📡 Documentação dos Endpoints e Exemplos de Requisição

### 1. Inserir uma Tarefa
- **Método:** `POST`
- **Rota:** `/api/todos`
- **Cabeçalho:** `Content-Type: application/json`
- **Corpo da Requisição (JSON):**
```json
{
  "title": "Estudar Spring Boot",
  "deadLine": "2026-10-30T18:00:00"
}
```
- **Status HTTP de Resposta:** `201 Created`
- **Corpo da Resposta (JSON):**
```json
{
  "id": 1,
  "title": "Estudar Spring Boot",
  "createdAt": "2026-11-01T23:15:00",
  "deadLine": "2026-10-30T18:00:00",
  "finishedAt": null
}
```

---

### 2. Informar a Conclusão de uma Tarefa
- **Método:** `PATCH`
- **Rota:** `/api/todos/{id}/conclusao` (ex.: `/api/todos/1/conclusao`)
- **Cabeçalho:** `Content-Type: application/json`
- **Corpo da Requisição (JSON):**
```json
{
  "finishedAt": "2026-10-22T10:30:00"
}
```
- **Status HTTP de Resposta:** `200 OK`
- **Corpo da Resposta (JSON):**
```json
{
  "id": 1,
  "title": "Estudar Spring Boot",
  "createdAt": "2026-11-01T23:15:00",
  "deadLine": "2026-10-30T18:00:00",
  "finishedAt": "2026-10-22T10:30:00"
}
```

---

### 3. Listar Tarefas Pendentes
- **Método:** `GET`
- **Rota:** `/api/todos/pendentes`
- **Condição:** Tarefas com `finishedAt == null`
- **Status HTTP de Resposta:** `200 OK`
- **Corpo da Resposta (JSON):**
```json
[
  {
    "id": 2,
    "title": "Fazer exercícios de POO",
    "createdAt": "2026-11-01T23:16:00",
    "deadLine": "2026-11-05T20:00:00",
    "finishedAt": null
  }
]
```
*(Se não houver tarefas pendentes, retorna `[]`)*

---

### 4. Listar Tarefas Concluídas
- **Método:** `GET`
- **Rota:** `/api/todos/concluidas`
- **Condição:** Tarefas com `finishedAt` preenchido
- **Status HTTP de Resposta:** `200 OK`
- **Corpo da Resposta (JSON):**
```json
[
  {
    "id": 1,
    "title": "Estudar Spring Boot",
    "createdAt": "2026-11-01T23:15:00",
    "deadLine": "2026-10-30T18:00:00",
    "finishedAt": "2026-10-22T10:30:00"
  }
]
```
*(Se não houver tarefas concluídas, retorna `[]`)*

---

### 5. Listar Todas as Tarefas
- **Método:** `GET`
- **Rota:** `/api/todos`
- **Status HTTP de Resposta:** `200 OK`
- **Corpo da Resposta (JSON):**
```json
[
  {
    "id": 1,
    "title": "Estudar Spring Boot",
    "createdAt": "2026-11-01T23:15:00",
    "deadLine": "2026-10-30T18:00:00",
    "finishedAt": "2026-10-22T10:30:00"
  },
  {
    "id": 2,
    "title": "Fazer exercícios de POO",
    "createdAt": "2026-11-01T23:16:00",
    "deadLine": "2026-11-05T20:00:00",
    "finishedAt": null
  }
]
```

---

### 6. Excluir uma Tarefa
- **Método:** `DELETE`
- **Rota:** `/api/todos/{id}` (ex.: `/api/todos/1`)
- **Status HTTP de Resposta:** `204 No Content`
- **Corpo da Resposta:** Vazio (sem corpo)

---

## 🧪 Roteiro de Testes no Thunder Client

Siga exatamente o roteiro solicitado na avaliação prática:

1. **Cadastrar duas tarefas:**
   - Faça `POST /api/todos` com:
     `{"title": "Tarefa 1", "deadLine": "2026-11-10T12:00:00"}`
   - Faça `POST /api/todos` com:
     `{"title": "Tarefa 2", "deadLine": "2026-11-15T18:00:00"}`
   - Verifique o status `201 Created` e os IDs gerados (ex.: 1 e 2).
2. **Informar a conclusão de uma delas:**
   - Faça `PATCH /api/todos/1/conclusao` com:
     `{"finishedAt": "2026-10-22T10:30:00"}`
   - Verifique o status `200 OK` e confirme que a resposta possui a data registrada.
3. **Consultar as concluídas e as pendentes:**
   - Faça `GET /api/todos/concluidas`: deve retornar apenas a Tarefa 1.
   - Faça `GET /api/todos/pendentes`: deve retornar apenas a Tarefa 2.
4. **Conferir a data de conclusão:**
   - Na lista de concluídas, confirme que `finishedAt` está exatamente com `"2026-10-22T10:30:00"`.
5. **Excluir uma tarefa e conferir a listagem:**
   - Faça `DELETE /api/todos/2`: verifique o status `204 No Content`.
   - Faça `GET /api/todos`: a Tarefa 2 não deve mais aparecer.

---

## 🚀 Como Executar o Projeto e os Testes

### Pré-requisitos
- JDK 21 instalado e configurado no PATH
- MySQL em execução com o banco criado:
  ```sql
  CREATE DATABASE IF NOT EXISTS todolistif;
  ```

### Configuração do Banco (`src/main/resources/application.properties`)
```properties
spring.application.name=todolistif
spring.datasource.username=aluno
spring.datasource.password=aluno
spring.datasource.url=jdbc:mysql://localhost:3306/todolistif

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Executar a Aplicação
Entre na pasta `todolistif-api` e execute:
- **Windows (PowerShell):**
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **Linux/macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

A API estará acessível em: `http://localhost:8080/api/todos`

### Executar os Testes Automatizados
O projeto conta com suíte de testes completa cobrindo Controller, Service e contexto da aplicação:
```powershell
.\mvnw.cmd test
```
*Observação: Os testes utilizam banco H2 em memória e Mockito, executando de forma rápida e independente do MySQL estar ativo.*