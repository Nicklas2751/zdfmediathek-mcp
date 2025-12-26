# ZDF Mediathek MCP Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9+-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Status](https://img.shields.io/badge/status-development-orange.svg)]()
[![MCP](https://img.shields.io/badge/MCP-1.0-purple.svg)]()

## Overview

The ZDF Mediathek MCP Server is a standalone [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server
that provides AI assistants with access to the ZDF Mediathek API. It enables natural language search and retrieval of
German public broadcasting content from ZDF (Zweites Deutsches Fernsehen).

This server can be used independently with any MCP-compatible AI client, including Claude Desktop, GitHub Copilot, VS
Code, IntelliJ IDEA, and others.

### Key Features

- 🔍 **Content Search**: Search ZDF Mediathek content by title, topic, or description
- 📅 **Broadcast Schedules**: Retrieve TV program schedules for all ZDF channels
- 📺 **Current Broadcasts**: Get currently airing programs on ZDF channels
- 🔐 **OAuth2 Authentication**: Secure API access with OAuth2 client credentials flow
- 🐳 **Docker Ready**: Pre-configured Docker images for easy deployment
- 🔌 **MCP Compatible**: Works with all MCP-compatible AI clients

## MCP Tools Reference

The server provides the following MCP tools:

### Phase 1 (MVP) - Available

| Tool                     | Description                                     | Key Parameters                                                              |
|--------------------------|-------------------------------------------------|-----------------------------------------------------------------------------|
| `search_content`         | Search for content in ZDF Mediathek             | `query` (string, required)<br/>`limit` (number, optional, default: 5)       |
| `get_broadcast_schedule` | Get TV schedule for a specific channel and date | `channel` (string, required)<br/>`date` (string, ISO 8601 format, optional) |
| `get_current_broadcast`  | Get currently airing program on a channel       | `channel` (string, required)                                                |

### Phase 2 - Planned

| Tool                  | Description                                     | Key Parameters                 |
|-----------------------|-------------------------------------------------|--------------------------------|
| `search_brands`       | Search for TV brands/series                     | `query` (string, required)     |
| `get_content_details` | Get detailed information about specific content | `contentId` (string, required) |

### Phase 3 - Future

| Tool                  | Description                                      | Key Parameters               |
|-----------------------|--------------------------------------------------|------------------------------|
| `get_brand_details`   | Get detailed information about a TV brand/series | `brandId` (string, required) |
| `suggest_completions` | Get search suggestions                           | `query` (string, required)   |

### Example Tool Usage

**Search Content:**

```json
{
  "tool": "search_content",
  "arguments": {
    "query": "Tatort",
    "limit": 5
  }
}
```

**Get Broadcast Schedule:**

```json
{
  "tool": "get_broadcast_schedule",
  "arguments": {
    "channel": "ZDF",
    "date": "2025-12-19"
  }
}
```

**Get Current Broadcast:**

```json
{
  "tool": "get_current_broadcast",
  "arguments": {
    "channel": "ZDFneo"
  }
}
```

## Usage with AI Clients

### Prerequisites

1. **ZDF API Credentials**: Obtain OAuth2 credentials from ZDF API (TODO: Add registration URL)
2. **Docker**: Install Docker on your system (or use native Kotlin build)

### Docker Setup (General)

The MCP server runs on Spring Boot with WebSocket transport.

Pull and run the Docker image:

```bash
docker pull <registry>/zdf-mediathek-mcp:<version>
docker run -p 8080:8080 \
  -e ZDF_CLIENT_ID=<your-client-id> \
  -e ZDF_CLIENT_SECRET=<your-secret> \
  <registry>/zdf-mediathek-mcp:<version>
```

**MCP Endpoint:** `ws://localhost:8080/mcp` (WebSocket)

> **Note**: Container name, registry URL, and version numbers will be added after initial release.

### Anthropic Claude Desktop

**Configuration File Location:**

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

**Configuration Example:**

```json
{
  "mcpServers": {
    "zdf-mediathek": {
      "command": "docker",
      "args": [
        "run", "-i", "--rm",
        "-e", "ZDF_CLIENT_ID=your-client-id",
        "-e", "ZDF_CLIENT_SECRET=your-secret",
        "<container-name>"
      ]
    }
  }
}
```

**Usage:**

1. Add the configuration to your `claude_desktop_config.json`
2. Restart Claude Desktop
3. Ask Claude: "Search for Tatort in ZDF Mediathek"

### GitHub Copilot CLI

**Setup:**

```bash
# Install GitHub Copilot CLI (if not already installed)
gh extension install github/gh-copilot

# Configure MCP server
# TODO: Add Copilot CLI specific configuration
```

**Usage:**

```bash
gh copilot suggest "What's on ZDF tonight?"
```

### VS Code with GitHub Copilot

**Extension Requirements:**

- GitHub Copilot extension
- MCP support (TODO: Check if additional extension needed)

**Configuration (settings.json):**

```json
{
  "github.copilot.advanced": {
    "mcpServers": {
      "zdf-mediathek": {
        "command": "docker",
        "args": [
          "run", "-i", "--rm",
          "-e", "ZDF_API_CLIENT_ID=your-client-id",
          "-e", "ZDF_API_CLIENT_SECRET=your-secret",
          "<container-name>"
        ]
      }
    }
  }
}
```

### IntelliJ IDEA

**Native MCP Support:**

IntelliJ IDEA has native MCP support (TODO: Verify version requirement).

**Configuration:**

1. Open Settings → Tools → MCP Servers
2. Add new MCP server
3. Configure Docker command or direct connection
4. Add environment variables for OAuth2 credentials

### IntelliJ with GitHub Copilot

**Plugin Requirements:**

- GitHub Copilot plugin

**Configuration:**

1. Install GitHub Copilot plugin from JetBrains Marketplace
2. Configure MCP server in plugin settings (TODO: Add specific steps)

### Kilocode

**Configuration:**

```yaml
# TODO: Add Kilocode specific configuration format
```

### Generic MCP Client (Stdio Transport)

For any MCP-compatible client supporting stdio transport:

```bash
docker run -i --rm \
  -e ZDF_API_CLIENT_ID=your-client-id \
  -e ZDF_API_CLIENT_SECRET=your-secret \
  <container-name>
```

## Development Setup

### Prerequisites

- JDK 21 or higher
- Gradle 8+ (wrapper included)
- Docker (for containerized deployment)

### Build from Source

```bash
# Clone repository
git clone <repo-url>
cd zdfmediathek-mcp

# Build project
./gradlew build

# Run tests
./gradlew test

# Run locally (Spring Boot)
./gradlew bootRun

# Or with environment variables
ZDF_CLIENT_ID=your-id ZDF_CLIENT_SECRET=your-secret ./gradlew bootRun
```

### Configuration

**Environment Variables:**

| Variable            | Required | Description                   | Example          |
|---------------------|----------|-------------------------------|------------------|
| `ZDF_CLIENT_ID`     | Yes      | OAuth2 Client ID from ZDF API | `abc123...`      |
| `ZDF_CLIENT_SECRET` | Yes      | OAuth2 Client Secret          | `xyz789...`      |
| `SERVER_PORT`       | No       | Server port                   | `8080` (default) |

**Application Properties** (`src/main/resources/application.properties`):

```properties
# MCP Server Configuration
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.transport=websocket
spring.ai.mcp.server.path=/mcp
spring.ai.mcp.server.name=zdf-mediathek-mcp
spring.ai.mcp.server.version=0.1.0

# Server Configuration
server.port=${SERVER_PORT:8080}

# OAuth2 Configuration
spring.security.oauth2.client.registration.zdf.client-id=${ZDF_CLIENT_ID}
spring.security.oauth2.client.registration.zdf.client-secret=${ZDF_CLIENT_SECRET}
spring.security.oauth2.client.registration.zdf.authorization-grant-type=client_credentials
spring.security.oauth2.client.registration.zdf.scope=api:read

# ZDF API Provider
spring.security.oauth2.client.provider.zdf.token-uri=https://auth.zdf.de/oauth/token

# Logging
logging.level.eu.wiegandt.zdfmediathekmcp=${LOG_LEVEL:INFO}
```

**OAuth2 Setup:**

1. Register for ZDF API access at: TODO (add URL)
2. Obtain client credentials (Client ID and Secret)
3. Set environment variables or pass via Docker

**API Quota Information:**

- TODO: Add quota limits
- TODO: Add rate limiting information

## API Coverage

This MCP server provides access to the following ZDF API capabilities:

- **Content Search**: Full-text search across ZDF Mediathek content
- **Program Schedules**: TV schedules for all ZDF channels (ZDF, ZDFneo, ZDFinfo, 3sat, etc.)
- **Live Information**: Currently airing programs
- **Content Metadata**: Detailed information about shows, episodes, brands
- **Availability**: Content availability windows and geographic restrictions

For detailed API endpoint documentation, see: [`.github/plans/`](.github/plans/)

### Known Limitations

- Content must be available in Mediathek (not all aired content is available)
- Geographic restrictions may apply (geolocation fields in API)
- API quota limits apply (check your ZDF API agreement)
- OAuth2 token expiration requires renewal

## Examples

### Example Queries

Ask your AI assistant questions like:

- "Search for Tatort episodes in ZDF Mediathek"
- "What's on ZDF tonight?"
- "Show me the TV schedule for ZDFneo today"
- "Is there a documentary about climate change on ZDF?"
- "What's currently playing on 3sat?"

### Expected Tool Call Sequences

**User asks: "What's on ZDF tonight?"**

1. AI calls `get_broadcast_schedule` with channel="ZDF" and today's date
2. AI presents the schedule in a readable format

**User asks: "Find crime shows"**

1. AI calls `search_content` with query="crime" or "Krimi"
2. AI presents search results with titles and descriptions

## Contributing

We welcome contributions! Please ensure:

- Follow Kotlin coding standards and conventions
- Write tests for all new features (TDD approach)
- Update documentation for any API changes
- Run `./gradlew build` successfully before submitting PR
- No SonarQube issues in your code

For detailed contribution guidelines, see [AGENTS.md](AGENTS.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **ZDF** for providing the Mediathek API
- **Anthropic** for the Model Context Protocol specification
- **Spring AI Team** for Spring AI MCP integration
- **Spring Community** for excellent frameworks and tooling
- **Kotlin Community** for the excellent language and ecosystem

## Links

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)
- [ZDF Mediathek](https://www.zdf.de/mediathek)
- [Kotlin](https://kotlinlang.org/)
- [Project Plans](.github/plans/)

