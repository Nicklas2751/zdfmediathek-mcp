# Contributing to ZDF Mediathek MCP Server

First off, thank you for considering contributing to the ZDF Mediathek MCP Server! It's people like you that make this project great.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

This project and everyone participating in it is governed by respect and professionalism. By participating, you are expected to uphold this standard. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (code snippets, configuration files, etc.)
- **Describe the behavior you observed** and what you expected
- **Include logs and error messages**
- **Specify your environment** (OS, Java version, Docker version, etc.)

**Template:**

```markdown
**Description:**
A clear description of the bug.

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. ...

**Expected Behavior:**
What you expected to happen.

**Actual Behavior:**
What actually happened.

**Environment:**
- OS: [e.g., macOS 14.0]
- Java Version: [e.g., OpenJDK 21]
- Docker Version: [e.g., 24.0.6]
- Project Version: [e.g., 1.0.0]

**Logs:**
> Paste relevant logs here
```

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the proposed enhancement
- **Explain why this enhancement would be useful**
- **List any alternative solutions** you've considered
- **Include mockups or examples** if applicable

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the development setup** instructions below
3. **Make your changes** following our coding standards
4. **Write or update tests** for your changes (TDD approach preferred)
5. **Ensure all tests pass** (`./gradlew test`)
6. **Ensure code quality passes** (no SonarQube issues)
7. **Update documentation** if needed (README, AGENTS.md, etc.)
8. **Submit your pull request** with a clear description

**Pull Request Template:**

```markdown
## Description
Brief description of changes.

## Related Issues
Fixes #123, relates to #456

## Changes Made
- Change 1
- Change 2
- ...

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] All tests pass locally
- [ ] Manual testing performed

## Checklist
- [ ] Code follows project coding standards
- [ ] No SonarQube issues introduced
- [ ] Documentation updated
- [ ] CHANGELOG.md updated (if applicable)
```

## Development Setup

### Prerequisites

- **JDK 21 or higher** (LTS recommended)
- **Gradle 8+** (wrapper included, no installation needed)
- **Docker** (for containerized testing)
- **Git**

### Setup Steps

```bash
# Clone your fork
git clone https://github.com/YOUR-USERNAME/zdfmediathek-mcp.git
cd zdfmediathek-mcp

# Add upstream remote
git remote add upstream https://github.com/Nicklas2751/zdfmediathek-mcp.git

# Create a branch for your changes
git checkout -b feature/your-feature-name

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run locally
ZDF_CLIENT_ID=your-id ZDF_CLIENT_SECRET=your-secret ./gradlew bootRun
```

### ZDF API Credentials

For development, you need ZDF API credentials:

1. Register at: [https://developer.zdf.de/limited-access](https://developer.zdf.de/limited-access)
2. Obtain Client ID and Secret
3. Set environment variables:
   ```bash
   export ZDF_CLIENT_ID=your-client-id
   export ZDF_CLIENT_SECRET=your-client-secret
   ```

## Coding Standards

### Kotlin Style Guide

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Limit function length (prefer < 30 lines)
- One class per file (except small private helper classes)
- Use data classes for DTOs
- Prefer immutability (val over var)
- Use nullable types appropriately (avoid !! operator)

### Code Organization

```
src/main/kotlin/eu/wiegandt/zdfmediathekmcp/
├── config/              # Configuration classes
├── mcp/
│   └── tools/           # MCP tool service classes
├── model/               # Domain models and DTOs
├── service/             # Business logic services
└── ZdfMediathekMcpApplication.kt
```

### Documentation

- Document public APIs with KDoc
- Explain **why**, not **what** (code should be self-documenting)
- Update README.md for user-facing changes
- Update AGENTS.md for architectural/development changes

## Testing Guidelines

### Test-Driven Development (TDD)

We follow TDD principles:

1. **Write tests first** (red)
2. **Implement code** to make tests pass (green)
3. **Refactor** if needed (refactor)

### Test Coverage

- **Minimum**: 80% overall coverage
- **Critical paths**: 100% coverage for tool handlers and API clients
- Check coverage: `./gradlew test jacocoTestReport`

### Test Types

#### Unit Tests

- Test individual components in isolation
- Mock external dependencies
- Fast execution
- Naming: `*Test.kt`

```kotlin
@Test
fun `searchContent should return results for valid query`() {
    // given
    val query = "Tatort"
    
    // when
    val results = searchContentService.searchContent(query)
    
    // then
    assertThat(results).isNotEmpty()
    assertThat(results[0].title).contains("Tatort")
}
```

#### Integration Tests

- Test component integration with mocked HTTP responses
- Use WireMock for API mocking
- Naming: `*IT.kt` or `*IntegrationTest.kt`

```kotlin
@SpringBootTest
@AutoConfigureWireMock
class SearchContentServiceIT {
    @Test
    fun `should call ZDF API and parse response correctly`() {
        // given: WireMock stub
        // when: actual API call
        // then: verify results
    }
}
```

### Test Naming Convention

Use descriptive names with backticks:

```kotlin
@Test
fun `search content should throw exception for empty query`()

@Test
fun `broadcast schedule should return programs for valid date range`()

@Test
fun `current broadcast should handle API errors gracefully`()
```

## Commit Message Guidelines

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks (dependencies, build, etc.)
- **perf**: Performance improvements
- **ci**: CI/CD changes

### Examples

```
feat(tools): add list_brands tool for listing TV brands

Implements a new MCP tool to list all TV brands/series available
in the ZDF Mediathek. Includes full test coverage and integration
with the ZDF API brands endpoint.

Closes #42
```

```
fix(oauth2): handle token expiration correctly

The OAuth2 token was not being refreshed when expired, causing
API calls to fail. This fix ensures tokens are automatically
refreshed when needed.

Fixes #87
```

```
docs(readme): update Docker setup instructions

Added health check endpoints and clarified container registry URL.
```

## Quality Checks

Before submitting a pull request, ensure:

```bash
# All tests pass
./gradlew test

# No SonarQube issues
./gradlew sonar  # Requires SONAR_TOKEN

# Code builds successfully
./gradlew build

# Docker image builds (optional)
./gradlew bootBuildImage
```

## Questions?

If you have questions:

1. Check [AGENTS.md](AGENTS.md) for architectural decisions
2. Check existing issues and discussions
3. Open a new issue with your question

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉

