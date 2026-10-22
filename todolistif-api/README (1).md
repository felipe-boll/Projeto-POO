# TodoList com Spring Boot: primeiros passos da API REST

Tutorial da aula de 15/10/2026 — IFPR, 3º ano de Informática.

Nesta etapa, vamos criar um projeto independente para a API REST, configurar a conexão com o MySQL e implementar a entidade `Todo`, o repositório e o DTO de resposta usando Lombok.

**Ponto de parada da aula:** `TodoRespostaDTO`. Service e Controller REST serão implementados na continuação. Portanto, ao concluir este tutorial, ainda não haverá um endpoint para consultar tarefas.

## 1. O que muda em relação ao projeto MVC?

No projeto anterior, o Controller utilizava o Thymeleaf para entregar páginas HTML ao navegador. Na API, o Controller devolverá dados, geralmente em JSON, para um cliente como um frontend, aplicativo ou Thunder Client.

| Aspecto | Projeto MVC com Thymeleaf | Novo projeto de API REST |
| --- | --- | --- |
| Resposta principal | Página HTML | Dados em JSON |
| Controller | `@Controller` | `@RestController` |
| Templates | Páginas processadas pelo Thymeleaf | Não utilizados neste projeto |
| Entidade e banco | Representam e armazenam as tarefas | Continuam necessários |

Uma API também pode ter um pacote chamado `model`. Deixar de montar páginas HTML não elimina as entidades do domínio.

## 2. Antes de começar

Precisamos de:

- JDK 21.
- VS Code com suporte ao desenvolvimento Java, por exemplo, Extension Pack for Java.
- MySQL instalado e em execução.
- Usuário e senha para acessar o MySQL.
- Acesso à internet para baixar as dependências do projeto.

Confira a versão do Java no terminal:

```bash
java -version
```

O projeto deste tutorial utiliza Java 21. Confira também se o VS Code está usando esse JDK para o projeto.

## 3. Criar o projeto no Spring Initializr

Acesse [Spring Initializr](https://start.spring.io/).

Preencha:

| Campo | Valor |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | Uma versão estável compatível com Java 21, sem SNAPSHOT ou milestone |
| Group | `br.edu.ifpr` |
| Artifact | `todolistif` |
| Name | `todolistif` |
| Package name | `br.edu.ifpr.todolistif` |
| Packaging | Jar |
| Java | 21 |

Em **Add Dependencies**, selecione:

| Dependência | Finalidade |
| --- | --- |
| Spring Web | Atender requisições HTTP na API que criaremos |
| Spring Data JPA | Trabalhar com persistência e repositórios |
| MySQL Driver | Conectar a aplicação ao MySQL |
| Lombok | Gerar métodos repetitivos a partir de anotações |
| Spring Boot DevTools | Facilitar o desenvolvimento com reinício automático |

Para esta API, não adicione Thymeleaf.

Clique em **Generate**, extraia o ZIP e, no VS Code, use **File → Open Folder** para abrir a pasta que contém o arquivo `pom.xml`.

Aguarde o carregamento do projeto e o download das dependências. O `pom.xml` descreve o projeto e suas dependências. Não precisamos instalar o Maven separadamente para usar os scripts Maven Wrapper fornecidos pelo Initializr.

### Se você esqueceu o Lombok

Adicione o trecho abaixo dentro de `<dependencies>` no `pom.xml`, apenas se a dependência ainda não existir:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

Depois, salve o arquivo e aguarde a atualização das dependências no VS Code.

## 4. Preparar o banco de dados

No cliente MySQL, conectado com seu usuário, execute:

```sql
CREATE DATABASE IF NOT EXISTS todolistif;
```

Vamos criar o banco manualmente e deixar o Hibernate criar a tabela a partir da entidade Java.

Abra `src/main/resources/application.properties` e configure:

```properties
spring.application.name=todolistif

spring.datasource.url=jdbc:mysql://localhost:3306/todolistif
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Os valores entre `${...}` permitem usar variáveis de ambiente. Sem `DB_USER`, o usuário será `root`. Sem `DB_PASSWORD`, a senha será vazia. Se seu MySQL utiliza senha, defina essa variável antes de executar ou substitua a linha localmente por `spring.datasource.password=SUA_SENHA`. Use as credenciais do laboratório e não publique uma senha real no GitHub.

| Propriedade | Significado |
| --- | --- |
| `spring.datasource.url` | Endereço, porta e nome do banco |
| `spring.datasource.username` | Usuário do MySQL |
| `spring.datasource.password` | Senha desse usuário |
| `spring.jpa.hibernate.ddl-auto=update` | Atualiza a estrutura das tabelas a partir das entidades durante o desenvolvimento |
| `spring.jpa.show-sql=true` | Exibe no terminal os comandos SQL executados |

`update` não cria o banco MySQL nem substitui uma estratégia de migrações para produção. Aqui, utilizamos essa opção para acompanhar a aula.

Se o projeto anterior usa o mesmo banco, as duas aplicações acessarão os mesmos dados. Para manter os dados separados, crie outro banco e ajuste o nome na URL.

## 5. Criar os pacotes

Dentro de `src/main/java/br/edu/ifpr/todolistif`, crie os pacotes:

- `model`
- `repository`
- `dto`

Mantenha esses pacotes abaixo de `br.edu.ifpr.todolistif`, onde está a classe principal `TodolistifApplication`. Essa organização permite que a configuração padrão do Spring encontre as classes do projeto.

| Arquivo | Caminho a partir da raiz do projeto |
| --- | --- |
| Classe principal | `src/main/java/br/edu/ifpr/todolistif/TodolistifApplication.java` |
| Entidade | `src/main/java/br/edu/ifpr/todolistif/model/Todo.java` |
| Repositório | `src/main/java/br/edu/ifpr/todolistif/repository/TodoRepository.java` |
| DTO de resposta | `src/main/java/br/edu/ifpr/todolistif/dto/TodoRespostaDTO.java` |

## 6. Implementar a entidade Todo

A entidade representa a tarefa que será persistida no banco.

| Campo | Tipo Java | Significado |
| --- | --- | --- |
| `id` | `Long` | Identificador da tarefa |
| `title` | `String` | Título ou descrição da tarefa |
| `createdAt` | `LocalDateTime` | Data e hora de criação |
| `deadLine` | `LocalDateTime` | Prazo para conclusão |
| `finishedAt` | `LocalDateTime` | Data e hora de conclusão |

Crie `Todo.java` no pacote `model`:

```java
package br.edu.ifpr.todolistif.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private LocalDateTime deadLine;

    private LocalDateTime finishedAt;

    public Todo() {
        this.createdAt = LocalDateTime.now();
    }

    public void markAsFinished() {
        this.finishedAt = LocalDateTime.now();
    }
}
```

### Entendendo as anotações de persistência

| Anotação | Função |
| --- | --- |
| `@Entity` | Identifica a classe como uma entidade JPA |
| `@Id` | Identifica a chave primária |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Configura o identificador para usar o valor gerado pelo banco na inserção |
| `@Column(nullable = false)` | Informa que a coluna não aceita valores nulos |

Como `@Id` está no atributo, o JPA usa acesso aos campos e não exige setters públicos para todos eles.

`nullable = false` não impede uma string vazia e não produz, sozinho, uma mensagem de validação amigável para a API. Vamos trabalhar essas regras na continuação.

### Entendendo o Lombok

`@Getter` na classe gera os getters de todos os campos. Por exemplo, podemos chamar `todo.getTitle()` mesmo sem escrever esse método manualmente.

`@Setter` foi colocado somente em `title` e `deadLine`, gerando `setTitle()` e `setDeadLine()`. Neste exemplo, escolhemos controlar os demais campos por outros mecanismos.

O Lombok gera esses métodos durante a compilação. O arquivo `.java` continua exibindo as anotações, sem acrescentar visualmente os métodos ao texto do arquivo.

### Quem preenche cada campo?

| Campo | Como recebe seu valor |
| --- | --- |
| `id` | O banco gera na inserção, e o JPA atribui à entidade |
| `title` | O código atribui usando `setTitle()` |
| `deadLine` | O código atribui usando `setDeadLine()` |
| `createdAt` | O construtor preenche ao executar `new Todo()` |
| `finishedAt` | Começa como `null` e recebe um valor quando chamamos `markAsFinished()` |

Mantivemos o construtor sem parâmetros escrito manualmente porque ele contém o preenchimento de `createdAt`. Não precisamos acrescentar `@NoArgsConstructor` à entidade.

`LocalDateTime` representa uma data e hora sem fuso horário. Vamos utilizá-lo para manter o mesmo modelo do nosso exemplo.

## 7. Implementar o Repository

Crie a **interface** `TodoRepository.java` no pacote `repository`:

```java
package br.edu.ifpr.todolistif.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpr.todolistif.model.Todo;

public interface TodoRepository
        extends JpaRepository<Todo, Long> {

}
```

O Repository será o ponto de acesso às tarefas no banco.

Em `JpaRepository<Todo, Long>`:

- `Todo` indica a entidade com a qual o repositório trabalha.
- `Long` indica o tipo da chave primária dessa entidade.

Ao estender `JpaRepository`, a interface recebe operações como:

| Método | Finalidade |
| --- | --- |
| `findAll()` | Consultar todas as tarefas |
| `findById(id)` | Procurar uma tarefa pelo identificador |
| `save(todo)` | Salvar uma entidade nova ou persistir alterações |
| `delete(todo)` | Excluir a entidade |

Não precisamos escrever uma classe implementando essa interface. O Spring Data fornece a implementação em tempo de execução. Também não precisamos acrescentar `@Repository` a essa interface para o funcionamento padrão do Spring Data JPA.

Criar a interface não executa essas operações automaticamente. Na próxima etapa, o Service chamará os métodos do Repository.

## 8. Implementar o DTO de resposta

DTO significa **Data Transfer Object**, ou Objeto de Transferência de Dados.

Vamos definir os dados que a API devolverá ao consultar uma tarefa. Crie `TodoRespostaDTO.java` no pacote `dto`:

```java
package br.edu.ifpr.todolistif.dto;

import java.time.LocalDateTime;

import br.edu.ifpr.todolistif.model.Todo;
import lombok.Getter;

@Getter
public class TodoRespostaDTO {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final LocalDateTime deadLine;
    private final LocalDateTime finishedAt;

    public TodoRespostaDTO(Todo todo) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.createdAt = todo.getCreatedAt();
        this.deadLine = todo.getDeadLine();
        this.finishedAt = todo.getFinishedAt();
    }
}
```

### O que esse construtor faz?

O construtor recebe uma entidade `Todo` e copia os campos que escolhemos devolver:

```java
TodoRespostaDTO resposta = new TodoRespostaDTO(tarefa);
```

Nesse trecho ilustrativo, `tarefa` deve ser uma entidade já disponível no código. O DTO copia os valores existentes, sem consultar o banco e sem gerar um identificador.

O construtor continua escrito manualmente porque realiza a conversão de `Todo` para `TodoRespostaDTO`. `@Getter` gera os métodos usados para ler os dados da resposta.

Os atributos são `final`: recebem seus valores no construtor e não podem ser reatribuídos depois. Não precisamos gerar setters nesse DTO.

### Entidade e DTO têm funções diferentes

| Entidade `Todo` | `TodoRespostaDTO` |
| --- | --- |
| Representa a tarefa no domínio e possui mapeamento JPA | Representa os dados escolhidos para a resposta |
| Possui `@Entity`, `@Id` e outras anotações de persistência | Não possui mapeamento de persistência |
| Participa das operações do Repository | Transporta dados para o cliente da API |
| Possui comportamentos como `markAsFinished()` | Reúne os valores que serão apresentados na resposta |

Os dois possuem campos semelhantes nesta etapa, mas essa igualdade não é uma exigência. Podemos escolher campos diferentes para a resposta sem alterar a tabela.

**O DTO ainda não devolve JSON sozinho.** Na continuação, o Controller retornará o DTO, e o Spring utilizará um conversor para produzir o JSON HTTP.

## 9. Executar e conferir esta etapa

Salve os arquivos e abra `TodolistifApplication.java`, a classe principal gerada pelo Initializr:

```java
package br.edu.ifpr.todolistif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodolistifApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodolistifApplication.class, args);
    }
}
```

Use **Run** sobre o método `main` no VS Code. Outra opção é executar pelo terminal na pasta do `pom.xml`.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS ou Linux:**

```bash
./mvnw spring-boot:run
```

Se houver erro de permissão no macOS/Linux:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Confira se a aplicação inicia sem falhas. Com o MySQL acessível e a configuração correta, o Hibernate criará ou atualizará a tabela associada à entidade `Todo`.

No cliente MySQL, você pode conferir:

```sql
USE todolistif;
SHOW TABLES;
DESCRIBE todo;
```

Na configuração padrão, a tabela será `todo`. Os nomes das colunas podem aparecer em snake_case, como `created_at`, conforme a estratégia de nomes utilizada pelo Hibernate.

Nesta etapa, a aplicação não cadastra tarefas automaticamente e ainda não oferece `/api/todos`. Abrir esse endereço ou a raiz `/` pode retornar 404. Isso é esperado, pois ainda não implementamos o Controller.

## 10. Problemas comuns

| Situação | O que conferir |
| --- | --- |
| Erro de conexão com MySQL | Se o servidor está em execução e se endereço, porta e credenciais estão corretos |
| `Unknown database` | Se você criou o banco com o mesmo nome da URL |
| `Access denied` | Usuário, senha e permissões no MySQL |
| Porta 8080 ocupada | Pare a execução do projeto anterior antes de iniciar este |
| `getTitle()` ou outro getter não reconhecido | Confira a dependência Lombok, os imports e o carregamento do projeto Java no VS Code |
| Import de persistência não encontrado | Confira Spring Data JPA e os imports `jakarta.persistence` |
| Tabela não aparece | Confira os logs, a conexão, `ddl-auto=update` e o pacote da entidade |

Se a compilação funciona pelo Maven, mas o VS Code continua indicando erro em métodos gerados pelo Lombok, use a paleta de comandos e execute **Java: Clean Java Language Server Workspace**, confirmando o recarregamento do ambiente Java.

## 11. Conferência do que foi feito

- [ ] Criei o projeto Maven com Java 21.
- [ ] Incluí Spring Web, Spring Data JPA, MySQL Driver, Lombok e DevTools.
- [ ] Criei o banco e configurei a conexão.
- [ ] Implementei `Todo` com anotações JPA e Lombok.
- [ ] Entendi quem preenche cada campo da tarefa.
- [ ] Criei a interface `TodoRepository`.
- [ ] Implementei `TodoRespostaDTO` e seu construtor de conversão.
- [ ] Executei a aplicação e conferi a tabela no banco.

## 12. Perguntas para revisar

1. Por que usamos `@Entity` em `Todo` e não em `TodoRespostaDTO`?
2. O que os tipos `Todo` e `Long` representam em `JpaRepository<Todo, Long>`?
3. Quais métodos o Lombok gera em cada uma das nossas classes?
4. Por que mantivemos o construtor de `Todo` escrito manualmente?
5. Por que `finishedAt` começa como `null`?
6. Qual é a função do construtor `TodoRespostaDTO(Todo todo)`?
7. Por que ainda não conseguimos consultar tarefas pelo Thunder Client?

## Continuação

Na próxima etapa, implementaremos o `TodoService` e o `TodoApiController` para disponibilizar `GET /api/todos`. Depois, criaremos o DTO de entrada e o cadastro com POST. O tratamento de exceções será abordado em uma aula própria.
