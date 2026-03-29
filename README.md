# Project Setup

## Prerequisites

- MySQL 8.x
- Java 25
- Node.js with `pnpm@10.30.3`

## 1. Create the database

Run [database.sql](database.sql) once before starting the backend.

`schema.sql` is no longer auto-executed at startup. The deployment path is:

1. Run `database.sql`
2. Fill in the required parameters
3. Start backend and frontend

## 2. Configure the backend

Edit [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) before startup.

Required database settings:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Required Aliyun PNVS SMS settings:

- `app.sms.aliyun.access-key-id`
- `app.sms.aliyun.access-key-secret`
- `app.sms.aliyun.sign-name`
- `app.sms.aliyun.template-code`

Notes:

- `sign-name` and `template-code` must exactly match the PNVS sign and template in the Aliyun console.
- PNVS SMS verification only works for phones allowed by the current PNVS configuration. In test mode, this usually means bound test phone numbers only.
- Uploaded snack images are stored under `backend/uploads/snacks` and the directory is created automatically.

## 3. Start the backend

```powershell
cd backend
.\mvnw.cmd -q spring-boot:run
```

## 4. Start the frontend

```powershell
cd frontend
pnpm install
pnpm dev
```

## 5. Frontend API address

The frontend default backend address is [frontend/nuxt.config.ts](frontend/nuxt.config.ts):

```ts
runtimeConfig: {
  public: {
    backendBaseUrl: "http://127.0.0.1:8080"
  }
}
```

If the backend runs on another host or port, update `backendBaseUrl` before starting the frontend.
