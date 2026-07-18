# FestWear

Processo Seletivo LAPES 2026 — Trilha de Desenvolvimento (Mini E-commerce).

**Candidato:** Paulo Ricardo Silva Fonseca
**Contato:** paulorsf29@gmail.com · WhatsApp (91) 98449-4995

---

E-commerce de camisas de time: API REST (Spring Boot) + interface web (React), com os domínios de
autenticação, catálogo, carrinho, checkout/pedidos e cupons de desconto, além dos requisitos
não-funcionais do desafio (migrations, controle de concorrência, cache, rate limiting, logs
estruturados, CI e testes automatizados).

## Stack

**Backend** — `Java 17`, `Spring Boot 4.1`, `Spring Data JPA` (Hibernate), `Spring Security` + JWT,
`PostgreSQL`, `Flyway` (migrations), `Redis` (cache do catálogo), `springdoc-openapi` (Swagger UI),
JUnit 5 + Mockito.

**Frontend** — `React 19`, `Vite`, `React Router 7`, `Tailwind CSS 4`, `axios`.

**Infra** — `Docker Compose` (Redis), `GitHub Actions` (CI).

## Estrutura do repositório

```
Ecomerce/
├── src/                    # backend (Spring Boot)
├── frontend/               # frontend (React + Vite)
├── docker-compose.yml      # Redis
├── .github/workflows/      # CI
└── pom.xml
```

## Pré-requisitos

- Java 17
- Node.js 22+
- PostgreSQL rodando localmente, com um banco `Ecomerce_DB` já criado
- Docker Desktop (para o Redis do cache)

## Setup

### 1. Banco de dados

Crie um banco Postgres vazio chamado `Ecomerce_DB` (via pgAdmin, `psql` ou similar). O schema é
criado automaticamente pelas migrations do Flyway na primeira execução — não é necessário rodar
nenhum script manualmente.

Se seu usuário/senha do Postgres não forem `postgres`/`root`, exporte antes de rodar o backend:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/Ecomerce_DB
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha
```

### 2. Redis (cache)

```bash
docker compose up -d redis
```

### 3. Backend

```bash
./mvnw spring-boot:run
```

Sobe em `http://localhost:8082`. Na primeira execução, o `AdminSeeder` cria um usuário admin
(`admin@ecomerce.com` / `admin123` por padrão — troque via `ADMIN_EMAIL`/`ADMIN_PASSWORD` em
produção) e o `DataSeeder` popula o catálogo com produtos de exemplo e dois cupons
(`BEMVINDO10`, `FRETE20`).

Documentação interativa da API: `http://localhost:8082/swagger-ui.html`.

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173`, já apontando para a API local (`frontend/.env`).

### 5. Testes

```bash
./mvnw test
```

55 testes (unitários, com Mockito) cobrindo autenticação, catálogo, carrinho, cupons, checkout e
pagamento — incluindo o teste de contexto completo do Spring (`EcomerceApplicationTests`), que
também aciona as migrations do Flyway.

## Domínios e regras de negócio

- **Autenticação** — registro/login com JWT, papéis `ADMIN`/`CUSTOMER`, rotas protegidas por papel.
- **Catálogo** — CRUD completo, busca por nome/time/marca, filtro por categoria e faixa de preço,
  paginação, cache Redis (invalidado em qualquer escrita).
- **Carrinho** — persistido no backend (`carts`/`cart_items`), um carrinho por usuário autenticado;
  estoque validado ao adicionar e novamente no checkout.
- **Checkout/Pedidos** — pedido é montado a partir do carrinho persistido do usuário; máquina de
  estados `PENDING_PAYMENT → PAID → SHIPPED → DELIVERED`, com `CANCELED` alcançável apenas antes de
  `SHIPPED` (e com devolução de estoque). Pagamento é simulado com ~80% de aprovação; uma rejeição
  não consome o pedido — o cliente pode tentar novamente.
- **Cupons** — percentual ou valor fixo, com expiração, valor mínimo de pedido e uso único por
  usuário, validados no checkout e com endpoint de pré-visualização do desconto.

## Decisões técnicas

**Controle de concorrência no estoque** — a reserva de estoque no checkout usa lock pessimista
(`SELECT ... FOR UPDATE`, via `@Lock(PESSIMISTIC_WRITE)` em `ProductRepository`), segurando a linha
do produto até o commit da transação. Isso garante que duas requisições simultâneas para o último
item em estoque não possam ambas ter sucesso — a segunda espera a primeira e então vê o estoque já
decrementado.

**Migrations (Flyway) em vez de `ddl-auto=update`** — o schema é versionado em
`src/main/resources/db/migration`, reproduzível do zero em qualquer ambiente. *Nota de ambiente*:
neste build específico do Spring Boot usado no projeto, a auto-configuração padrão do Flyway não
está disponível (módulo ausente), então as migrations são disparadas explicitamente em
`EcomerceApplication` antes do container Spring subir (`FlywayRunner`), e também no
`@BeforeAll` do teste de contexto — mantendo o comportamento correto independente disso.

**Cache do catálogo com Redis** — solução externa (não em memória), com TTL configurável e
invalidação total do cache `products` em qualquer criação/edição/remoção. Cachear `Page<T>` do
Spring Data diretamente não funciona bem com serialização JSON genérica (`PageImpl` não tem
construtor utilizável por reflexão) — por isso o cache guarda um DTO simples e equivalente
(`PageResponse<T>`) em vez do `Page` do Spring Data.

**Rate limiting** — filtro próprio (janela fixa por IP, em memória) aplicado a `POST /api/auth/**`
e `GET /api/products/**`, sem dependência externa. Suficiente para uma instância única; um
deployment multi-nó precisaria de um contador compartilhado (ex.: Redis).

**Logs estruturados** — filtro que loga uma linha JSON por request (`timestamp`, `method`, `path`,
`status`, `durationMs`), incluindo requests rejeitadas por rate limit ou autenticação.

**Simulação de pagamento testável** — a aprovação/rejeição do "gateway" fica atrás de uma interface
(`PaymentGatewaySimulator`), não de `Math.random()` direto no serviço — permitindo que os testes
controlem o resultado deterministicamente em vez de depender de sorte.

**Monorepo** — backend e frontend no mesmo repositório (`frontend/`), como pedido no desafio.

## Variáveis de ambiente (backend)

| Variável | Padrão | Descrição |
|---|---|---|
| `SERVER_PORT` | `8082` | Porta HTTP do backend |
| `DB_URL` | `jdbc:postgresql://localhost:5432/Ecomerce_DB` | URL do Postgres |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `root` | Credenciais do Postgres |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Conexão com o Redis |
| `JWT_SECRET` / `JWT_EXPIRATION_MS` | — | Configuração do token JWT |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@ecomerce.com` / `admin123` | Admin criado no primeiro boot |
