# MyProject

A Kotlin Spring Boot multi-module project with automated release management using GitHub Actions, Release Please, and JReleaser.

## Features

- **Multi-module Maven project** with Kotlin and Spring Boot 3.3.x
- **Automated releases** via Release Please (versioning, changelog)
- **Artifact distribution** via JReleaser (Docker, GitHub Packages)
- **SBOM generation** via CycloneDX (JSON and XML formats)
- **Conventional commits** enforcement with commitlint
- **Cherry-pick automation** for backporting fixes to release branches
- **Preview deployments** for pull requests (on-demand via label)
- **Automated dependency updates** via Dependabot
- **Code quality analysis** via SonarQube (SonarCloud or self-hosted)
- **Integration testing** support with Testcontainers
- **API documentation** via OpenAPI/Swagger UI

## Prerequisites

- JDK 17+
- Maven 3.9+
- Docker (for building images locally)
- [pre-commit](https://pre-commit.com/) (for Git hooks)

## Quick Start

```bash
# Clone the repository
git clone https://github.com/thpham/gha-mvn-rflow.git
cd gha-mvn-rflow

# Build and test
mvn clean verify

# Run the API locally
mvn spring-boot:run -pl modules/api

# Access the API
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/api/v1/info

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Project Structure

```
.
├── modules/
│   ├── common/          # Shared utilities, DTOs, constants
│   ├── core/            # Business logic and services
│   └── api/             # REST API (Spring Boot application)
├── .github/
│   └── workflows/       # CI/CD workflows
├── pom.xml              # Parent POM
├── jreleaser.yml        # JReleaser configuration
└── release-please-config.json
```

## Docker Images

Docker images are published to GitHub Container Registry (GHCR).

### Tagging Strategy

| Source       | Tag Pattern                              | Example                                 |
| ------------ | ---------------------------------------- | --------------------------------------- |
| Release      | `{version}`, `{major}.{minor}`, `latest` | `1.2.3`, `1.2`, `latest`                |
| Main branch  | `main-{timestamp}-{sha}`, `edge`         | `main-20250105143052-a1b2c3d`, `edge`   |
| Pull Request | `pr-{N}-{timestamp}-{sha}`, `pr-{N}`     | `pr-42-20250105143052-a1b2c3d`, `pr-42` |

### Pull an Image

```bash
# Latest release
docker pull ghcr.io/thpham/gha-mvn-rflow/myproject-api:latest

# Specific version
docker pull ghcr.io/thpham/gha-mvn-rflow/myproject-api:1.0.0

# Edge (latest from main)
docker pull ghcr.io/thpham/gha-mvn-rflow/myproject-api:edge
```

## Release Flow

This project follows the **Release Flow** branching strategy:

```
main (development)
  │
  ├── feature/xyz → PR → main (squash merge)
  │                   │
  │                   └── Merge → Release Please PR → Merge → v1.2.3
  │                                                      │
  │                                                      └── JReleaser:
  │                                                          - Docker image
  │                                                          - GitHub Packages
  │
  └── release/1.x (LTS/maintenance)
        └── Backport via label: "backport release/1.x"
```

### Creating a Release

1. Merge PRs with conventional commit messages to `main`
2. Release Please automatically creates a Release PR
3. Review and merge the Release PR
4. JReleaser publishes artifacts and Docker images

### Backporting

To backport a fix to a release branch:

1. Merge the fix to `main` first
2. Add label `backport release/1.x` to the merged PR
3. A cherry-pick PR is automatically created

## Commit Conventions

This project uses [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

| Type       | Description   | Version Bump |
| ---------- | ------------- | ------------ |
| `feat`     | New feature   | Minor        |
| `fix`      | Bug fix       | Patch        |
| `docs`     | Documentation | Patch        |
| `perf`     | Performance   | Patch        |
| `refactor` | Refactoring   | Patch        |
| `test`     | Tests         | None         |
| `ci`       | CI/CD         | None         |
| `chore`    | Maintenance   | None         |

### Scopes

- `core` - Core module
- `api` - API module
- `common` - Common module
- `deps` - Dependencies
- `ci` - CI/CD
- `docker` - Docker

### Examples

```bash
feat(api): add user authentication endpoint
fix(core): resolve null pointer in health check
docs: update README with Docker instructions
refactor(common): simplify API response wrapper
```

## Preview Images

To build a preview Docker image for your PR:

1. Add the `preview-image` label to your PR
2. CI will build and push a Docker image to GHCR
3. A comment with `docker pull` instructions will be added to the PR

**Automatic rebuilds**: Once the label is added, every new commit pushed to the PR will automatically build a new image with fresh tags.

**Automatic cleanup**: When a PR is closed (merged or declined), all associated images (`pr-{N}*`) are automatically deleted from the registry.

> **Note**: This builds an image for manual testing - it does not deploy to any environment.

## Development

### Setting Up Pre-commit Hooks

This project uses [pre-commit](https://pre-commit.com/) to run quality checks before each commit:

```bash
# Install pre-commit (choose one)
brew install pre-commit          # macOS
pip install pre-commit           # Python/pip
pipx install pre-commit          # pipx (isolated)

# Install the Git hooks
pre-commit install
pre-commit install --hook-type commit-msg

# Run all hooks manually (optional)
pre-commit run --all-files
```

**Hooks included:**

| Hook                  | Purpose                                 |
| --------------------- | --------------------------------------- |
| `trailing-whitespace` | Remove trailing whitespace              |
| `end-of-file-fixer`   | Ensure files end with newline           |
| `check-yaml`          | Validate YAML syntax                    |
| `check-json`          | Validate JSON syntax                    |
| `hadolint`            | Lint Dockerfile                         |
| `detect-secrets`      | Scan for accidentally committed secrets |
| `actionlint`          | GitHub Actions workflow validation      |
| `spotless-check`      | Kotlin code formatting (ktlint)         |
| `maven-validate`      | POM syntax validation                   |
| `commitlint`          | Conventional commit message validation  |

**Skipping hooks** (when needed):

```bash
# Skip all hooks
git commit --no-verify -m "message"

# Skip specific hook
SKIP=spotless-check git commit -m "message"
```

### Running Tests

```bash
# All tests
mvn test

# Single module
mvn test -pl modules/core

# With coverage
mvn verify
```

### Code Formatting

```bash
# Check formatting
mvn spotless:check

# Apply formatting
mvn spotless:apply
```

### Local Docker Build

```bash
# Build the JAR first
mvn package -pl modules/api -am -DskipTests

# Build Docker image
docker build -t myproject-api:local modules/api

# Run locally
docker run -p 8080:8080 myproject-api:local
```

## API Documentation

When the application is running, API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Code Quality (SonarQube)

This project supports code quality analysis with either SonarCloud or self-hosted SonarQube.

### Setup

1. **For SonarCloud:**

   - Create a project at [sonarcloud.io](https://sonarcloud.io)
   - Add `SONAR_TOKEN` secret to your GitHub repository

2. **For Self-hosted SonarQube:**
   - Add `SONAR_TOKEN` secret to your GitHub repository
   - Add `SONAR_HOST_URL` secret with your SonarQube server URL

### Run Locally

```bash
# Run analysis (requires SONAR_TOKEN environment variable)
mvn verify sonar:sonar -Dsonar.token=$SONAR_TOKEN

# For self-hosted SonarQube
mvn verify sonar:sonar \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.host.url=https://your-sonarqube-server.com
```

## Integration Testing (Testcontainers)

Testcontainers is pre-configured for integration testing with real dependencies.

### Available Containers

Add containers as needed in `modules/api/pom.xml`:

```xml
<!-- PostgreSQL -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <scope>test</scope>
</dependency>

<!-- RabbitMQ -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>rabbitmq</artifactId>
  <scope>test</scope>
</dependency>
```

### Example Test

```kotlin
@Testcontainers
@SpringBootTest
class DatabaseIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("testdb")
    }

    @Test
    fun `should connect to database`() {
        assertThat(postgres.isRunning).isTrue()
    }
}
```

## GitHub Actions Security

This project follows security best practices for GitHub Actions workflows, validated by automated tooling.

### Security Measures

| Practice                          | Description                                                                         |
| --------------------------------- | ----------------------------------------------------------------------------------- |
| **Pinned Actions**                | All actions pinned to SHA hashes (not version tags) to prevent supply chain attacks |
| **Minimal Permissions**           | Explicit `permissions:` blocks at job level with least-privilege principle          |
| **Cache Poisoning Prevention**    | Read-only cache restore for PRs; cache writes only on push events                   |
| **Credential Isolation**          | `persist-credentials: false` on all checkout steps                                  |
| **Fork Protection**               | PR workflows verify `head.repo.full_name == github.repository`                      |
| **Template Injection Prevention** | User-controlled data passed via `env:` blocks, not inline `${{ }}`                  |

### Security Scanning Tools

| Tool                                              | Purpose                                   | Install                   |
| ------------------------------------------------- | ----------------------------------------- | ------------------------- |
| [actionlint](https://github.com/rhysd/actionlint) | Workflow syntax and shellcheck validation | `brew install actionlint` |
| [zizmor](https://github.com/woodruffw/zizmor)     | Security vulnerability scanning           | `brew install zizmor`     |

### Running Security Scans

```bash
# Syntax and shell script validation
actionlint

# Security vulnerability scan
zizmor .github/workflows/

# Both tools should report no findings
```

### Suppressed Findings

Some security findings are intentionally suppressed with documented justifications:

| Rule                 | Workflow     | Justification                                                                                                                   |
| -------------------- | ------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| `dangerous-triggers` | backport.yml | `pull_request_target` required for backport action write permissions. Mitigated by fork check and `persist-credentials: false`. |

Suppressions use inline comments: `# zizmor: ignore[rule-name]`

### Action Version Reference

All actions are pinned to SHA hashes with version comments for maintainability:

```yaml
- uses: actions/checkout@34e114876b0b11c390a56381ad16ebd13914f8d5 # v4
```

To update actions, use tools like [Dependabot](https://docs.github.com/en/code-security/dependabot) or [Renovate](https://github.com/renovatebot/renovate).

## Testing GitHub Actions Locally

This project includes tools for testing GitHub Actions workflows locally before pushing.

### Tools

| Tool                                              | Purpose                         | Install                   |
| ------------------------------------------------- | ------------------------------- | ------------------------- |
| [actionlint](https://github.com/rhysd/actionlint) | Workflow syntax validation      | `brew install actionlint` |
| [zizmor](https://github.com/woodruffw/zizmor)     | Security vulnerability scanning | `brew install zizmor`     |
| [act](https://github.com/nektos/act)              | Run workflows locally           | `brew install act`        |

### Quick Validation (No Docker)

```bash
# Lint all workflows
actionlint

# Security scan
zizmor .github/workflows/

# Lint specific workflow
actionlint .github/workflows/ci.yml
```

### Local Execution with act

```bash
# List available workflows/jobs
act -l

# Run push event (simulates push to main)
act push

# Run specific job
act -j build

# Dry run (show what would run)
act -n

# Run with secrets
cp .secrets.example .secrets  # Edit with your values
act --secret-file .secrets
```

### Limitations

**act** cannot fully simulate all GitHub Actions features:

| Workflow             | act Support | Notes                              |
| -------------------- | ----------- | ---------------------------------- |
| ci.yml               | ⚠️ Partial  | Label conditions hard to simulate  |
| sonar.yml            | ⚠️ Partial  | Requires SONAR_TOKEN               |
| release.yml          | ❌ Limited  | release-please integration complex |
| backport.yml         | ❌ Limited  | Requires merged PR event           |
| cleanup-registry.yml | ❌ Limited  | Requires GHCR access               |

**Recommendation**: Use `actionlint` for syntax validation (catches most issues), and test complex workflows via short-lived branches.

## License

Apache License 2.0
