# QuizForge AI — AI-Powered Knowledge Quiz Builder

A full-stack, production-grade quiz generation platform with an embedded "trained" AI model that works **entirely offline** behind corporate firewalls.

## Architecture

```
┌──────────────────┐         ┌──────────────────────────────┐
│  React 18 + TS   │ ──HTTP──▶  Spring Boot 3.2 + Java 21   │
│  Vite + Tailwind │         │  H2 In-Memory · JPA · Cache  │
└──────────────────┘         └──────────────┬───────────────┘
                                            │
                                ┌───────────▼────────────┐
                                │  AI Orchestrator (with │
                                │   primary + fallback)  │
                                ├────────────────────────┤
                                │  • OpenAI (optional)   │
                                │  • LocalKnowledgeModel │
                                │     - 16 curated topics│
                                │     - Fuzzy matching   │
                                │     - Template fallback│
                                └────────────────────────┘
```

## Features

- **Offline-first AI**: Curated knowledge base + template generator runs without any external API
- **Pluggable AI providers**: Swap in OpenAI by setting `OPENAI_API_KEY` and `ai.provider=openai`
- **Strategy + fallback chain**: Primary provider fails → automatic fallback to local model
- **Scalable**: Caching (Spring Cache), pagination, async support, indexed JPA repositories
- **Production-grade**: OpenAPI/Swagger, Actuator metrics, validation, exception handling
- **Modern UI**: Animated React frontend with TanStack Query, Framer Motion, Tailwind
- **Live leaderboard**, quiz history, detailed result breakdowns with explanations

## Prerequisites

- **Java 21** (required — Lombok 1.18.34 doesn't support Java 25)
- **Node.js 18+** and npm

Install Java 21 via Homebrew if needed:
```bash
brew install openjdk@21
```

## Running

### Backend (port 8080)

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
./mvnw spring-boot:run        # or: mvn spring-boot:run
```

URLs:
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (jdbc URL: `jdbc:h2:mem:quizdb`, user `sa`, blank password)
- Actuator: http://localhost:8080/actuator/health

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 — the Vite dev server proxies `/api` → backend.

### Production frontend build

```bash
cd frontend
npm run build
npm run preview
```

## Running Tests

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
mvn test
```

## Enabling OpenAI (optional)

By default the app uses the offline local model. To enable OpenAI:

```bash
export OPENAI_API_KEY=sk-...
# In backend/src/main/resources/application.yml set:
#   ai:
#     provider: openai
```

The orchestrator will try OpenAI first and fall back to the local model on any error.

## Project Structure

```
backend/
  src/main/java/com/quizbuilder/
    QuizBuilderApplication.java
    config/         # OpenAPI, CORS, async config
    controller/     # REST endpoints
    dto/            # Request/response DTOs + ApiResponse wrapper
    exception/      # Global exception handler
    model/          # JPA entities
    repository/     # Spring Data JPA repositories
    service/        # Business logic
    ai/             # AI strategy, orchestrator, local model, OpenAI client
  src/main/resources/
    application.yml
    knowledge/      # JSON knowledge base (science, technology, history, general)
  src/test/java/    # Unit tests

frontend/
  src/
    api/            # Axios client + typed namespaces
    components/     # Layout + reusable UI primitives
    pages/          # Routed views (Home, Generate, Quiz, Result, History, Leaderboard, 404)
    types/          # Shared TypeScript types matching backend DTOs
    main.tsx        # React entry with QueryClient + Router + Toaster
    index.css       # Tailwind layers + component classes
```

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/quizzes/generate` | Generate a new quiz via AI |
| GET | `/api/v1/quizzes/{id}` | Fetch quiz by ID (options stripped of `isCorrect`) |
| GET | `/api/v1/quizzes` | Paginated list |
| GET | `/api/v1/quizzes/recent?limit=5` | Recent quizzes |
| DELETE | `/api/v1/quizzes/{id}` | Delete quiz |
| POST | `/api/v1/attempts/submit` | Submit answers, get graded result |
| GET | `/api/v1/attempts/{id}` | Full attempt breakdown with explanations |
| GET | `/api/v1/attempts/leaderboard` | Top performers |
| GET | `/api/v1/stats` | System stats |
| GET | `/api/v1/stats/topics` | Curated topics list |
| GET | `/api/v1/stats/categories` | Categories list |

## Scaling Notes

- **Caching**: `@Cacheable` on quiz fetches and stats; cache evicted on writes
- **Pagination**: All list endpoints are paginated to support large datasets
- **Indices**: Primary keys (UUIDs), composite ordering on questions/options for deterministic reads
- **Async**: `@EnableAsync` enabled for future background processing
- **DB**: Swap H2 for Postgres/MySQL — change `spring.datasource.*` only
- **AI scaling**: Local model is in-memory and stateless; horizontally scales with replicas
