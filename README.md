# ZDF Mediathek MCP Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3+-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Status](https://img.shields.io/badge/status-development-orange.svg)]()
[![MCP](https://img.shields.io/badge/MCP-1.0-purple.svg)]()
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Nicklas2751_zdfmediathek-mcp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Nicklas2751_zdfmediathek-mcp)

## Overview

The ZDF Mediathek MCP Server is a standalone [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server
that provides AI assistants with access to the ZDF Mediathek API. It enables natural language search and retrieval of
German public broadcasting content from ZDF (Zweites Deutsches Fernsehen).

This server can be used independently with any MCP-compatible AI client, including Claude Desktop, GitHub Copilot, VS
Code, IntelliJ IDEA, and others.

**Transport Support:** Supports both Streamable HTTP (default) and Stdio transport. Only one transport can be active at a time. Use the `stdio` Spring profile to enable stdio transport (sets `spring.ai.mcp.server.stdio=true` and disables console output).

### Key Features

- 🔍 **Content Search**: Search ZDF Mediathek content by title, topic, or description
- 📅 **Broadcast Schedules**: Retrieve TV program schedules for all ZDF channels
- 📺 **Current Broadcasts**: Get currently airing programs on ZDF channels
- 🐳 **Docker Ready**: Pre-configured Docker images for easy deployment
- 🔌 **MCP Compatible**: Works with all MCP-compatible AI clients
- 🌐 **Flexible Transport**: Streamable HTTP (default/production) or Stdio (local development)
- 📊 **Comprehensive Logging**: Detailed debug logging for OAuth2 and API requests

## MCP Tools Reference

The server provides the following MCP tools:

### Phase 1 (MVP) - Currently Available

| Tool                     | Description                                     | Key Parameters                                                                                                                                           |
|--------------------------|-------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `search_content`         | Search for content in ZDF Mediathek             | `query` (string, required)<br/>`limit` (number, optional, default: 5)                                                                                    |
| `get_broadcast_schedule` | Get TV schedule for a specific channel and date | `from` (string, required, ISO 8601)<br/>`to` (string, required, ISO 8601)<br/>`tvService` (string, optional)<br/>`limit` (number, optional, default: 10) |
| `get_current_broadcast`  | Get currently airing program on a channel       | `tvService` (string, required)<br/>`limit` (number, optional, default: 10)                                                                               |
| `list_brands`            | List all TV brands/series in the ZDF Mediathek  | `limit` (number, optional, default: 10)                                                                            |
| `list_series`            | List all series available in the ZDF Mediathek  | `limit` (number, optional, default: 4)                                                                            |
| `list_seasons`           | List all seasons available in the ZDF Mediathek | `limit` (number, optional, default: 4)                                                                            |

### Phase 2 - Planned

*None currently.*

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
    "from": "2025-12-27T00:00:00+01:00",
    "to": "2025-12-27T23:59:59+01:00",
    "tvService": "ZDF",
    "limit": 10
  }
}
```

**Get Current Broadcast:**

```json
{
  "tool": "get_current_broadcast",
  "arguments": {
    "tvService": "ZDFneo",
    "limit": 10
  }
}
```

## Usage with AI Clients

### Prerequisites

1. **ZDF API Credentials**: Obtain OAuth2 credentials
   from [ZDF Developer Portal](https://developer.zdf.de/limited-access)
2. **Docker**: Install Docker on your system

### MCP Transport Types

This server supports **two transport modes** (only one active at a time):

#### 1. Streamable HTTP Transport (Default)

Best for: Production, remote access, multiple clients

**Active by default.** The server runs with HTTP transport when started normally.

**Endpoint:** `http://localhost:8080/`

**Docker:**
```bash
docker run -d --name zdf-mcp \
  -p 8080:8080 \
  --restart unless-stopped \
  -e ZDF_CLIENT_ID=your-client-id \
  -e ZDF_CLIENT_SECRET=your-secret \
  ghcr.io/nicklas2751/zdfmediathek-mcp:latest
```

**MCP Client Configuration:**
```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "type": "streamable-http",
      "url": "http://localhost:8080"
    }
  }
}
```

**Advantages:**
- Server runs independently
- Multiple clients can connect simultaneously
- Can be accessed remotely
- Better for debugging (persistent logs)

**Health Check Endpoints:**
- Liveness: `http://localhost:8080/actuator/health/liveness`
- Readiness: `http://localhost:8080/actuator/health/readiness`
- General Health: `http://localhost:8080/actuator/health`

#### 2. Stdio Transport

Best for: Local development, single user, IDE integrations

The MCP client starts and manages the Docker container automatically. The container communicates via stdin/stdout.

**Activate with:** Spring profile `stdio` (recommended) or `SPRING_AI_MCP_SERVER_STDIO=true`

The `stdio` profile:
- Enables stdio transport (`spring.ai.mcp.server.stdio=true`)
- Disables console output (banner, logs) to keep stdin/stdout clean for MCP
- Redirects logs to file (`/tmp/zdfmediathek-mcp.log`)

**Configuration:**
```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

**Note:** The Docker image is pre-configured with buildpack settings that minimize startup output for clean stdio communication.

**Advantages:**
- No manual server management
- Container lifecycle managed by MCP client
- Simpler setup for single user
- Automatic cleanup when client closes

> **Note:** When stdio is enabled, HTTP transport is automatically disabled. Only one transport can be active at a time.

---

### Anthropic Claude Desktop

**Transport:** Stdio (Docker managed by Claude Desktop)

**Configuration File Location:**

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

**Configuration Example:**

```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

**Usage:**

1. Add the configuration to your `claude_desktop_config.json`
2. Restart Claude Desktop
3. Claude will automatically start the Docker container when needed (stdio transport)
4. Ask Claude: "Search for Tatort in ZDF Mediathek"

### GitHub Copilot CLI

**Transport:** Stdio (Docker managed by Copilot CLI)

**Prerequisites:**

```bash
# Install GitHub Copilot CLI (if not already installed)
gh extension install github/gh-copilot
```

**Setup:**

Configure MCP server in `~/.config/github-copilot/mcp-servers.json`:

```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

**Usage:**

```bash
copilot "What's on ZDF tonight?"
copilot "Search for Tatort episodes"
```

### VS Code with GitHub Copilot

**Transport:** Stdio (Docker managed by VS Code)

**Extension Requirements:**

- GitHub Copilot extension
- VS Code version 1.99 or later

**Configuration (.vscode/mcp.json or global settings.json):**

```json
{
  "servers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

**Usage:**

1. Open Copilot Chat in VS Code
2. Select "Agent" mode
3. Click the tools icon to view available MCP servers
4. Ask: "Search for Tatort in ZDF Mediathek"

### IntelliJ IDEA with GitHub Copilot

**Transport:** Stdio (Docker managed by IntelliJ)

**Plugin Requirements:**

- GitHub Copilot plugin
- IntelliJ IDEA 2024.3 or later

**Configuration:**

1. In IntelliJ IDEA, open the GitHub Copilot Chat window
2. Click **"+ Add more tools"** at the bottom of the chat window
3. This opens the `mcp.json` configuration file (usually at `~/.config/github-copilot/intellij/mcp.json`)
4. Add the ZDF Mediathek MCP server configuration:

```json
{
  "servers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

5. Save the file
6. Restart the GitHub Copilot service in IntelliJ (if needed)

**Usage:**

1. Open GitHub Copilot Chat in IntelliJ
2. The ZDF Mediathek tools should appear in the available tools list
3. Ask: "Search for Tatort in ZDF Mediathek"

### Kilo Code

**Transport:** Stdio (Docker managed by Kilo Code)

**Configuration:**

You can configure in either global settings or project-level:

**Option A: Global Configuration (mcp_settings.json):**

1. Click the ⚙️ icon in the Kilo Code pane → "MCP Servers" tab
2. Click "Edit Global MCP"
3. Add configuration:

```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      },
      "alwaysAllow": ["search_content", "get_broadcast_schedule", "get_current_broadcast", "list_brands", "list_series", "list_seasons"],
      "disabled": false
    }
  }
}
```

**Option B: Project-level Configuration (.kilocode/mcp.json):**

Same configuration as above, saved in `.kilocode/mcp.json` in your project root.

**Usage:**

1. Type your request in the Kilo Code chat interface
2. Kilo Code will automatically detect and use the ZDF Mediathek MCP tools
3. Ask: "What's currently on ZDF?"

### Other MCP Clients

For any MCP-compatible client:

**Option 1: Stdio Transport (via Docker)**

```json
{
  "mcpServers": {
    "zdfmediathek-mcp": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e", "SPRING_PROFILES_ACTIVE=stdio",
        "-e", "ZDF_CLIENT_ID",
        "-e", "ZDF_CLIENT_SECRET",
        "ghcr.io/nicklas2751/zdfmediathek-mcp:latest"
      ],
      "env": {
        "ZDF_CLIENT_ID": "your-client-id",
        "ZDF_CLIENT_SECRET": "your-secret"
      }
    }
  }
}
```

**Option 2: Streamable HTTP Transport**

1. Start the server:
   ```bash
   docker run -d --name zdf-mcp \
     -p 8080:8080 \
     -e ZDF_CLIENT_ID=your-client-id \
     -e ZDF_CLIENT_SECRET=your-secret \
     ghcr.io/nicklas2751/zdfmediathek-mcp:latest
   ```

2. Configure your client:
   ```json
   {
     "mcpServers": {
       "zdfmediathek-mcp": {
         "type": "streamable-http",
         "url": "http://localhost:8080"
       }
     }
   }
   ```

## Development Setup

### Prerequisites

- JDK 21 or higher
- Gradle 8+ (wrapper included)
- Docker (for containerized deployment)

### Build from Source

```bash
# Clone repository
git clone https://github.com/Nicklas2751/zdfmediathek-mcp
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

**Application Configuration** (`src/main/resources/application.yaml`):

```yaml
spring:
  application:
    name: zdfmediathek-mcp

  ai:
    mcp:
      server:
        enabled: true
        name: zdf-mediathek-mcp
        protocol: streamable
        streamable-http:
          mcp-endpoint: /

  security:
    oauth2:
      client:
        registration:
          zdf:
            client-id: ${ZDF_CLIENT_ID}
            client-secret: ${ZDF_CLIENT_SECRET}
            client-authentication-method: client_secret_post
            authorization-grant-type: client_credentials
        provider:
          zdf:
            token-uri: https://prod-api.zdf.de/oauth/token

# Server Configuration
server:
  port: ${SERVER_PORT:8080}

# ZDF API Configuration
zdf:
  url: https://prod-api.zdf.de
  client.id: ${ZDF_CLIENT_ID}
  client.secret: ${ZDF_CLIENT_SECRET}

# Logging
logging:
  level:
    eu.wiegandt.zdfmediathekmcp: ${LOG_LEVEL:INFO}
```

**OAuth2 Setup:**

1. Register for ZDF API access at: https://developer.zdf.de/limited-access
2. Obtain client credentials (Client ID and Secret)
3. Set environment variables or pass via Docker

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

## Links

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)
- [ZDF Mediathek](https://www.zdf.de/)
- [Kotlin](https://kotlinlang.org/)
- [Project Plans](.github/plans/)
