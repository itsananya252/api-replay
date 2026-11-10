# API Replay

![CI](https://github.com/itsananya252/api-replay/actions/workflows/ci.yml/badge.svg)

Record real API traffic, replay it against a new version of your service, and see exactly what changed, with breaking changes flagged.

![Report view](docs/screenshot.png)

## Why

A renamed field or a removed property is easy to miss in code review and hard to catch with hand-written tests. API Replay uses real requests as the test suite: record once, replay after every change, and read the diff.

## How it works

1. **Record**: a servlet filter captures every `/api/**` request and response into a database. Authorization and cookie headers are masked.
2. **Replay**: saved GET calls are re-sent to a target server. Replay traffic is tagged so it is never recorded again.
3. **Diff**: responses are compared field by field.

| Change | Type | Breaking |
|---|---|---|
| Field removed | `REMOVED` | Yes |
| Field type changed | `TYPE_CHANGED` | Yes |
| Status code changed | `STATUS_CHANGED` | Yes |
| Field added | `ADDED` | No |
| Value changed | `VALUE_CHANGED` | No |

4. **Ignore rules**: fields such as timestamps can be excluded from value comparison with `replay.ignore-fields`. Removing such a field is still reported as breaking.

## Tech stack

- Backend: Java 21, Spring Boot 4, Spring Data JPA, H2
- Frontend: React (Vite)
- CI: GitHub Actions (unit tests for the diff engine, frontend build)

## Run locally

Backend:

    cd backend
    ./mvnw spring-boot:run

Frontend:

    cd frontend
    npm install
    npm run dev

Open http://localhost:5173

## Try it

1. Create a few users with `POST /api/users`, then call `GET /api/users` and `GET /api/users/1`.
2. Open the UI and press **Run replay**. Everything should match.
3. Rename a field in `UserResponse`, restart the backend, and press **Run replay** again. The removed field is flagged as breaking.

## API

| Endpoint | Purpose |
|---|---|
| `GET /api/recorded` | List recorded calls |
| `DELETE /api/recorded` | Clear recordings |
| `POST /api/replay?target=URL` | Replay recorded GET calls against a server |
| `GET /api/replay` | List replay runs |
| `GET /api/replay/{runId}/report` | Field-level diff report |
| `POST /api/replay/ci?target=URL` | Replay and return HTTP 409 if any change is breaking |