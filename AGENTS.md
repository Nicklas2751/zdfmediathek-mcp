# AGENTS.md - ZDF Mediathek MCP Server

## Project Overview

**Project Name:** zdfmediathek-mcp  
**Type:** Standalone MCP server for ZDF Mediathek API  
**Language:** Kotlin 1.9+  
**Build System:** Gradle with Kotlin DSL  
**JVM Target:** 21  
**Purpose:** Provide MCP tools for searching and retrieving ZDF Mediathek content to AI assistants

### Description

This is a standalone Model Context Protocol (MCP) server that enables AI assistants to interact with the ZDF Mediathek
API. The server provides tools for content search, broadcast schedules, and live program information.

**Parent Project:** mediathek-ai (will be separated into own repository)

---

## Architecture Overview

### High-Level Architecture

```mermaid
graph TB
    Client[MCP Client<br/>Claude/Copilot/etc.] -->|WebSocket/MCP Protocol| Server[Spring Boot<br/>Spring AI MCP Server]
    Server -->|@Tool Annotation| Tools[Tool Functions]
    Tools -->|Dependency Injection| APIClient[WebClient<br/>ZDF API Client]
    APIClient -->|Spring Security OAuth2| Auth[OAuth2 Client]
    APIClient -->|HTTP/HTTPS| ZDF[ZDF API]
    Tools -->|Transform| Transformer[Response Transformer]
    Server -->|SLF4J| Logger[Logback]
    Auth -->|Auto Token Mgmt| TokenCache[Spring Security<br/>Token Cache]
```

### Component Structure

- **Spring Boot Application**: Main application with Spring AI MCP auto-configuration
- **MCP Server**: Automatically configured by Spring AI (WebSocket transport)
- **Tool Functions**: Spring beans with `@Tool` annotation
- **WebClient**: Spring WebFlux HTTP client for ZDF API communication
- **OAuth2 Client**: Spring Security OAuth2 for authentication and token management
- **Data Models**: DTOs for API requests/responses (Jackson serialization)
- **Response Transformers**: Convert ZDF API responses to MCP tool responses
- **Error Handlers**: Convert exceptions to MCP error responses

---

## Core Principles

1. **Standalone**: Can run independently of parent project
2. **Privacy First**: No user data storage, no tracking, no PII logging
3. **Clean Code**: Follows SonarQube standards, maintainable, testable, readable
4. **TDD**: Test-driven development is mandatory
5. **KISS**: Keep it simple - avoid over-engineering and unnecessary abstractions
6. **Security**: OAuth2 credentials via environment variables only

---

## Communication and Documentation Rules

### Language Guidelines

- **User Communication:** German (communicate with Nicklas in German)
- **Code & Documentation:** English (all code, comments, README, etc.)
- **This File (AGENTS.md):** English
- **Other Documentation:** AsciiDoc format in `.github/plans/`

### Diagram Guidelines

- **Preferred Format:** Mermaid (first choice)
- **Fallback Format:** PlantUML (use only when Mermaid cannot express the diagram adequately)

---

## Project Rules and Guidelines

### CRITICAL RULES (Must Follow)

#### Rule #1: ALWAYS CREATE A PLAN FIRST

- **MANDATORY:** Before any implementation, development, or project file changes, create a plan in AsciiDoc format
- **Location:** Store plans in `.github/plans/` directory
- **Wait for Approval:** Do NOT implement until plan is explicitly approved by user
- **Exceptions:**
    - Questions and clarifications don't need a plan
    - Updates to AGENTS.md (this file)
    - Updates to plan files themselves

#### Rule #2: ASK WHEN UNCERTAIN

- When anything is unclear, ask before proceeding
- When multiple solution approaches exist, present options and ask for preference
- Never assume or guess requirements

#### Rule #3: BE BRUTALLY HONEST

- Don't try to please or agree just to be agreeable
- If something is not possible, say so clearly
- If code is bad/insecure/unmaintainable, say it directly and explain why
- When asked for feedback, provide honest assessment with reasoning
- Critical feedback is valued and expected

### Code Quality Standards

- **Quality:** No SonarQube issues - same standards as professional production code
- **Testability:** Code must be easily testable with clear boundaries
- **Maintainability:** Code must be easy to maintain and modify
- **Readability:** Code must be clear and self-documenting
- **Security:** Security considerations are paramount - never compromise security for convenience

### Test-Driven Development (TDD)

**ALWAYS follow TDD workflow:**

1. Write tests FIRST
2. Run tests (they should fail)
3. Implement code to make tests pass
4. Refactor if needed

This is non-negotiable unless explicitly stated otherwise by user.

### Code Changes

- **Minimal Changes:** Make only the smallest possible modifications needed
- **Surgical Edits:** Precise, targeted changes only
- **No Unnecessary Changes:** Don't fix unrelated bugs or refactor working code unless explicitly asked
- **Documentation:** Update only if directly related to changes made

---

## Technology Stack

### Core Technologies

- **Language:** Kotlin 1.9+
- **JVM:** Java 21 (LTS)
- **Build Tool:** Gradle 8+ with Kotlin DSL
- **Framework:** Spring Boot 3.5+ with WebFlux
- **MCP:** Spring AI MCP Server
- **HTTP Client:** Spring WebFlux WebClient
- **JSON Serialization:** Jackson (Spring Boot default)
- **OAuth2:** Spring Security OAuth2 Client
- **Logging:** SLF4J with Logback
- **Testing:** JUnit 5, AssertJ, Spring Boot Test
- **Docker:** Eclipse Temurin JRE (Alpine)

### Key Dependencies

```kotlin
// Spring Boot
implementation("org.springframework.boot:spring-boot-starter-webflux")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
implementation("org.jetbrains.kotlin:kotlin-reflect")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

// Spring AI MCP
implementation("org.springframework.ai:spring-ai-starter-mcp-server-webflux")

// Development Tools
developmentOnly("org.springframework.boot:spring-boot-devtools")
annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

// Testing
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.projectreactor:reactor-test")
testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
testImplementation("org.assertj:assertj-core")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
```

---

## Build and Test Commands

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Check code quality (if detekt is configured)
./gradlew detekt

# Run application
./gradlew run

# Build Docker image
docker build -t zdf-mediathek-mcp .
```

---

## Project Structure

```
zdfmediathek-mcp/
├── .github/
│   ├── plans/                    # Planning documents (AsciiDoc)
│   └── workflows/                # GitHub Actions workflows
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── eu/wiegandt/zdfmediathekmcp/
│   │   │       ├── api/          # ZDF API client
│   │   │       │   ├── ZdfApiClient.kt
│   │   │       │   ├── OAuth2Config.kt
│   │   │       │   └── model/    # API DTOs
│   │   │       ├── tool/         # MCP tool handlers
│   │   │       │   ├── SearchContentTool.kt
│   │   │       │   ├── GetBroadcastScheduleTool.kt
│   │   │       │   └── GetCurrentBroadcastTool.kt
│   │   │       ├── transformer/  # Response transformers
│   │   │       │   └── SearchResultTransformer.kt
│   │   │       ├── model/        # Domain models
│   │   │       │   └── SearchResult.kt
│   │   │       └── ZdfmediathekMcpApplication.kt
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/
│       ├── kotlin/
│       │   └── eu/wiegandt/zdfmediathekmcp/
│       │       ├── api/          # API client tests
│       │       ├── tool/         # Tool handler tests
│       │       ├── transformer/  # Transformer tests
│       │       └── model/        # Model tests
│       └── resources/
│           └── application-test.properties
├── build.gradle.kts              # Build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
├── gradlew                       # Gradle wrapper script
├── gradlew.bat                   # Gradle wrapper (Windows)
├── Dockerfile                    # Docker image configuration
├── .dockerignore                 # Docker ignore patterns
├── .gitignore                    # Git ignore patterns
├── README.md                     # User documentation
├── AGENTS.md                     # This file - agent instructions
└── LICENSE                       # MIT License
```

---

## Error Handling Best Practices

### Required Parameters Missing or Invalid

**✅ DO: Throw exceptions with clear messages**

```kotlin
if (query.isBlank()) {
    throw IllegalArgumentException("Parameter 'query' is required and must not be empty")
}
```

**❌ DON'T: Return empty lists or null**

```kotlin
// BAD - misleading!
if (query.isBlank()) {
    return emptyList()
}
```

**Why?**

- MCP clients (LLMs) can understand and use error messages
- The LLM can help the user correct their request
- Spring AI MCP automatically converts exceptions to JSON-RPC error responses
- Empty results suggest successful search with no matches (which is different!)

### Response Format Guidelines

**✅ DO: Return structured data, not formatted text**

Tools should return data structures (data classes, lists, etc.). The LLM will format the response naturally for the
user.

```kotlin
data class SearchResult(
    val title: String,
    val description: String,
    val broadcastDate: String,
    val url: String
)

fun searchContent(query: String): List<SearchResult>
```

**❌ DON'T: Return pre-formatted text strings**

```kotlin
// BAD - Don't format the response yourself
fun searchContent(query: String): String {
    return "Found 3 results:\n1. Title: ..."
}
```

**Why?**

- MCP protocol works with structured data (JSON)
- LLMs can combine data from multiple tool calls
- LLMs create natural dialogue from structured facts
- Tools provide facts, LLMs create conversation

### API Response Transformation

**✅ DO: Simplify API responses to essential information**

Remove internal IDs, technical metadata, and redundant fields. Keep only user-relevant information:

**Keep:**

- Title, description, teaser
- Broadcast date/time (ISO 8601 format)
- Media URL (for playback)
- Thumbnail/image URL
- Duration (in minutes)
- Channel name
- Genre/category
- Availability information

**Remove:**

- Internal tracking IDs
- Technical metadata (encodings, bitrates, etc.)
- Debug information
- Redundant or duplicate fields

**Example:**

```kotlin
// ZDF API Response (complex, many fields)
data class ZdfApiResponse(
    val id: String,
    val title: String,
    val tracking: TrackingInfo,
    val mainVideoContent: VideoContent,
    // ... 20+ more fields
)

// MCP Tool Response (simplified, user-focused)
data class SearchResult(
    val title: String,
    val description: String,
    val broadcastDate: String,
    val url: String,
    val imageUrl: String?,
    val durationMinutes: Int?
)
```

---

## MCP Tools Implementation Guide

### Tool Naming Convention

- Use snake_case for tool names (MCP convention)
- Be descriptive and concise
- Examples: `search_content`, `get_broadcast_schedule`, `get_current_broadcast`

### Parameter Validation

- Validate all required parameters
- Provide meaningful error messages
- Check parameter types and formats
- Validate enums and constrained values (e.g., channel names)

### Error Handling Patterns

**Parameter Validation:**

```kotlin
@Tool(name = "search_content", description = "Search ZDF Mediathek content")
fun searchContent(query: String, limit: Int = 5): List<SearchResult> {
    // Validate required parameters
    require(query.isNotBlank()) {
        "Parameter 'query' is required and must not be empty"
    }
    require(limit in 1..50) {
        "Parameter 'limit' must be between 1 and 50"
    }

    // ... implementation
}
```

**API Error Handling:**

```kotlin
try {
    val response = apiClient.searchContent(query)
        .timeout(Duration.ofSeconds(10))
        .block()

    return transformer.transform(response)
} catch (e: TimeoutException) {
    logger.error("ZDF API timeout", e)
    throw RuntimeException("ZDF API request timed out, please try again")
} catch (e: WebClientResponseException.TooManyRequests) {
    logger.error("Rate limit exceeded", e)
    throw RuntimeException("ZDF API rate limit exceeded, please try again later")
} catch (e: Exception) {
    logger.error("API error", e)
    throw RuntimeException("Failed to search ZDF Mediathek: ${e.message}")
}
```

**Spring AI MCP automatically:**

- Catches exceptions from tool functions
- Converts them to JSON-RPC error responses
- Sends error messages to the MCP client (LLM)
- The LLM can then explain the error to the user

### Response Formatting

- Return JSON responses
- Use clear, structured data
- Include relevant metadata
- Keep responses concise but informative

### Date/Time Handling

- Use ISO 8601 format (YYYY-MM-DD, YYYY-MM-DDTHH:mm:ss)
- Timezone: CET/CEST (Central European Time)
- Parse dates safely with error handling

### Channel Name Normalization

ZDF API channel names:

- `ZDF` - Main channel
- `ZDFneo` - ZDF neo
- `ZDFinfo` - ZDF info
- `3sat` - 3sat
- `phoenix` - phoenix
- `KiKA` - KiKA (children's channel)

Normalize user input to match API channel names (case-insensitive, handle variations).

---

## ZDF API Integration

### OAuth2 Flow

1. **Client Credentials Grant**
2. Request token from token endpoint
3. Cache token until expiration
4. Refresh token when needed
5. Include token in all API requests (Bearer token)

### API Configuration

- **Base URL:** `https://prod-api.zdf.de`
- **Authentication:** OAuth2 Client Credentials
- **Token Endpoint:** TODO (add from API documentation)
- **Required Scopes:** TODO (add from API documentation)

### Rate Limiting Handling

- Respect API rate limits
- Implement exponential backoff on rate limit errors
- Log rate limit warnings
- Return user-friendly error messages

### Error Response Handling

Handle common HTTP status codes:

- `400 Bad Request` - Invalid parameters
- `401 Unauthorized` - OAuth2 token expired/invalid
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Content not found
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - API error

### Retry Strategy

- Retry on `500`, `502`, `503`, `504` (server errors)
- Do NOT retry on `400`, `401`, `403`, `404` (client errors)
- Maximum 3 retry attempts
- Exponential backoff: 1s, 2s, 4s

---

## Testing Strategy

### Test Coverage

- **Minimum Coverage:** 80% overall
- **Critical Paths:** 100% coverage for tool handlers and API client

### Test Types

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test API client with mocked HTTP responses
3. **Contract Tests**: Verify MCP protocol compliance

### Mock ZDF API Responses

- Create realistic test fixtures in `src/test/resources/`
- Mock OAuth2 token responses
- Mock search results
- Mock schedule data
- Mock error responses

### Test Naming Convention

```kotlin
class SearchContentToolTest : FunSpec({
    context("search_content tool") {
        test("should return results for valid query") { }
        test("should return error for empty query") { }
        test("should handle API errors gracefully") { }
    }
})
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "SearchContentToolTest"

# Run with coverage
./gradlew test jacocoTestReport
```

---

## Security Considerations

### OAuth2 Credentials

- **NEVER** hardcode credentials in source code
- **ALWAYS** use environment variables
- **NEVER** commit `.env` files to Git
- Validate environment variables at startup

### API Quota Monitoring

- Log API usage
- Monitor quota limits
- Alert when approaching limits
- Implement graceful degradation

### Input Validation and Sanitization

- Validate all user inputs
- Sanitize strings before passing to API
- Prevent injection attacks
- Validate date formats

### No PII Logging

- Do NOT log user queries (they might contain PII)
- Log only anonymized or aggregated data
- Sanitize error messages before logging

---

## Known Limitations

### API Limitations

- **Quota Restrictions:** API has usage limits (TODO: specify limits)
- **OAuth2 Token Expiration:** Tokens expire and need renewal
- **Content Availability:** Not all aired content is available in Mediathek
- **Geographic Restrictions:** Some content has geolocation restrictions

### Implementation Limitations

- Phase 1 (MVP) tools only initially
- No caching layer (direct API calls)
- No metrics/monitoring yet
- No advanced search features yet

---

## Future Enhancements

### Phase 2

- Brand/series search
- Detailed content information
- Advanced filtering

### Phase 3

- Semantic tag search
- Content recommendations
- Search suggestions/autocomplete

### Infrastructure

- Caching layer (Redis?)
- Metrics and monitoring (Prometheus?)
- Rate limiting on server side
- Health checks and readiness probes

---

## Links to Documentation

- **Implementation Plan:** [
  `.github/plans/003-spring-ai-mcp-implementation.adoc`](.github/plans/003-spring-ai-mcp-implementation.adoc)
- **Endpoint Analysis:** [
  `.github/plans/zdf-mcp-endpoints-analysis.adoc`](../.github/plans/zdf-mcp-endpoints-analysis.adoc)
- **Quick Reference:** [`.github/plans/zdf-mcp-quick-reference.adoc`](../.github/plans/zdf-mcp-quick-reference.adoc)
- **Parent Project:** [`../README.md`](../README.md)
- **Main AGENTS.md:** [`../AGENTS.md`](../AGENTS.md)

---

## Development Workflow

1. **Create Plan** (in `.github/plans/`, AsciiDoc format)
2. **Wait for Approval** from Nicklas
3. **Write Tests First** (TDD)
4. **Implement Code** to pass tests
5. **Run Tests** and verify
6. **Check Code Quality** (no SonarQube issues)
7. **Update Documentation** (if needed)
8. **Commit Changes** with meaningful message

---

## Questions and Clarifications

If anything is unclear:

1. Check this AGENTS.md file
2. Check plan documents in `.github/plans/`
3. Ask Nicklas (in German)

**Remember:** It's better to ask than to assume!

---

**Last Updated:** 2025-12-19  
**Version:** 0.1.0-SNAPSHOT

