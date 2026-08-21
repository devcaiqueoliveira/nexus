# 📋 Backlog de Issues - Nexus API

Este documento consolida todos os pontos de correção, segurança, arquitetura e melhorias identificados no projeto **Nexus API**, organizados por ordem decrescente de prioridade (**P0 - Crítico** até **P3 - Melhorias e Novas Features**). Cada item está formatado com template pronto para ser aberto como uma **Issue no GitHub**.

---

## 📑 Índice de Issues

| Prioridade | ID | Título | Tipo |
| :--- | :--- | :--- | :--- |
| 🔴 **P0 - Crítico** | [#1](#issue-01---fix-adicionar-pathvariable-no-endpoint-de-finalizao-de-sesso) | [BUG] Adicionar `@PathVariable` no endpoint de finalização de sessão de estudo | Bug |
| 🔴 **P0 - Crítico** | [#2](#issue-02---fix-retornar-instncia-persistida-no-studysessionservice) | [BUG] Retornar instância persistida no `StudySessionService.startSession` | Bug |
| 🔴 **P0 - Crítico** | [#3](#issue-03---security-criptografar-senhas-de-usurios-com-passwordencoder) | [SECURITY] Criptografia de senhas no cadastro de usuários (`PasswordEncoder`) | Segurança |
| 🔴 **P0 - Crítico** | [#4](#issue-04---fix-tratamento-global-de-excees-de-negcio-e-integridade-404-400-409) | [BUG] Tratamento global de exceções de negócio e integridade (404, 400, 409) | Bug / Handler |
| 🔴 **P0 - Crítico** | [#5](#issue-05---validation-verificao-e-tratamento-para-e-mail-duplicado) | [VALIDATION] Verificação e tratamento de erro amigável para e-mail duplicado | Validação |
| 🟠 **P1 - Alta** | [#6](#issue-06---validation-adicionar-anotaes-de-validao-valid-e-bean-validation-nos-dtos) | [VALIDATION] Adicionar anotações de validação (`@Valid` e Bean Validation) nos DTOs | Validação |
| 🟠 **P1 - Alta** | [#7](#issue-07---rule-impedir-mltiplas-sesses-de-estudo-simultneas-ativas) | [RULE] Impedir criação de múltiplas sessões de estudo ativas simultâneas | Regra de Negócio |
| 🟠 **P1 - Alta** | [#8](#issue-08---arch-adicionar-anotao-transactional-nos-servios-userservice-e-subjectservice) | [ARCH] Adicionar anotação `@Transactional` nos serviços `UserService` e `SubjectService` | Arquitetura |
| 🟡 **P2 - Média** | [#9](#issue-09---config-centralizar-configurao-de-cors-globalmente) | [CONFIG] Centralizar configuração de CORS globalmente via `WebMvcConfigurer` | Configuração |
| 🟡 **P2 - Média** | [#10](#issue-10---refactor-padronizar-criao-de-entidades-evitando-construtores-com-null) | [REFACTOR] Padronizar criação de entidades evitando construtores com argumentos `null` | Refatoração |
| 🟡 **P2 - Média** | [#11](#issue-11---config-ajustar-e-padronizar-verso-do-spring-boot-no-pomxml) | [CONFIG] Ajustar e padronizar versão do Spring Boot no `pom.xml` | Configuração |
| 🟢 **P3 - Baixa** | [#12](#issue-12---feat-implementar-endpoints-de-consulta-get-edio-put-e-excluso-delete) | [FEAT] Implementar endpoints de Consulta (GET), Edição (PUT) e Exclusão (DELETE) | Feature |
| 🟢 **P3 - Baixa** | [#13](#issue-13---feat-endpoint-de-mtricas-e-progresso-de-estudo-vs-meta) | [FEAT] Criar endpoint de progresso e métricas de horas estudadas vs meta | Feature |

---

## 🔴 Prioridade 0 (P0 - Crítica / Bloqueante & Segurança)

### Issue #01 - [FIX] Adicionar `@PathVariable` no endpoint de finalização de sessão

**Labels:** `bug`, `backend`, `p0-critical`, `good-first-issue`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/controller/StudySessionController.java`

#### 📌 Descrição
No endpoint `PATCH /api/study-sessions/{id}/finish`, o parâmetro `id` no método `finish` não possui a anotação `@PathVariable("id")`. Sem essa anotação, o Spring Boot não realiza o binding do segmento da URL para o parâmetro do método, resultando em parâmetro `null` ou erro de requisição HTTP 400/500 ao tentar finalizar a sessão.

#### 🎯 Comportamento Esperado
O método deve receber o ID extraído diretamente da URL da requisição:

```java
@PatchMapping("/{id}/finish")
public ResponseEntity<StudySessionResponse> finish(@PathVariable("id") UUID id) {
    StudySessionResponse finishedStudySession = studySessionService.finishSession(id);
    return ResponseEntity.ok(finishedStudySession);
}
```

#### ✅ Critérios de Aceite
- [ ] O parâmetro `UUID id` no método `finish` está anotado com `@PathVariable("id")` (ou `@PathVariable UUID id`).
- [ ] Requisições `PATCH /api/study-sessions/{id}/finish` com um UUID válido extraem o ID corretamente e finalizam a sessão.
- [ ] Teste de integração/controller cobrindo a finalização de sessão via URL.

---

### Issue #02 - [FIX] Retornar instância persistida no `StudySessionService.startSession`

**Labels:** `bug`, `backend`, `p0-critical`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/StudySessionService.java`

#### 📌 Descrição
No método `startSession` do `StudySessionService`, a entidade persistida `savedSession` é ignorada no momento do retorno, sendo retornado o objeto original não gerenciado `studySession`.

```java
StudySession savedSession = studySessionRepository.save(studySession);
return new StudySessionResponse(studySession); // ❌ Retorna o objeto antes de persistir
```

Isso causa inconsistências, pois valores gerados pelo banco de dados ou callbacks do JPA (como o `@PrePersist` que preenche `createdAt` e `status`) podem não estar refletidos no DTO retornado.

#### 🎯 Comportamento Esperado
Retornar `new StudySessionResponse(savedSession)`.

#### ✅ Critérios de Aceite
- [ ] `startSession` instancia o DTO `StudySessionResponse` utilizando a variável `savedSession`.
- [ ] A resposta do endpoint contém o `id`, `createdAt`, `status` (`IN_PROGRESS`) devidamente preenchidos.

---

### Issue #03 - [SECURITY] Criptografar senhas de usuários com `PasswordEncoder`

**Labels:** `security`, `backend`, `p0-critical`  
**Arquivos afetados:**  
- `pom.xml`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/UserService.java`
- Nova classe de configuração de segurança (ex: `SecurityConfig.java`)

#### 📌 Descrição
Atualmente, as senhas dos usuários são salvas em texto puro (plain text) na tabela `users` (`request.password()`). Isso viola princípios básicos de segurança (OWASP A02:2021 - Cryptographic Failures) e conformidade com leis de proteção de dados (LGPD).

#### 🎯 Comportamento Esperado
1. Incluir `spring-boot-starter-security` (ou disponibilizar bean `BCryptPasswordEncoder`).
2. Criptografar a senha via `passwordEncoder.encode(request.password())` antes de persistir o usuário no banco de dados.

#### ✅ Critérios de Aceite
- [ ] Configurado um bean `PasswordEncoder` utilizando `BCryptPasswordEncoder`.
- [ ] O `UserService` injeta `PasswordEncoder` e realiza o hash da senha antes de chamar `userRepository.save()`.
- [ ] Senhas no banco de dados passam a ser armazenadas com hash BCrypt (prefixo `$2a$`, `$2b$`).
- [ ] Endpoint `POST /api/users` não expõe o hash da senha no `UserResponse`.

---

### Issue #04 - [FIX] Tratamento global de exceções de negócio e integridade (404, 400, 409)

**Labels:** `bug`, `backend`, `exception-handling`, `p0-critical`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/exception/GlobalExceptionHandler.java`
- Novos DTOs/Records de resposta de erro padronizada (ex: `StandardErrorResponse.java` ou `ProblemDetail`)

#### 📌 Descrição
O `GlobalExceptionHandler` trata apenas `MethodArgumentNotValidException`. Quando ocorrem as seguintes exceções:
1. `EntityNotFoundException` (ex: usuário ou disciplina não encontrados) -> A API retorna HTTP 500 Internal Server Error em vez de **HTTP 404 Not Found**.
2. `IllegalStateException` (ex: tentar finalizar sessão já finalizada) -> A API retorna HTTP 500 em vez de **HTTP 400 Bad Request** ou **HTTP 422 Unprocessable Entity**.
3. `DataIntegrityViolationException` (ex: violação de chave única ou estrangeira) -> A API retorna HTTP 500 em vez de **HTTP 409 Conflict** ou **HTTP 400 Bad Request**.

#### 🎯 Comportamento Esperado
Criar handlers no `@RestControllerAdvice` para capturar essas exceções e retornar códigos HTTP semânticos e mensagens claras no corpo da resposta (podendo adotar o padrão RFC 7807 / RFC 9457 `ProblemDetail` nativo do Spring).

#### ✅ Critérios de Aceite
- [ ] Handler para `EntityNotFoundException` retornando HTTP 404 com a mensagem da exceção.
- [ ] Handler para `IllegalStateException` (ou exceção de negócio customizada) retornando HTTP 400 / 422.
- [ ] Handler para `DataIntegrityViolationException` retornando HTTP 409 / 400 com mensagem amigável sem vazar dados brutos de SQL.
- [ ] Respostas de erro padronizadas contendo `status`, `timestamp`, `message` e `path`.

---

### Issue #05 - [VALIDATION] Verificação e tratamento de erro amigável para e-mail duplicado

**Labels:** `validation`, `backend`, `p0-critical`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/repository/UserRepository.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/UserService.java`

#### 📌 Descrição
A coluna `email` na tabela `users` possui restrição `UNIQUE`. Contudo, o `UserService` não realiza verificação prévia se o e-mail já existe. Ao tentar cadastrar um e-mail repetido, o banco dispara uma `DataIntegrityViolationException`, gerando erro 500 não tratado.

#### 🎯 Comportamento Esperado
1. Declarar `boolean existsByEmail(String email);` no `UserRepository`.
2. Validar no `UserService.createUser`: se existir, lançar uma exceção de conflito de negócio (ex: `DuplicateResourceException` ou `EmailAlreadyInUseException`).
3. Retornar status HTTP 409 Conflict.

#### ✅ Critérios de Aceite
- [ ] Método `existsByEmail(String email)` criado em `UserRepository`.
- [ ] `UserService` verifica a existência do e-mail antes de criar o usuário.
- [ ] Tentativa de cadastro com e-mail duplicado retorna HTTP 409 com mensagem explicativa `"O e-mail informado já está cadastrado."`.

---

## 🟠 Prioridade 1 (P1 - Alta / Integridade, Regras de Negócio e Transações)

### Issue #06 - [VALIDATION] Adicionar anotações de validação (`@Valid` e Bean Validation) nos DTOs

**Labels:** `validation`, `backend`, `p1-high`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/dto/SubjectRequest.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/dto/StudySessionStart.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/controller/SubjectController.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/controller/StudySessionController.java`
- `src/main/resources/messages.properties`

#### 📌 Descrição
Enquanto `UserRequest` e `UserController` possuem validação com Bean Validation e `@Valid`, os endpoints de disciplinas (`Subject`) e sessões de estudo (`StudySession`) não possuem validação. É possível enviar nomes vazios, carga horária negativa (`targetHours: -10`), ou IDs nulos (`userId: null`, `subjectId: null`).

#### 🎯 Comportamento Esperado
1. Em `SubjectRequest`:
   - `name`: `@NotBlank`, `@Size(max = 100)`
   - `targetHours`: `@NotNull`, `@Positive(message = "A meta de horas deve ser maior que zero")`
   - `userId`: `@NotNull`
2. Em `StudySessionStart`:
   - `subjectId`: `@NotNull`
3. Nos Controllers (`SubjectController.java:24`, `StudySessionController.java:22`):
   - Adicionar `@Valid` no `@RequestBody`.

#### ✅ Critérios de Aceite
- [ ] DTOs `SubjectRequest` e `StudySessionStart` devidamente anotados com validações do Jakarta Validation.
- [ ] Métodos dos Controllers recebem `@Valid @RequestBody`.
- [ ] Mensagens de validação internacionalizadas em `messages.properties`.
- [ ] Requisições com dados inválidos retornam HTTP 400 com lista detalhada de erros de campos.

---

### Issue #07 - [RULE] Impedir criação de múltiplas sessões de estudo ativas simultâneas

**Labels:** `business-rule`, `backend`, `p1-high`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/repository/StudySessionRepository.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/StudySessionService.java`

#### 📌 Descrição
Atualmente, um usuário pode abrir infinitas sessões de estudo (`status = IN_PROGRESS`) sem finalizar as anteriores. Isso gera dados inconsistentes e sessões "órfãs" acumuladas.

#### 🎯 Comportamento Esperado
Ao iniciar uma nova sessão (`startSession`):
1. Verificar se a disciplina (ou o usuário associado) já possui uma sessão com `StudySessionStatus.IN_PROGRESS`.
2. Caso exista, lançar uma exceção de regra de negócio (ex: `ActiveSessionAlreadyExistsException` ou `IllegalStateException`), impedindo o início da nova sessão até que a anterior seja concluída.

#### ✅ Critérios de Aceite
- [ ] Query adicionada em `StudySessionRepository`: `boolean existsBySubjectIdAndStatus(UUID subjectId, StudySessionStatus status)` (ou por usuário).
- [ ] `StudySessionService.startSession` bloqueia criação caso já exista sessão `IN_PROGRESS`.
- [ ] Retorno semântico HTTP 400/409 informando que já existe uma sessão em andamento.

---

### Issue #08 - [ARCH] Adicionar anotação `@Transactional` nos serviços `UserService` e `SubjectService`

**Labels:** `architecture`, `backend`, `p1-high`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/UserService.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/SubjectService.java`

#### 📌 Descrição
O `StudySessionService` está devidamente anotado com `@Transactional` do Spring, porém `UserService` e `SubjectService` não possuem anotações transacionais em seus métodos de escrita. Isso pode levar a inconsistências transacionais e problemas com transações não delimitadas adequadamente.

#### 🎯 Comportamento Esperado
Anotar classes ou métodos que executam mutações no banco de dados com `org.springframework.transaction.annotation.Transactional`.

#### ✅ Critérios de Aceite
- [ ] `@Transactional` adicionado nos métodos de criação/alteração de `UserService` e `SubjectService`.

---

## 🟡 Prioridade 2 (P2 - Média / Configuração, Arquitetura & Clean Code)

### Issue #09 - [CONFIG] Centralizar configuração de CORS globalmente

**Labels:** `configuration`, `backend`, `cors`, `p2-medium`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/controller/UserController.java`
- Nova classe de configuração (ex: `src/main/java/com/devcaiqueoliveira/nexus_api/config/WebConfig.java` ou `CorsConfig.java`)

#### 📌 Descrição
O `UserController` possui `@CrossOrigin(origins = "http://localhost:4200")`, enquanto `SubjectController` e `StudySessionController` não possuem. Quando o frontend (Angular/React) tentar consumir os endpoints de matérias ou sessões, as requisições serão bloqueadas por política de CORS do navegador.

#### 🎯 Comportamento Esperado
1. Remover `@CrossOrigin` dos Controllers individuais.
2. Criar uma classe `@Configuration` implementando `WebMvcConfigurer` para configurar CORS globalmente para todas as rotas `/api/**`, permitindo os métodos (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) e headers necessários.
3. Permitir configurar a origem permitida através do `application.yaml` (ex: `cors.allowed-origins: http://localhost:4200`).

#### ✅ Critérios de Aceite
- [ ] Classe `WebConfig` configurando CORS global para `/api/**`.
- [ ] `@CrossOrigin` removido dos controllers.
- [ ] Requisições com pre-flight `OPTIONS` respondem adequadamente para todos os endpoints.

---

### Issue #10 - [REFACTOR] Padronizar criação de entidades evitando construtores com argumentos `null`

**Labels:** `refactor`, `clean-code`, `p2-medium`  
**Arquivos afetados:**  
- `src/main/java/com/devcaiqueoliveira/nexus_api/entity/User.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/entity/Subject.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/entity/StudySession.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/UserService.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/SubjectService.java`
- `src/main/java/com/devcaiqueoliveira/nexus_api/service/StudySessionService.java`

#### 📌 Descrição
Nos Services, as entidades estão sendo instanciadas através do `@AllArgsConstructor` passando múltiplos valores `null` para campos gerados (ID, createdAt, status, endedAt):
```java
// Exemplo em SubjectService:
Subject createdSubject = new Subject(null, subjectRequest.name(), subjectRequest.description(), subjectRequest.targetHours(), null, user);
```
Isso é propenso a erros de posicionamento de parâmetros (ex: inverter nome com descrição) e polui o código de domínio.

#### 🎯 Comportamento Esperado
Adotar uma das seguintes abordagens limpas:
1. **Construtores de Negócio Específicos:** Construtor que recebe apenas os campos necessários para criação (ex: `new Subject(name, description, targetHours, user)`).
2. **Padrão Builder (`@Builder` do Lombok):** Facilitando a leitura explícita dos campos criados.

#### ✅ Critérios de Aceite
- [ ] Entidades refatoradas com construtores semânticos ou `@Builder`.
- [ ] Chamadas nos Services eliminam a passagem de múltiplos literais `null`.

---

### Issue #11 - [CONFIG] Ajustar e padronizar versão do Spring Boot no `pom.xml`

**Labels:** `configuration`, `maven`, `p2-medium`  
**Arquivos afetados:**  
- `pom.xml`

#### 📌 Descrição
No `pom.xml`, a versão do `spring-boot-starter-parent` está declarada como `4.1.0`. A linha estável atual do Spring Boot é a versão **3.x** (ex: 3.3.x / 3.4.x). O uso de uma versão inexistente/incompatível ou typo pode acarretar problemas de resolução de dependências em pipelines de CI/CD e ambientes de build limpos.

#### 🎯 Comportamento Esperado
Definir uma versão estável e compatível do Spring Boot 3.x (ex: `3.4.2` ou `3.3.5`).

#### ✅ Critérios de Aceite
- [ ] `pom.xml` atualizado com versão GA estável do Spring Boot 3.
- [ ] Projeto compila limpo com `mvn clean compile`.

---

## 🟢 Prioridade 3 (P3 - Baixa / Novas Funcionalidades e Completude)

### Issue #12 - [FEAT] Implementar endpoints de Consulta (GET), Edição (PUT) e Exclusão (DELETE)

**Labels:** `enhancement`, `feature`, `rest-api`, `p3-low`  
**Arquivos afetados:**  
- Controllers, Services e Repositories de `User`, `Subject` e `StudySession`

#### 📌 Descrição
A API atualmente possui apenas operações de criação (`POST`) e finalização pontual de sessão (`PATCH`). Para permitir o funcionamento completo do frontend, é necessário disponibilizar operações de leitura, atualização e deleção:
1. **Disciplinas (`Subject`):**
   - `GET /api/subjects?userId={userId}` (Listar disciplinas do usuário)
   - `GET /api/subjects/{id}` (Buscar disciplina por ID)
   - `PUT /api/subjects/{id}` (Editar nome, descrição, meta de horas)
   - `DELETE /api/subjects/{id}` (Remover disciplina)
2. **Sessões de Estudo (`StudySession`):**
   - `GET /api/study-sessions?subjectId={subjectId}` (Histórico de sessões de uma matéria)
   - `GET /api/study-sessions/{id}` (Detalhes de uma sessão)
   - `DELETE /api/study-sessions/{id}` (Cancelar/remover sessão)
3. **Usuários (`User`):**
   - `GET /api/users/{id}` (Buscar perfil de usuário)

#### ✅ Critérios de Aceite
- [ ] Endpoints implementados seguindo padrão RESTful.
- [ ] Suporte a paginação (`Pageable`) nas listagens de histórico.
- [ ] Retorno com DTOs de resposta adequados.

---

### Issue #13 - [FEAT] Endpoint de métricas e progresso de estudo vs meta

**Labels:** `enhancement`, `feature`, `analytics`, `p3-low`  
**Arquivos afetados:**  
- `StudySessionRepository.java`
- `SubjectService.java` / Novo `AnalyticsService.java`
- Novo DTO `SubjectProgressResponse.java`

#### 📌 Descrição
A entidade `Subject` possui o campo `targetHours` (meta de horas), e as sessões de estudo possuem `startedAt` e `endedAt`. Atualmente, a API não calcula o total de horas estudadas acumuladas e a porcentagem concluída em relação à meta.

#### 🎯 Comportamento Esperado
Criar endpoint `GET /api/subjects/{id}/progress` (ou incluir dados consolidados no `SubjectResponse`) contendo:
- `targetHours`: Horas planejadas.
- `totalHoursStudied`: Total de horas/minutos calculados a partir das sessões finalizadas (`COMPLETED`).
- `remainingHours`: Horas restantes para atingir a meta.
- `completionPercentage`: Porcentagem de conclusão (0 a 100%).

#### ✅ Critérios de Aceite
- [ ] Query no repositório para calcular a duração total das sessões `COMPLETED` de uma disciplina.
- [ ] Endpoint REST documentado no OpenAPI/Swagger retornando o progresso calculado.
- [ ] Cálculo protegido contra divisão por zero caso a meta seja zero ou nula.
