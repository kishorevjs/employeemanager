# Employee Manager API

A Spring Boot REST API for managing employees with AI-powered chat, Kafka event streaming, and MCP server integration.

## Tech Stack

- **Java 21** / **Spring Boot 3.4.1**
- **MySQL** — persistent storage
- **Apache Kafka** — event streaming
- **Spring AI + Anthropic Claude** — AI HR assistant
- **Spring AI MCP Server** — Model Context Protocol tool exposure
- **Micrometer + Prometheus** — metrics

---

## Getting Started

### Prerequisites

| Service       | Default                          |
|---------------|----------------------------------|
| MySQL         | `localhost:3306/employeemanager` |
| Kafka         | `localhost:9092`                 |

### Environment Variables

```bash
export DB_PASSWORD=<your-mysql-password>
export ANTHROPIC_API_KEY=<your-anthropic-api-key>
```

### Run

```bash
./mvnw spring-boot:run
```

Base URL: `http://localhost:8080`

---

## Employee API

All endpoints are prefixed with `/employee`.

### Get All Employees

```
GET /employee/all
```

**Response** `200 OK`

```json
[
  {
    "id": 1,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "jobTitle": "Engineer",
    "phone": "555-0100",
    "imageUrl": "https://example.com/jane.png",
    "employeeCode": "a1b2c3d4-...",
    "salary": 90000
  }
]
```

---

### Get Employee by ID

```
GET /employee/find/{id}
```

| Parameter | Type   | Description     |
|-----------|--------|-----------------|
| `id`      | `Long` | Employee ID     |

**Response** `200 OK` — Employee object
**Error** `404 Not Found` — if employee does not exist

---

### Get Employee by Email

```
GET /employee/findEmail/{email}
```

| Parameter | Type     | Description       |
|-----------|----------|-------------------|
| `email`   | `String` | Employee email    |

**Response** `200 OK` — Employee object
**Error** `404 Not Found` — if employee does not exist

---

### Add Employee

```
POST /employee/add
Content-Type: application/json
```

**Request Body**

```json
{
  "name": "John Smith",
  "email": "john@example.com",
  "jobTitle": "Designer",
  "phone": "555-0199",
  "imageUrl": "https://example.com/john.png",
  "salary": 75000
}
```

> `id` and `employeeCode` are auto-generated and should be omitted.

**Response** `201 Created` — saved Employee with generated `employeeCode`

**Side effect:** publishes a `CREATED` event to the `employee.events` Kafka topic.

---

### Bulk Add Employees

```
POST /employee/add/bulk
Content-Type: application/json
```

**Request Body** — array of Employee objects (same shape as single add, without `id`/`employeeCode`)

```json
[
  { "name": "Alice", "email": "alice@example.com", "jobTitle": "PM", "phone": "555-0101", "salary": 85000 },
  { "name": "Bob",   "email": "bob@example.com",   "jobTitle": "QA", "phone": "555-0102", "salary": 70000 }
]
```

**Response** `201 Created` — array of saved Employee objects

---

### Update Employee

```
PUT /employee/update
Content-Type: application/json
```

**Request Body** — full Employee object including `id`

```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "jobTitle": "Senior Engineer",
  "phone": "555-0100",
  "imageUrl": "https://example.com/jane.png",
  "employeeCode": "a1b2c3d4-...",
  "salary": 110000
}
```

**Response** `200 OK` — updated Employee object

---

### Delete Employee

```
DELETE /employee/delete/{id}
```

| Parameter | Type   | Description     |
|-----------|--------|-----------------|
| `id`      | `Long` | Employee ID     |

**Response** `200 OK`

---

## AI Chat API

An AI HR assistant powered by Anthropic Claude (`claude-haiku-4-5-20251001`). The assistant has access to live employee data via tool calls.

### Chat

```
POST /employee/chat
Content-Type: application/json
```

**Request Body**

```json
{
  "message": "Who is the highest paid employee?"
}
```

**Response** `200 OK`

```json
{
  "reply": "Based on the employee records, Jane Doe is the highest paid employee with a salary of $110,000."
}
```

The assistant can:
- List all employees
- Look up an employee by ID
- Look up an employee by email

---

## Employee Data Model

| Field          | Type     | Notes                              |
|----------------|----------|------------------------------------|
| `id`           | `Long`   | Auto-generated, not updatable      |
| `name`         | `String` |                                    |
| `email`        | `String` |                                    |
| `jobTitle`     | `String` |                                    |
| `phone`        | `String` |                                    |
| `imageUrl`     | `String` |                                    |
| `employeeCode` | `String` | UUID, auto-generated, not updatable|
| `salary`       | `Long`   |                                    |

---

## Kafka Events

Employee creation publishes an event to the `employee.events` topic.

**Topic:** `employee.events`
**Key:** Employee ID (string)

**Event Payload**

```json
{
  "eventType": "CREATED",
  "employeeId": 1,
  "eventTime": "2026-02-28T10:15:30Z"
}
```

**Consumer Group:** `employee-manager`

---

## MCP Server

The application exposes an MCP (Model Context Protocol) server, allowing external AI clients to query employee data directly via standardized tool calls.

**Server name:** `employee-manager-mcp`
**Version:** `1.0.0`

### Endpoints

| Transport | URL |
|-----------|-----|
| HTTP (recommended) | `http://localhost:8080/mcp` |
| SSE (legacy) | `http://localhost:8080/sse` |

### Connecting Claude Code

With the app running, register the server once:

```bash
claude mcp add --transport sse employee-manager-mcp http://localhost:8080/sse
```

Verify the connection:

```bash
claude mcp list
```

Remove mcp server
```bash
claude mcp remove employee-manager-mcp
```

Inside a Claude Code session, run `/mcp` to confirm the server and tools are visible.

### Connecting Claude Desktop

Add the following to your Claude Desktop config file:

- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "employee-manager-mcp": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Team / Project-scoped Config

Create a `.mcp.json` at the project root to share the MCP config with your team:

```json
{
  "mcpServers": {
    "employee-manager-mcp": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Available Tools

| Tool                  | Description                               |
|-----------------------|-------------------------------------------|
| `getAllEmployees`     | Returns the full employee roster          |
| `getEmployeeById`    | Finds an employee by numeric ID           |
| `getEmployeeByEmail` | Finds an employee by email address        |

### Example Prompts

Once connected, ask questions naturally and Claude will call the tools automatically:

```
List all employees
Find the employee with ID 1
Find the employee with email jane@example.com
Who is the highest paid employee?
```

---

## Actuator & Metrics

| Endpoint                          | Description              |
|-----------------------------------|--------------------------|
| `GET /actuator/health`            | Application health check |
| `GET /actuator/info`              | Application info         |
| `GET /actuator/prometheus`        | Prometheus metrics scrape|

---

## Error Handling

| Scenario                   | Exception                 | HTTP Status  |
|----------------------------|---------------------------|--------------|
| Employee not found by ID   | `UserNotFoundException`   | `500`*       |
| Employee not found by email| `UserNotFoundException`   | `500`*       |

> *A global exception handler mapping `UserNotFoundException` to `404` is not yet implemented; the default Spring Boot error response is returned.