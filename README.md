# Auto Escola 3ESS – API REST (Spring Boot)

Projeto da disciplina **SOA e WebServices** (FIAP) – Checkpoint 5.
API ReST para agendamento de instruções de uma auto escola.

## Integrantes do grupo

| Nome | RM |
|------|----|
| _Danilo Wendler_ | _RM556602_ |
| _Italo Silva_ | _RM554758_ |
| _Julio Cesar_ | _RM554676_ |
| _Pedro Muzel_ | _RM555983_ |
| _Vitor Ismael_ | _RM556027_ |

## Tecnologias

Java 21 (compatível com 17+) · Spring Boot 4 · Spring Data JPA · Flyway · MySQL · Spring Security + JWT ·
springdoc-openapi (Swagger) · JUnit 5 / Mockito / MockMvc / H2 (testes)

## Como executar

1. Tenha um MySQL rodando em `localhost:3306` (o banco `autoescola3ess` é criado automaticamente
   pela URL de conexão com `createDatabaseIfNotExist=true`).
2. Ajuste (se necessário) as variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
   (valores padrão em `application.properties`: usuário `root`, senha `fiap`).
3. Rode: `./mvnw spring-boot:run` (ou execute `AutoEscola3EssApplication` pela IDE).
   As migrations do Flyway (V1 a V10) criam as tabelas e um **usuário ADMIN inicial**:
   login `admin` / senha `admin` (migration `V10__insert-usuario-admin.sql`).
4. Acesse o Swagger: `http://localhost:8080/swagger-ui.html`

## Roteiro de validação (Swagger + MySQL)

1. **Autenticação** – `POST /login` com `{"login": "admin", "senha": "admin"}` → copie o `tokenJWT`,
   clique em **Authorize** e cole o token.
2. **Instrutor** – `POST /instrutores`:
   ```json
   {"nome": "Carlos Souza", "email": "carlos@autoescola.com", "telefone": "11999990000",
    "cnh": "12345678901", "especialidade": "CARROS",
    "endereco": {"logradouro": "Praça da Sé", "numero": "100", "complemento": "lado ímpar",
                 "bairro": "Sé", "cidade": "São Paulo", "uf": "SP", "cep": "01001-000"}}
   ```
3. **Aluno** – `POST /alunos`:
   ```json
   {"nome": "Ana Lima", "email": "ana@email.com", "telefone": "11988887777", "cpf": "12345678901",
    "endereco": {"logradouro": "Av. Paulista", "numero": "1000", "complemento": null,
                 "bairro": "Bela Vista", "cidade": "São Paulo", "uf": "SP", "cep": "01310-100"}}
   ```
4. **API externa** – `GET /enderecos/cep/01001000` → retorna o endereço consultado no ViaCEP.
5. **Agendamento** – `POST /instrucoes` (data futura, segunda a sábado, hora inteira entre 06h e 20h,
   formato `dd/MM/yyyy - HH:mm`):
   ```json
   {"id_aluno": 1, "especialidade": "CARROS", "data_hora": "20/10/2026 - 10:00"}
   ```
   Repetir para o mesmo aluno no mesmo dia → `400` (regra de 1 instrução por dia).
6. **Cancelamento** – `DELETE /instrucoes/1?motivo=ALUNO_DESISTIU` → `204`.
7. **Usuários** – `POST /usuarios` com `{"login": "atendente", "senha": "1234", "perfil": "USER"}`;
   logando com esse usuário, `POST /instrutores` deve retornar `403` (somente ADMIN).
8. **Conferir no MySQL** – `select * from flyway_schema_history;` (10 migrations com `success = 1`) e
   `select * from instrucoes;` (motivo_cancelamento preenchido após o passo 6).
9. **CORS** – testado automaticamente em `CorsSwaggerTest` (origem `http://localhost:3000` permitida,
   origem desconhecida bloqueada).

## O que o Checkpoint 5 entrega

| Requisito | Onde está |
|-----------|-----------|
| Consumo de API/WebService externo | `adapter/out/client/viacep/ViaCepClient` consome o **ViaCEP**; exposto em `GET /enderecos/cep/{cep}` |
| Documentação automática (Swagger) | `config/documentation/SwaggerConfig` + anotações `@Tag`/`@Operation` nos controllers. UI: `http://localhost:8080/swagger-ui.html` · JSON/YAML: `/v3/api-docs` e `/v3/api-docs.yaml` |
| CORS | `config/cors/CorsConfig` (origens em `api.cors.allowed-origins`) integrado ao Spring Security |
| Testes automatizados por entidade | `src/test/java` – ver abaixo |

### Endpoints principais

| Recurso | Endpoints |
|---------|-----------|
| Autenticação | `POST /login` |
| Instrutores | `POST/GET/PUT /instrutores`, `GET/DELETE /instrutores/{id}` |
| Alunos | `POST/GET/PUT /alunos`, `GET/DELETE /alunos/{id}` |
| Usuários (ADMIN) | `POST/GET/PUT /usuarios`, `GET/DELETE /usuarios/{id}` |
| Instruções | `POST /instrucoes` (agendar), `GET /instrucoes`, `GET /instrucoes/{id}`, `DELETE /instrucoes/{id}?motivo=...` (cancelar) |
| API externa | `GET /enderecos/cep/{cep}` |

Para usar os endpoints protegidos no Swagger: faça `POST /login`, copie o `tokenJWT` e clique em **Authorize**.

### Regras de negócio do agendamento

Horas inteiras · funcionamento de segunda a sábado, das 06h às 20h · antecedência mínima de 30 min ·
aluno e instrutor ativos · instrutor sem outra instrução no horário · no máximo 1 instrução por dia por aluno ·
instrutor escolhido aleatoriamente (por especialidade) quando não informado.
**Cancelamento:** exige motivo (`ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`) e antecedência mínima de 24 h.

## Testes automatizados

Rodam com H2 em memória (perfil `test`) – não precisam do MySQL nem de internet: `./mvnw test`

| Entidade | Domínio | Repositório | Controller |
|----------|---------|-------------|------------|
| Instrutor | `InstrutorTest` | `InstrutorRepositoryTest` | `InstrutorControllerTest` |
| Aluno | `AlunoTest` | `AlunoRepositoryTest` | `AlunoControllerTest` |
| Usuário | `UsuarioTest` | `UsuarioRepositoryTest` | `UsuarioControllerTest`, `LoginControllerTest` (JWT) |
| Instrução | `InstrucaoTest` | `InstrucaoRepositoryTest` | `InstrucaoControllerTest`, `AgendaDeInstrucoesTest`, `ValidadoresAgendamentoTest` |

Também: `ViaCepClientTest` (API externa simulada), `EnderecoControllerTest`, `CorsSwaggerTest` (CORS e Swagger).

## Atualizar o arquivo `SwaggerOffLine.yaml`

Com a API rodando: `curl http://localhost:8080/v3/api-docs.yaml -o SwaggerOffLine.yaml`
