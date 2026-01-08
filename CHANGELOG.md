# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - TBD

### Added
- Initial release of ZDF Mediathek MCP Server
- MCP tools for content search and discovery:
  - `search_content` - Search for content in ZDF Mediathek
  - `get_broadcast_schedule` - Get TV schedule for specific channel and date
  - `get_current_broadcast` - Get currently airing program on a channel
  - `list_brands` - List all TV brands/series in the ZDF Mediathek
  - `list_series` - List all series available in the ZDF Mediathek
  - `list_seasons` - List all seasons available in the ZDF Mediathek
- OAuth2 client credentials flow for ZDF API authentication
- Spring Boot Actuator health checks (liveness, readiness)
- Docker image publishing via GitHub Container Registry
- Comprehensive test suite with unit and integration tests
- CI/CD pipeline with GitHub Actions
- SonarCloud integration for code quality
- Complete API documentation and usage examples

### Technical Details
- Built with Kotlin 2.3.0 and Spring Boot 3.5.9
- Spring AI MCP Server integration with Streamable HTTP transport
- WebFlux reactive HTTP client for ZDF API communication
- Test coverage > 80%
- Production-ready logging and error handling

[Unreleased]: https://github.com/Nicklas2751/zdfmediathek-mcp/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Nicklas2751/zdfmediathek-mcp/releases/tag/v1.0.0

