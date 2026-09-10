<div align="center">

# Nexus API

### Gerenciador e planejador de estudos

Uma API REST para organizar matérias, registrar sessões de estudo e acompanhar o progresso de cada objetivo.

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

## Sobre o projeto

O Nexus nasceu para centralizar a rotina de estudos em um único lugar. Cada usuário pode cadastrar suas matérias, definir metas em horas, iniciar e finalizar sessões e consultar quanto já avançou em cada objetivo.

A API foi construída com Spring Boot e segue uma arquitetura em camadas. A autenticação é stateless, feita com JWT, e os dados de matérias e sessões são isolados pelo usuário autenticado.

## Funcionalidades

- Cadastro e autenticação de usuários
- Autenticação stateless com JSON Web Token (JWT)
- Criação, consulta, atualização e exclusão de matérias
- Definição de metas de estudo em horas
- Inicialização e finalização de sessões de estudo
- Histórico paginado de sessões por matéria
- Cálculo de horas estudadas, horas restantes e percentual concluído
- Isolamento de dados pelo usuário autenticado
- Validação das requisições e respostas de erro padronizadas
- Documentação interativa com Swagger UI
- Versionamento do banco de dados com Flyway
- Testes unitários com JUnit e Mockito
- Testes de integração com PostgreSQL executado por Testcontainers

## Tecnologias

| Categoria | Tecnologias |
| --- | --- |
| Linguagem | Java 17 |
| Framework | Spring Boot 4.1, Spring MVC |
| Persistência | Spring Data JPA, Hibernate |
| Banco de dados | PostgreSQL 15 |
| Segurança | Spring Security, JWT, BCrypt |
| Validação | Jakarta Bean Validation |
| Migrações | Flyway |
| Documentação | Springdoc OpenAPI / Swagger UI |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers |
| Build | Maven Wrapper |
| Ambiente local | Docker Compose |

## Arquitetura

O fluxo principal da aplicação segue estas camadas:

```text
HTTP Request
    │
    ▼
Controller ──► DTO / Validation
    │
    ▼
Service ──► Regras de negócio e autorização
    │
    ▼
Repository ──► JPA / Hibernate
    │
    ▼
PostgreSQL
```

Estrutura principal do código:

```text
src/
├── main/
│   ├── java/com/devcaiqueoliveira/nexus_api/
│   │   ├── config/       # Segurança, CORS e OpenAPI
│   │   ├── controller/   # Endpoints REST
│   │   ├── dto/          # Contratos de entrada e saída
│   │   ├── entity/       # Entidades JPA
│   │   ├── exception/    # Exceções e tratamento global
│   │   ├── repository/   # Acesso ao banco de dados
│   │   ├── security/     # Filtro de autenticação JWT
│   │   └── service/      # Regras de negócio
│   └── resources/
│       └── db/migration/ # Migrações Flyway
└── test/                 # Testes unitários e de integração
```

## Como executar

### Pré-requisitos

- Java 17
- Docker com Docker Compose
- Git

O projeto inclui o Maven Wrapper, portanto não é necessário instalar o Maven globalmente.

### 1. Clone o repositório

```bash
git clone https://github.com/devcaiqueoliveira/nexus.git
cd nexus
```

### 2. Inicie o PostgreSQL

```bash
docker compose up -d
```

O Docker Compose cria o banco `nexus_db` na porta `5432`. As tabelas são criadas automaticamente pelo Flyway quando a aplicação inicia.

### 3. Configure o segredo do JWT

No Linux ou macOS:

```bash
export JWT_SECRET="troque-por-uma-chave-longa-e-segura"
```

No PowerShell:

```powershell
$env:JWT_SECRET="troque-por-uma-chave-longa-e-segura"
```

### 4. Execute a aplicação

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Documentação da API

Com a aplicação em execução, acesse:

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Especificação OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Autenticação

Somente o cadastro e o login são públicos. Os demais endpoints exigem um token JWT no header `Authorization`.

### Criar uma conta

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ada Lovelace",
    "email": "ada@example.com",
    "password": "senha-segura"
  }'
```

### Fazer login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ada@example.com",
    "password": "senha-segura"
  }'
```

Resposta:

```json
{
  "token": "seu-token-jwt"
}
```

Use o token nas próximas requisições:

```http
Authorization: Bearer seu-token-jwt
```

### Criar uma matéria

```bash
curl -X POST http://localhost:8080/api/subjects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer seu-token-jwt" \
  -d '{
    "name": "Estruturas de Dados",
    "description": "Árvores, grafos e algoritmos de busca",
    "targetHours": 40
  }'
```

## Endpoints

### Usuários e autenticação

| Método | Endpoint | Descrição | Acesso |
| --- | --- | --- | --- |
| `POST` | `/api/users` | Cadastra um usuário | Público |
| `POST` | `/api/auth/login` | Autentica e retorna um JWT | Público |
| `GET` | `/api/users/{id}` | Consulta um usuário | Autenticado |

### Matérias

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/subjects` | Cria uma matéria |
| `GET` | `/api/subjects` | Lista as matérias do usuário autenticado |
| `GET` | `/api/subjects/{id}` | Consulta uma matéria |
| `PUT` | `/api/subjects/{id}` | Atualiza uma matéria |
| `DELETE` | `/api/subjects/{id}` | Exclui uma matéria |
| `GET` | `/api/subjects/{id}/progress` | Consulta o progresso da matéria |

### Sessões de estudo

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/study-sessions` | Inicia uma sessão |
| `PATCH` | `/api/study-sessions/{id}/finish` | Finaliza uma sessão em andamento |
| `GET` | `/api/study-sessions/{id}` | Consulta uma sessão |
| `DELETE` | `/api/study-sessions/{id}` | Exclui uma sessão |
| `GET` | `/api/study-sessions?subjectId={id}` | Lista as sessões de uma matéria com paginação |

Todos os endpoints de matérias e sessões exigem autenticação.

## Testes

Os testes unitários são executados pelo Maven Surefire. Os testes de integração seguem o padrão `*IT` e são executados pelo Maven Failsafe com um PostgreSQL temporário gerenciado pelo Testcontainers.

```bash
./mvnw test
```

Para executar a suíte completa, incluindo os testes de integração, mantenha o Docker ativo e use:

```bash
./mvnw verify
```

Para gerar o pacote da aplicação:

```bash
./mvnw clean package
```

## Banco de dados

As alterações de estrutura são versionadas em `src/main/resources/db/migration` e aplicadas automaticamente pelo Flyway.

O modelo atual possui três entidades principais:

```text
User 1 ─── N Subject 1 ─── N StudySession
```

O volume `nexus-db-data` mantém os dados do PostgreSQL entre reinicializações do container. Para encerrar o ambiente local:

```bash
docker compose down
```

## Roadmap

Os próximos passos incluem:

- ampliar os testes de integração dos controllers;
- adicionar auditoria às entidades;
- configurar CI/CD e deploy em nuvem;
- criar um sistema de sequência diária de estudos;
- gerar relatórios por semana, mês ou intervalo personalizado.

Consulte o [roadmap completo](./GITHUB_ISSUES.md) para acompanhar o planejamento técnico e as funcionalidades futuras.

## Autor

Desenvolvido por [Caique Oliveira](https://github.com/devcaiqueoliveira).
