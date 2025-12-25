# Client Portal API

A REST API for tax filing order management built with Spring Boot and Kotlin.

## Tech Stack

- **Language:** Kotlin
- **Framework:** Spring Boot 3.3.5
- **Database:** PostgreSQL
- **Authentication:** JWT
- **Payments:** Stripe
- **Document OCR:** Nanonets
- **Migrations:** Flyway

## Prerequisites

- Java 21
- Docker & Docker Compose
- Stripe account (for payments)

## Quick Start

### 1. Start Database (Development)

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your Stripe keys
```

### 3. Run Application

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8081`

## Docker Deployment

```bash
# Build and run everything
#docker-compose up --build

# Run in background
docker-compose up -d --build
```

## Project Structure

```
src/main/kotlin/com/taxportal/clientportal/
├── config/         # Configuration classes
├── controller/     # REST API endpoints
├── dto/            # Data Transfer Objects
├── entity/         # Database entities
├── event/          # Application events
├── exception/      # Custom exceptions
├── repository/     # JPA repositories
└── service/        # Business logic

src/main/resources/
├── application.yml           # Application configuration
└── db/migration/             # Flyway migration scripts
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login |
| GET | `/api/v1/auth/me` | Get current user |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create order |
| GET | `/api/v1/orders` | List orders |
| GET | `/api/v1/orders/{id}` | Get order details |
| POST | `/api/v1/orders/{id}/submit` | Submit order |
| DELETE | `/api/v1/orders/{id}` | Cancel order |

### Documents

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders/{id}/documents` | Upload document |
| GET | `/api/v1/orders/{id}/documents` | List documents |
| GET | `/api/v1/orders/{id}/documents/{docId}` | Get document info |
| GET | `/api/v1/orders/{id}/documents/{docId}/download` | Download document |
| DELETE | `/api/v1/orders/{id}/documents/{docId}` | Delete document |

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders/{id}/pay` | Create checkout session |

### Extractions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders/{id}/extractions` | Get extraction results |
| POST | `/api/v1/orders/{id}/extractions/{docId}/retry` | Retry failed extraction |

### Webhooks (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/webhooks/stripe` | Stripe payment callback |
| POST | `/api/v1/webhooks/nanonets` | Nanonets extraction callback |

## Order Status Flow

```
OPEN -> SUBMITTED -> IN_REVIEW -> PENDING_APPROVAL -> FILED
  |
  v
CANCELLED
```

## Example Requests

### Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","firstName":"John","lastName":"Doe"}'
```

### Create Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"taxYear":2024}'
```

### Upload Document

```bash
curl -X POST http://localhost:8080/api/v1/orders/{orderId}/documents \
  -H "Authorization: Bearer <token>" \
  -F "file=@document.pdf" \
  -F "slip_type=T4"
```

### Create Checkout

```bash
curl -X POST http://localhost:8080/api/v1/orders/{orderId}/pay \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"successUrl":"http://localhost:3000/success","cancelUrl":"http://localhost:3000/cancel"}'
```

## Data Extraction Workflow

When payment is successful:
1. Order status changes to `SUBMITTED`
2. Documents are sent to Nanonets for OCR extraction
3. Nanonets processes documents and calls webhook
4. Extracted data is stored in database (JSON format)
5. Accountant receives email notification
6. Order status changes to `IN_REVIEW`

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing key (min 32 chars) | - |
| `STRIPE_API_KEY` | Stripe secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | - |
| `UPLOAD_DIR` | File upload directory | `uploads` |
| `NANONETS_API_KEY` | Nanonets API key | - |
| `NANONETS_MODEL_ID` | Nanonets model ID | - |
| `APP_BASE_URL` | Application URL for webhooks | `http://localhost:8080` |
| `ACCOUNTANT_EMAIL` | Email for review notifications | - |
| `SMTP_HOST` | SMTP server host | `smtp.gmail.com` |
| `SMTP_USERNAME` | SMTP username | - |
| `SMTP_PASSWORD` | SMTP password | - |

## Running Tests

```bash
./gradlew test
```
