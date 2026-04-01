# Locadora Livros

Sistema completo para gerenciamento de biblioteca e locação de livros, com autenticação JWT, controle de usuários e um dashboard operacional com indicadores por período.

Projeto desenvolvido com foco em arquitetura full stack, experiência de uso e organização de código para cenários reais de negócio.

## Sumário

1. [Visão Geral](#visão-geral)
2. [Principais Funcionalidades](#principais-funcionalidades)
3. [Requisitos do Desafio (Matriz de Atendimento)](#requisitos-do-desafio-matriz-de-atendimento)
4. [Stack Tecnológica](#stack-tecnológica)
5. [Arquitetura do Projeto](#arquitetura-do-projeto)
6. [Dashboard e Indicadores](#dashboard-e-indicadores)
7. [Execução com Docker](#execução-com-docker)
8. [Execução Local (Sem Docker)](#execução-local-sem-docker)
9. [Documentação da API](#documentação-da-api)
10. [Testes](#testes)
11. [Melhorias da V1](#melhorias-da-v1)
12. [Roadmap](#roadmap)

## Visão Geral

O Locadora Livros centraliza a operação de uma biblioteca locadora em um único sistema:

- Gestão de editoras, livros, locatários e usuários.
- Controle completo do ciclo de aluguel (cadastro, atualização, devolução e status).
- Autenticação e autorização com JWT.
- Recuperação de senha por e-mail.
- Dashboard analítico com filtros de período e rankings.

## Principais Funcionalidades

### Módulos de domínio

- Editoras: cadastro, edição, exclusão lógica, busca e paginação.
- Livros: cadastro, edição, exclusão lógica, busca, paginação e vínculo com editora.
- Locatários: cadastro, edição, exclusão lógica, busca e paginação.
- Aluguéis: criação, edição, devolução, filtros por status, busca e paginação.
- Usuários: gerenciamento de contas e papéis.

### Segurança e autenticação

- Login com JWT (`/auth/login`).
- Persistência de sessão no frontend.
- Rotas protegidas no fluxo principal.
- Recuperação de senha com envio de e-mail.

### Experiência de uso

- Interface responsiva construída com Quasar.
- Feedback visual com notificações.
- Modais de confirmação e formulários padronizados.
- Dashboard com animações e layout harmonizado com o restante do sistema.

## Requisitos do Desafio (Matriz de Atendimento)

Esta seção documenta o atendimento dos requisitos obrigatórios definidos no desafio.

| Requisito | Status | Observação |
|---|---|---|
| Cadastro de Livro (CRUD, busca, estoque, vínculo com editora) | Atendido | Inclui validações de data de lançamento e estoque. |
| Regra de edição de estoque (>= quantidade alugada) | Atendido | Regras de integridade de quantidade são aplicadas na camada de validação/serviço. |
| Regra de exclusão de livro sem exemplar alugado | Atendido | Exclusão bloqueada quando há locação ativa do livro. |
| Cadastro de Locatário (CRUD, busca por nome, CPF opcional) | Atendido | CPF é opcional e validado quando informado. |
| Regra de exclusão de locatário com empréstimo ativo | Atendido com regra mais restritiva | Atualmente bloqueia exclusão se houver histórico de aluguel; supera a regra mínima de empréstimo ativo. |
| Cadastro de Editora (CRUD + busca) | Atendido | Com validações de nome, e-mail, telefone e site. |
| Cadastro de Aluguel (criar, listar, pesquisar, finalizar) | Atendido | Fluxo completo implementado com devolução e status. |
| Pesquisa de aluguel por nome de livro, editora, autor, status e usuário | Atendido (foco em livro, usuário, datas e status) | Cobertura principal implementada; filtros podem ser expandidos na V2 para cenários avançados. |
| Tela de Login (nome, e-mail, senha, autenticação) | Atendido | Autenticação via JWT e persistência de sessão. |
| Cadastro de usuários e níveis de acesso (editor e leitor) | Atendido (equivalência) | Papéis atuais: `ADMIN` (editor) e `USER` (leitor), com restrições no frontend e backend. |
| Relatórios e Dashboard obrigatórios | Atendido | Quantidades de alugados, atrasados, devoluções, ranking de livros e locatários por período. |
| Regra: usuário não pode pegar o mesmo livro com empréstimo ativo | Atendido | Regra de aluguel repetido ativo validada. |
| Regra: movimentação de aluguel/devolução atualiza estoque | Atendido | Estoque é atualizado na criação e devolução de aluguel. |
| Regra: prazo máximo de devolução em 30 dias corridos | Atendido | Validação aplicada no cadastro/edição de aluguel. |

### Requisitos funcionais detalhados

1. Cadastro de Livro
- Obrigatórios: nome, autor, editora, data de lançamento, estoque.
- Operações: criar, listar, pesquisar, editar e deletar com regras de consistência.
- Controle de estoque: total, em uso e disponível.

2. Cadastro de Locatário
- Obrigatórios: nome, e-mail, celular, endereço.
- Facultativo: CPF.
- Operações: criar, listar, pesquisar e editar.

3. Cadastro de Editoras
- Obrigatórios: nome, e-mail, telefone.
- Facultativo: site.
- Operações: criar, editar, deletar, listar e pesquisar.

4. Cadastro de Aluguel
- Obrigatórios: usuário e data de devolução.
- Operações: criar, listar, pesquisar e finalizar.

5. Login e Autenticação
- Cadastro e autenticação de usuário com token JWT.

6. Cadastro de Usuários e Acessos
- Campos: nome, e-mail, senha e nível de permissão.
- Permissões equivalentes:
	- Editor -> `ADMIN`
	- Leitor -> `USER`

7. Relatórios e Dashboard
- Quantidade de livros emprestados.
- Quantidade de livros atrasados.
- Quantidade de livros emprestados por usuário.
- Quantidade de livros devolvidos dentro e fora do prazo.
- Quantidade de aluguéis por usuário.
- Livro mais alugado.

## Stack Tecnológica

### Backend

- Java 21
- Spring Boot 3.3.2
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- Java JWT (`com.auth0:java-jwt`)
- Springdoc OpenAPI (Swagger)

### Frontend

- Vue 3
- Quasar Framework (Vite)
- Vue Router
- Axios
- Chart.js
- SCSS

### Infra e DevOps

- Docker
- Docker Compose
- Nginx (serving do frontend)

## Arquitetura do Projeto

```text
gerenciamento-livros/
├── backend/          # API REST (Spring Boot)
├── frontend/         # SPA (Vue 3 + Quasar)
├── test/             # Testes E2E (Robot Framework)
└── docker-compose.yml
```

### Padrão de organização (backend)

Cada contexto de domínio segue estrutura de camadas:

- `controllers`
- `services`
- `repositories`
- `models`
- `DTOs`
- `validation`
- `mappers`

Essa abordagem facilita manutenção, evolução e testes.

## Dashboard e Indicadores

A dashboard foi desenhada para apoiar tomada de decisão operacional.

### Indicadores exibidos

- Aluguéis ativos
- Aluguéis atrasados
- Aluguéis finalizados
- Livros ativos
- Locatários ativos
- Taxa de devolução no prazo

### Insights analíticos

- Status dos aluguéis (distribuição)
- Evolução mensal por janela de tempo
- Top livros mais alugados
- Top locatários

### Janela temporal

Filtro disponível para:

- 1 mês
- 3 meses
- 6 meses
- 12 meses

## Execução com Docker

### 1) Pré-requisitos

- Docker
- Docker Compose

### 2) Configure o arquivo `.env` na raiz

Use as variáveis listadas na seção [Variáveis de Ambiente](#variáveis-de-ambiente).

### 3) Suba os serviços

```bash
docker compose up --build
```

### 4) Acesse

- Frontend: `http://localhost:${FRONT_PORT}`
- Backend: `http://localhost:${BACK_PORT}`

## Execução Local (Sem Docker)

## Backend

```bash
cd backend
./mvnw spring-boot:run
```

ou

```bash
cd backend
sh ./mvnw spring-boot:run
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```


## Documentação da API

Com o backend em execução, a documentação OpenAPI fica disponível em:

- `http://localhost:8040/swagger-ui/index.html`

Principais grupos de endpoints:

- Autenticação (`/auth/**`)
- Editoras (`/publisher/**`)
- Livros (`/book/**`)
- Locatários (`/renter/**`)
- Aluguéis (`/rent/**`)
- Usuários (`/user/**`)
- Dashboard (`/dashboard/**`)

## Testes

O projeto possui cenários automatizados em Robot Framework no diretório `test/`:

- `aluguel.robot`
- `editora.robot`
- `livro.robot`
- `locatario.robot`
- `usuario.robot`

Esses testes cobrem fluxos críticos de interface e regras de uso.

## Melhorias da V1

Evoluções entregues nesta versão:

- Contadores de cards no frontend alimentados por endpoints de backend.
- Novos endpoints de contagem para editoras, livros, locatários e aluguéis.
- Dashboard reestruturada com endpoint agregado e lógica consistente por período.
- Correções de inconsistência em rankings e métricas temporais.
- Melhorias de UX: animações de entrada e alinhamento visual dos blocos.
- Branding no drawer com logo e identidade do projeto.

## Roadmap

Próximos passos sugeridos para V2:

- Testes unitários e de integração no backend.
- Pipeline CI/CD (lint, build, testes e deploy).
- Observabilidade (logs estruturados + métricas).
- Alertas operacionais no dashboard (atrasos críticos e devoluções próximas).
- Hardening de segurança (refresh token, rate limiting, auditoria).


