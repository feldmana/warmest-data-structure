# Warmest

A key→value store that tracks the most recently accessed key (`getWarmest`).

Built with **Java 17** and **Spring Boot**, exposed via **REST API**, and distributed across 3 instances using **Redis**.

---

## Parts

| Part | Description | Implementation |
|---|---|---|
| 1 | In-memory data structure | `WarmestInMemoryDataStructure` (HashMap + doubly linked list) |
| 2 | REST API | Spring Boot — `WarmestController` → `WarmestService` → `WarmestDataStructureInterface` |
| 3 | Distributed (3 instances) | `WarmestRedisDataStructure` — all instances share one Redis |

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

---

## Profiles

| Profile | Implementation | Use case |
|---|---|---|
| `redis` | `WarmestRedisDataStructure` | Distributed — all instances share one Redis |
| `in-memory` | `WarmestInMemoryDataStructure` | Local development and unit tests |

---

## API

| Method | Endpoint | Returns |
|---|---|---|
| `PUT` | `/api/{key}/{value}` | Previous value, empty if new key |
| `GET` | `/api/{key}` | Current value, `404` if missing |
| `DELETE` | `/api/{key}` | Removed value, `404` if missing |
| `GET` | `/api/warmest` | Most recently accessed key, `204` if empty |

---

## Run distributed (3 instances + Redis)

Uses Docker to start Redis and 3 app instances on ports `8080`, `8081`, `8082`, runs PUT / GET / DELETE across different ports to verify distributed behaviour, and shuts down.

Run: `bash demo.sh`

For more manual testing use Postman:

1. Start Docker
2. Run `docker compose up --build`
3. Use Postman to call any combination of `PUT`, `GET`, `DELETE`, `GET /warmest` across ports `8080`, `8081`, `8082`
