# Todo API — Design

Date: 2026-10-04

## Goal

A small RESTful API built with Jakarta EE for learning purposes. It manages Todo
tasks with full CRUD. Success = the app starts with one command, every endpoint
below returns the documented status codes, and the unit tests pass.

## Constraints and decisions

- Jakarta EE 10 Web Profile API (`jakarta.jakartaee-web-api:10.0.0`, `provided` scope).
- Runtime: Open Liberty, started with `liberty-maven-plugin` (`./mvnw liberty:dev`).
  Liberty features: `restfulWS-3.1`, `jsonb-3.0`, `cdi-4.0`.
- Build: Maven via the Maven Wrapper (Maven is not installed globally). `war` packaging.
- Java release level 21 (compiled with the installed JDK 25).
- Storage: in memory only. Data is lost on restart. This is intentional.
- No authentication, no database, no persistence layer.
- HTTP port 9080, context root `/`, application path `/api`.

## Components

Package `com.example.todo`:

| File | Responsibility |
|---|---|
| `RestApplication` | `@ApplicationPath("/api")`, extends `jakarta.ws.rs.core.Application`. Nothing else. |
| `Task` | POJO with `Long id`, `String title`, `boolean done`. Public no-arg constructor plus getters/setters (JSON-B needs them). |
| `TaskRepository` | `@ApplicationScoped`. `ConcurrentHashMap<Long, Task>` + `AtomicLong` id generator. Methods: `findAll()`, `findById(id) -> Optional<Task>`, `create(task) -> Task`, `update(id, task) -> Optional<Task>`, `delete(id) -> boolean`. |
| `TaskResource` | `@Path("/tasks")`, `@Produces/@Consumes(APPLICATION_JSON)`. Injects `TaskRepository`. Maps results to HTTP responses. |

Config: `src/main/liberty/config/server.xml` declares the features and the port.

## Endpoints

Base URL: `http://localhost:9080/api`

| Method | Path | Body | Success | Errors |
|---|---|---|---|---|
| GET | `/tasks` | — | 200, JSON array | — |
| GET | `/tasks/{id}` | — | 200, task | 404 if not found |
| POST | `/tasks` | `{"title": "...", "done": false}` | 201, created task, `Location: /api/tasks/{id}` | 400 if body is missing or title is null/blank |
| PUT | `/tasks/{id}` | `{"title": "...", "done": true}` | 200, updated task | 400 if title blank; 404 if not found |
| DELETE | `/tasks/{id}` | — | 204 | 404 if not found |

Rules:
- The server always assigns `id`. Any `id` sent in a POST/PUT body is ignored.
- `findAll()` returns tasks sorted by id ascending, so the output order is stable.
- Error responses carry a JSON body `{"error": "<message>"}`.

## Data flow

JSON request → JSON-B deserializes it into `Task` → `TaskResource` validates the
title and calls `TaskRepository` → the result goes into a `Response` with the
correct status → JSON-B serializes it back to JSON.

## Testing

- JUnit 5 unit tests for `TaskRepository`: create assigns increasing ids; find
  by an existing or missing id; update of an existing or missing id; delete of
  an existing or missing id; findAll ordering.
- Manual smoke test with curl against `liberty:dev`, covering each endpoint and
  each error code in the table above.

## Out of scope

Database/JPA, Bean Validation, authentication, pagination, OpenAPI docs, Docker image.
