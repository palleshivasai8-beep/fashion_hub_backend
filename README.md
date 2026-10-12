# Fashion Hub Backend

Spring Boot REST API for the Fashion Hub React storefront. The existing product, user, and order entities and `/api` routes are retained.

## Local development

Requires Java 17 and a reachable MySQL or PostgreSQL database. For local MySQL, set the database variables in the current PowerShell session (use your own local credentials; do not commit them):

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/fashionhub"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "<your-local-database-password>"
.\mvnw.cmd spring-boot:run
```

The default local port is `8081`; `PORT` overrides it. To package the application:

```powershell
.\mvnw.cmd clean package
```

The Maven wrapper can also be run as `./mvnw spring-boot:run` and `./mvnw clean package` on Linux/macOS.

## Render deployment

Use a **Web Service** connected to `palleshivasai8-beep/fashion_hub_backend`, branch `main`, with the repository root as the service root and the checked-in `Dockerfile` as the runtime configuration. Render's Docker runtime uses these commands from that Dockerfile:

- Build: `./mvnw clean package -DskipTests -B`
- Start: `java -Djava.security.egd=file:/dev/./urandom -jar app.jar`

Create a Render PostgreSQL database and set these Web Service environment variables:

| Variable | Value |
| --- | --- |
| `DATABASE_URL` | Render PostgreSQL **internal** connection URL |
| `CORS_ALLOWED_ORIGINS` | `https://<your-vercel-project>.vercel.app` (comma-separate additional exact origins if needed) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` for initial schema creation/compatibility |
| `SPRING_JPA_SHOW_SQL` | `false` |

Render supplies `PORT`; the application binds to it. The backend also accepts explicit `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` variables instead of `DATABASE_URL`. Do not set `SPRING_DATASOURCE_URL` when relying on `DATABASE_URL`, since the explicit JDBC URL takes precedence. Never put a database password in source files.

Health checks are available at `/health` and `/api/health`.

## Database compatibility and migration

The backend keeps the existing MySQL driver and entity model and also includes the PostgreSQL driver. PostgreSQL is suitable for a new Render deployment, but MySQL and PostgreSQL are separate databases: Hibernate's `ddl-auto=update` can create/update tables, but it does **not** copy existing records between them.

The current model maps to the `app_users` (`id`, `name`, `email`, `password`), `product` (`id`, `name`, `category`, `price`, `image`), and `shop_orders` (`id`, `user_id`, `customer_name`, `address`, `product_names`, `quantity`, `total_price`, `status`) tables. If the MySQL database contains data that must be retained:

1. Take and verify a restorable MySQL backup before changing deployment configuration.
2. Create a separate PostgreSQL database and migrate the schema and rows with a MySQL-to-PostgreSQL migration tool such as `pgloader`; supply source/target connection strings from a secure local secret store, not the repository.
3. Compare row counts and key records for all three tables. Smoke-test product listing, login, registration, order placement, and order status against the migrated database.
4. Point Render at the migrated PostgreSQL database only after validation. Keep the original MySQL database and backup unchanged until the new deployment is confirmed.

For a new, empty Render database, `ddl-auto=update` creates the tables and the application seeds the catalogue only when the product table is empty.

## REST API

- `GET /api/products`
- `POST /api/auth/register` and `POST /api/auth/login`
- `POST /api/orders`, `GET /api/orders?userId=<id>`, and `PUT /api/orders/{id}/deliver`
