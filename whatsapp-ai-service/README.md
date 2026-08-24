# WhatsApp AI Automation Service

Middleware between the **Meta WhatsApp Cloud API** and an **LLM** (OpenAI `gpt-4o-mini`).
It receives WhatsApp webhooks, keeps conversation memory in PostgreSQL, generates a reply with
the LLM, extracts structured leads and sends the sanitized answer back to the customer.

## Stack

- Node.js 20 + TypeScript + Express
- PostgreSQL (raw SQL via `pg`, plain SQL migrations)
- OpenAI Chat Completions (`gpt-4o-mini`)
- Meta Graph API v19.0
- Docker Compose for local database + app

## Project layout

```
src/
  app.ts                    express app factory
  server.ts                 entrypoint (runs migrations, then listens)
  config/                   env config, business context, system prompt
  controllers/              webhook controller (verification + ingestion)
  routes/                   express routers
  services/                 payload parsing, LLM, lead parsing, WhatsApp dispatch, orchestration
  db/                       pool, migrations runner, repositories (sessions/messages/leads)
db/migrations/              SQL schema
scripts/                    mock webhook runner + sample Meta payloads
tests/                      unit tests (vitest)
```

## Data model

| table      | columns |
| ---------- | ------- |
| `sessions` | `id`, `phone_number` (unique), `created_at`, `updated_at` |
| `messages` | `id`, `session_id` → sessions, `role` (`user`/`assistant`/`system`), `content`, `timestamp` |
| `leads`    | `id`, `phone_number`, `name`, `service_interest`, `metadata` (JSONB), `created_at` |

## Endpoints

| method | path       | description |
| ------ | ---------- | ----------- |
| `GET`  | `/webhook` | Meta verification: checks `hub.mode=subscribe` and `hub.verify_token` against `WEBHOOK_VERIFY_TOKEN`, echoes `hub.challenge` with 200 (403 otherwise). |
| `POST` | `/webhook` | Answers `200 OK` immediately, then processes the message asynchronously. Status updates (delivered/read) and non-text messages are ignored. |
| `GET`  | `/health`  | Liveness + database check. |

## Message flow

1. Parse `entry[0].changes[0].value.messages[0]`, extract `from` and `text.body`.
2. `getOrCreateSession(phone)`; load the last `CONTEXT_MESSAGE_LIMIT` (default 6) messages of the session.
3. Persist the user message, then call the LLM with system prompt + history.
4. If the answer contains `[LEAD_DATA: {...}]`, the JSON is stored in `leads` and the tag is stripped from the customer reply.
5. Send the clean text via `POST https://graph.facebook.com/v19.0/{PHONE_NUMBER_ID}/messages` and persist the assistant message.

## Setup (local, without Docker)

```bash
cp .env.example .env      # fill in your credentials
npm install
npm run migrate           # applies db/migrations/*.sql
npm run dev               # http://localhost:3000
```

## Setup with Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

`db` exposes PostgreSQL on `localhost:5432` (`whatsapp` / `whatsapp` / `whatsapp_ai`) and `app` runs
on `localhost:3000`. Migrations run automatically at boot.

To run only the database and keep the app in watch mode locally:

```bash
docker compose up -d db
npm run dev
```

## Environment variables

| variable | description |
| -------- | ----------- |
| `PORT` | HTTP port (default `3000`) |
| `DATABASE_URL` | PostgreSQL connection string |
| `META_ACCESS_TOKEN` | Meta permanent access token |
| `META_PHONE_NUMBER_ID` | WhatsApp Cloud API phone number id |
| `WEBHOOK_VERIFY_TOKEN` | Token configured in the Meta webhook setup |
| `OPENAI_API_KEY` | OpenAI API key |
| `OPENAI_MODEL` | Defaults to `gpt-4o-mini` |
| `GRAPH_API_VERSION` | Defaults to `v19.0` |
| `MOCK_MODE` | `true` skips real OpenAI/Meta calls and uses a canned reply |
| `CONTEXT_MESSAGE_LIMIT` | Number of previous messages sent to the LLM (default `6`) |

## Local testing without Meta credentials

Unit tests (payload parsing and lead extraction, no database required):

```bash
npm test
```

End-to-end simulation against the running service, no live Meta/OpenAI credentials needed:

```bash
docker compose up -d db
MOCK_MODE=true npm run dev            # in one terminal
MOCK_MODE=true npm run mock:webhook   # in another terminal
```

The runner verifies `GET /webhook` (challenge echo) and posts every payload in `scripts/payloads/`:
a text question, an appointment confirmation (triggers a lead), a delivery status update and an
image message (both must be ignored). Send a single payload with
`npm run mock:webhook -- appointmentMessage`.

Check the persisted results:

```bash
docker compose exec db psql -U whatsapp -d whatsapp_ai -c 'SELECT role, content FROM messages ORDER BY id;'
docker compose exec db psql -U whatsapp -d whatsapp_ai -c 'SELECT * FROM leads;'
```

## Connecting the real Meta webhook

1. Expose the service publicly (e.g. `ngrok http 3000` or a deployed host).
2. In Meta → WhatsApp → Configuration, set the callback URL to `https://<host>/webhook` and the
   verify token to the value of `WEBHOOK_VERIFY_TOKEN`, then click *Verify and save*.
3. Subscribe to the `messages` webhook field.
4. Make sure `META_ACCESS_TOKEN` and `META_PHONE_NUMBER_ID` are set and `MOCK_MODE=false`.

## Deployment notes

- The image built by the `Dockerfile` runs `node dist/src/server.js` and applies pending migrations at startup.
- Provide the environment variables through your platform's secret manager; never commit `.env`.
- The business knowledge used by the assistant lives in `src/config/businessContext.ts` — edit it to
  match your own business before going live.

## Quality checks

```bash
npm run lint
npm run typecheck
npm test
```
