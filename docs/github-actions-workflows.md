# GitHub Actions Workflows Documentation

This document provides comprehensive documentation for all GitHub Actions workflows in this project, including their design, use cases, and interactions.

## Table of Contents

- [Overview](#overview)
- [Workflow Architecture](#workflow-architecture)
- [Workflow Details](#workflow-details)
  - [Release Workflow](#1-release-workflow-releaseyml)
  - [CI Workflow](#2-ci-workflow-ciyml)
  - [Backport Workflow](#3-backport-workflow-backportyml)
  - [Suggest Backports Workflow](#4-suggest-backports-workflow-suggest-backportsyml)
  - [Commitlint Workflow](#5-commitlint-workflow-commitlintyml)
  - [SonarQube Workflow](#6-sonarqube-workflow-sonaryml)
  - [Lint Workflows](#7-lint-workflows-lint-workflowsyml)
  - [Cleanup Registry Workflow](#8-cleanup-registry-workflow-cleanup-registryyml)
- [Versioning Strategy](#versioning-strategy)
- [Scenario Guides](#scenario-guides)
- [Troubleshooting](#troubleshooting)

---

## Overview

This project uses 8 GitHub Actions workflows that work together to provide:

- **Continuous Integration**: Build, test, and lint on every PR and push
- **Automated Releases**: Semantic versioning with Release Please
- **Multi-Architecture Docker Builds**: Native amd64 and arm64 images
- **Backport Automation**: Cherry-pick fixes to maintenance branches
- **Code Quality**: SonarQube analysis and commit message validation
- **Container Registry Management**: Automatic cleanup of old images

| Workflow          | File                    | Purpose                                                       |
| ----------------- | ----------------------- | ------------------------------------------------------------- |
| Release           | `release.yml`           | Orchestrates releases, creates branches, builds Docker images |
| CI                | `ci.yml`                | Build, test, and lint on PRs and pushes                       |
| Backport          | `backport.yml`          | Automatically creates backport PRs when labeled               |
| Suggest Backports | `suggest-backports.yml` | Suggests backport labels for fix/security PRs                 |
| Commitlint        | `commitlint.yml`        | Enforces Conventional Commits specification                   |
| SonarQube         | `sonar.yml`             | Code quality and coverage analysis                            |
| Lint Workflows    | `lint-workflows.yml`    | Validates workflow syntax and security                        |
| Cleanup Registry  | `cleanup-registry.yml`  | Manages container image lifecycle                             |

---

## Workflow Architecture

### High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PULL REQUEST FLOW                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  PR Opened ─┬─→ CI.build (compile & test)                                   │
│             ├─→ CI.lint (Spotless format check)                             │
│             ├─→ Commitlint (validate commit messages)                       │
│             ├─→ SonarQube (code quality analysis)                           │
│             ├─→ Suggest Backports (for fix/security PRs)                    │
│             └─→ CI.build-image (if "preview-image" label)                   │
│                       ↓                                                     │
│                 CI.merge-manifest (Docker manifest merge)                   │
│                                                                             │
│  PR Merged ──→ Backport (creates PRs for labeled backport branches)         │
│           └──→ Cleanup Registry (removes PR-specific images)                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                         RELEASE FLOW (main/master branch)                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Push to main ─┬─→ CI.build + CI.lint + CI.build-image                      │
│                ├─→ SonarQube                                                │
│                └─→ Release.release-please (creates release PR)              │
│                                                                             │
│  Release PR merged ──→ Release.release-please (creates tag)                 │
│                    ├──→ Release.create-release-branch (release/X.Y)         │
│                    ├──→ Release.build (Maven artifacts)                     │
│                    ├──→ Release.build-docker (multi-arch images)            │
│                    ├──→ Release.merge-docker (manifest merge & tags)        │
│                    └──→ Release.distribute (JReleaser deployment)           │
│                                                                             │
│  New release branch ──→ Release.release-please (SNAPSHOT bump PR)           │
│                    └──→ Auto-merge SNAPSHOT PR                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                        BACKPORT FLOW (release branches)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Fix merged to main with "backport release/X.Y" label                       │
│         ↓                                                                   │
│  Backport workflow creates cherry-pick PR to release/X.Y                    │
│         ↓                                                                   │
│  SNAPSHOT version check (warns if target not SNAPSHOT)                      │
│         ↓                                                                   │
│  Backport PR merged → Release workflow creates patch release                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Job Dependencies

```
                          release.yml
                              │
    ┌─────────────────────────┼─────────────────────────┐
    │                         │                         │
    ▼                         ▼                         ▼
release-please ──────→ create-release-branch    (parallel jobs)
    │                         │
    │ release_created?        │ main/master only
    │                         │
    ▼                         ▼
  build ────────────────────────────────────────────────┐
    │                                                   │
    │ artifacts                                         │
    ▼                                                   ▼
build-docker ────────────────────────────────────→ merge-docker
(amd64)     │                                          │
            │                                          │
build-docker ──────────────────────────────────────────┘
(arm64)           (platform digests merged)
    │
    ▼
distribute (JReleaser)
```

---

## Workflow Details

### 1. Release Workflow (`release.yml`)

**Purpose**: Orchestrates the entire release process including version bumping, Docker builds, and artifact distribution.

**Triggers**:

- Push to `main`, `master`, or `release/**` branches
- Manual trigger via `workflow_dispatch`

#### Jobs

| Job                     | Purpose                             | Runs When                |
| ----------------------- | ----------------------------------- | ------------------------ |
| `release-please`        | Creates release PRs and tags        | Always                   |
| `create-release-branch` | Creates `release/X.Y` branch        | Release from main/master |
| `build`                 | Compiles and stages Maven artifacts | Release created          |
| `build-docker`          | Builds multi-arch Docker images     | Release created          |
| `merge-docker`          | Merges manifests and applies tags   | After Docker builds      |
| `distribute`            | Deploys via JReleaser               | Release created          |

#### Key Features

**Dynamic Configuration Selection**:

```yaml
config-file: ${{ contains(github.ref_name, 'release/')
  && 'release-please-config-patch.json'
  || 'release-please-config.json' }}
```

- `main`/`master`: Uses minor version bumping
- `release/*`: Uses patch version bumping

**Auto-Merge SNAPSHOT PRs**:

- PRs with label `autorelease: snapshot` are automatically merged
- Prevents manual intervention for version maintenance

**Release Branch Creation**:

- Creates `release/X.Y` branch from release tag
- Triggers Release workflow on new branch for SNAPSHOT bump

**Multi-Architecture Docker Builds**:

- Native ARM runners (`ubuntu-24.04-arm`) for arm64
- Standard runners (`ubuntu-latest`) for amd64
- 2-3x faster than QEMU emulation

**Docker Tags**:

| Branch      | Tags                                         |
| ----------- | -------------------------------------------- |
| main/master | `{version}`, `{major}.{minor}`, `latest`     |
| release/\*  | `{version}`, `{major}.{minor}` (no `latest`) |

#### Permissions

```yaml
permissions:
  contents: write # Create tags, branches
  pull-requests: write # Create/merge PRs
  packages: write # Push Docker images
  actions: write # Trigger workflow_dispatch
```

---

### 2. CI Workflow (`ci.yml`)

**Purpose**: Continuous integration pipeline for building, testing, and linting code.

**Triggers**:

- Push to `main`, `master`, `release/**` (with path filters)
- Pull requests (opened, synchronized, reopened, labeled)

**Path Filters**: `pom.xml`, `modules/**`

#### Jobs

| Job              | Purpose                | Condition                                     |
| ---------------- | ---------------------- | --------------------------------------------- |
| `build`          | Compile and test       | Always                                        |
| `lint`           | Spotless format check  | Always (parallel)                             |
| `build-image`    | Build Docker images    | PR with `preview-image` label OR push to main |
| `merge-manifest` | Merge Docker manifests | After image builds                            |

#### Key Features

**Concurrency Control**:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.head_ref || github.run_id }}
  cancel-in-progress: true
```

**Image Tagging**:

| Context      | Primary Tag                | Rolling Tag |
| ------------ | -------------------------- | ----------- |
| PR           | `pr-{N}-{timestamp}-{sha}` | `pr-{N}`    |
| Push to main | `main-{timestamp}-{sha}`   | `edge`      |

**Cache Strategy**:

- Maven dependencies cached
- Cache only saved on push (not PRs) to prevent poisoning

**PR Comments**:

- Automatically comments with preview image pull commands

---

### 3. Backport Workflow (`backport.yml`)

**Purpose**: Automatically creates backport PRs when a merged PR has backport labels.

**Trigger**: `pull_request_target` (closed)

**Security Note**: Uses `pull_request_target` for write permissions. Risk mitigated by:

1. Only runs on merged PRs from same repository (not forks)
2. Doesn't execute untrusted PR code

#### Key Features

**Label Pattern**: `^backport ([^ ]+)$`

- Example: `backport release/1.9` creates PR to `release/1.9`

**SNAPSHOT Version Check**:

- After creating backport PR, checks if target branch has SNAPSHOT version
- Adds warning comment if target has non-SNAPSHOT version
- Prevents merging before version bump is complete

```yaml
if [[ "${version}" != *-SNAPSHOT ]]; then
gh pr comment "${pr_number}" --body "⚠️ Warning..."
fi
```

---

### 4. Suggest Backports Workflow (`suggest-backports.yml`)

**Purpose**: Intelligently suggests backport labels for fix/security PRs.

**Trigger**: `pull_request_target` (opened, ready_for_review)

**Skips**:

- Draft PRs
- Fork PRs
- Existing backport PRs

#### Logic

1. Detects `fix:` or `security:` commit types in PR
2. Finds active release branches (sorted by semver)
3. Suggests labels based on PR target:

   - **PR to main**: Suggests backport to 2 latest release branches
   - **PR to release/X.Y**: Suggests backport to main + other branches

4. Creates missing labels if needed
5. Adds informative comment explaining the suggestion

---

### 5. Commitlint Workflow (`commitlint.yml`)

**Purpose**: Enforces Conventional Commits specification.

**Trigger**: Pull requests to `main` and `release/**`

**Skips**: Dependabot PRs

**Uses**: `wagoid/commitlint-github-action@v6`

**Config**: `commitlint.config.mjs`

---

### 6. SonarQube Workflow (`sonar.yml`)

**Purpose**: Code quality and coverage analysis.

**Trigger**: Push/PR to `main`, `master`, `release/**`

**Path Filters**: `pom.xml`, `modules/**`

#### Key Features

- **Graceful Skip**: If `SONAR_TOKEN` not configured, workflow succeeds silently
- **Supports**: SonarCloud (default) and self-hosted SonarQube
- **Skips**: Dependabot PRs, fork PRs
- **Full History**: `fetch-depth: 0` for accurate blame info

---

### 7. Lint Workflows (`lint-workflows.yml`)

**Purpose**: Validates GitHub Actions workflow syntax and security.

**Trigger**: Changes to `.github/workflows/**` or `.github/actionlint.yaml`

#### Jobs

| Job          | Tool                       | Purpose                 |
| ------------ | -------------------------- | ----------------------- |
| `actionlint` | `rhysd/actionlint`         | Validates YAML syntax   |
| `zizmor`     | `zizmorcore/zizmor-action` | Detects security issues |

---

### 8. Cleanup Registry Workflow (`cleanup-registry.yml`)

**Purpose**: Manages container image lifecycle in GitHub Container Registry.

**Triggers**:

- Pull request closed
- Scheduled: Daily at 3 AM UTC
- Manual: `workflow_dispatch`

#### Jobs

**`cleanup-pr`** (on PR close):

- Deletes `pr-{N}` rolling tag
- Deletes `pr-{N}-*` timestamped tags

**`cleanup-scheduled`**:

- Preserves: `edge`, semver tags, open PR images
- Deletes: Old `main-*` tags (keeps 5 newest)
- Deletes: Orphaned closed PR images
- Deletes: Untagged manifests (keeps 4 for in-progress builds)

---

## Versioning Strategy

### Branch-Based Versioning

| Branch          | Config File                        | Version Bump | Example        |
| --------------- | ---------------------------------- | ------------ | -------------- |
| `main`/`master` | `release-please-config.json`       | Minor        | 1.9.0 → 1.10.0 |
| `release/*`     | `release-please-config-patch.json` | Patch        | 1.9.0 → 1.9.1  |

### SNAPSHOT Lifecycle

```
main: 1.10.0-SNAPSHOT (development)
        ↓ (release)
Tag: v1.10.0
        ↓
main: 1.11.0-SNAPSHOT (bumped to next minor)
        ↓
release/1.10 created from v1.10.0
        ↓
release/1.10: 1.10.1-SNAPSHOT (ready for patches)
```

### Conventional Commits

| Type                  | Version Bump | Changelog Section        |
| --------------------- | ------------ | ------------------------ |
| `feat`                | Minor        | Features                 |
| `fix`                 | Patch        | Bug Fixes                |
| `security`            | Patch        | Security                 |
| `perf`                | Patch        | Performance Improvements |
| `docs`                | None         | Documentation            |
| `ci`, `chore`, `test` | None         | Hidden                   |

---

## Scenario Guides

### Scenario 1: Normal Development Flow

1. **Create feature branch** from `main`
2. **Make commits** using Conventional Commits format
3. **Open PR** to `main`
4. **CI checks run** automatically:
   - Build and Tests (Units + perimeter integration tests)
   - Lint check
   - Commitlint validation
   - SonarQube analysis (if configured)
5. **Review and merge** PR
6. **Release Please** creates/updates release PR
7. **Merge release PR** when ready to release

### Scenario 2: Creating a Release

1. **Merge release PR** created by Release Please
2. **Workflow automatically**:
   - Creates git tag (e.g., `v1.10.0`)
   - Creates `release/1.10` branch
   - Builds multi-arch Docker images
   - Pushes to GitHub Container Registry
   - Distributes via JReleaser
3. **SNAPSHOT bump** PR created on release branch
4. **Auto-merged** to prepare for patches

### Scenario 3: Backporting a Fix

1. **Create fix** on `main` branch with `fix:` commit type
2. **Suggest Backports workflow** suggests labels (or add manually)
3. **Add label** (e.g., `backport release/1.9`)
4. **Merge PR** to `main`
5. **Backport workflow** creates cherry-pick PR to `release/1.9`
6. **SNAPSHOT check** warns if target branch needs version bump
7. **Review and merge** backport PR
8. **Release workflow** creates patch release (e.g., `v1.9.1`)

### Scenario 4: Preview Docker Image for PR

1. **Open PR** with code changes
2. **Add label** `preview-image`
3. **CI workflow** builds Docker image
4. **Comment posted** with pull command:
   ```bash
   docker pull ghcr.io/owner/repo/myproject-api:pr-123
   ```
5. **Image deleted** when PR is closed

---

## Troubleshooting

### Common Issues

#### Release Please Not Creating PR

**Symptoms**: No release PR after merging commits

**Checks**:

1. Verify commits follow Conventional Commits format
2. Check `release-please-config.json` for `changelog-types`
3. Ensure branch is `main`, `master`, or `release/*`

**Manual Trigger**:

```bash
gh workflow run release.yml --ref main
```

#### Backport PR Version Conflict

**Symptoms**: Warning about non-SNAPSHOT version

**Cause**: Backport PR created before SNAPSHOT bump was merged

**Resolution**:

1. Wait for SNAPSHOT bump PR to be auto-merged
2. Re-run backport workflow or manually cherry-pick

#### Docker Build Failing on ARM

**Symptoms**: `build-docker` job fails for arm64

**Checks**:

1. Verify ARM runner available (`ubuntu-24.04-arm`)
2. Check Dockerfile compatibility with ARM
3. Review BuildKit cache configuration

#### SNAPSHOT Bump Not Auto-Merging

**Symptoms**: SNAPSHOT PR remains open

**Cause**: Auto-merge not enabled on repository

**Resolution**:

1. Enable auto-merge in repository settings
2. Or manually merge PR with:
   ```bash
   gh pr merge <PR_NUMBER> --squash
   ```

#### Workflow Dispatch Permission Denied

**Symptoms**: `HTTP 403` when triggering workflow

**Resolution**:
Add `actions: write` permission to the job:

```yaml
permissions:
  actions: write
```

### Manual Interventions

#### Manually Create Release Branch

```bash
# Checkout the release tag
git checkout v1.10.0

# Create and push branch
git checkout -b release/1.10
git push origin release/1.10

# Trigger Release workflow for SNAPSHOT bump
gh workflow run release.yml --ref release/1.10
```

#### Manually Trigger Release

```bash
# From main branch
gh workflow run release.yml --ref main

# From release branch
gh workflow run release.yml --ref release/1.10
```

#### Clean Up Stale Images

```bash
# Manual cleanup trigger
gh workflow run cleanup-registry.yml

# Or delete specific image
gh api -X DELETE /user/packages/container/myproject-api/versions/VERSION_ID
```

#### Re-run Failed Backport

```bash
# List recent workflow runs
gh run list --workflow=backport.yml

# Re-run failed run
gh run rerun RUN_ID
```

### Recovery Procedures

#### Recover from Failed Release

1. **Check workflow logs** for error details
2. **Fix the issue** (e.g., test failure, Docker build error)
3. **Delete partial artifacts** if needed:
   ```bash
   # Delete failed release tag
   git push --delete origin v1.10.0
   git tag -d v1.10.0
   ```
4. **Re-run release workflow**:
   ```bash
   gh workflow run release.yml --ref main
   ```

#### Recover from Version Conflict

1. **Identify conflicting versions** in manifest and POMs
2. **Update `.release-please-manifest.json`**:
   ```json
   { ".": "1.10.0" }
   ```
3. **Update POM versions** if needed
4. **Commit and push** to trigger new release cycle

---

## Configuration Files Reference

| File                               | Purpose                               |
| ---------------------------------- | ------------------------------------- |
| `release-please-config.json`       | Main branch: minor version bumps      |
| `release-please-config-patch.json` | Release branches: patch version bumps |
| `.release-please-manifest.json`    | Tracks current release version        |
| `commitlint.config.mjs`            | Commit message validation rules       |
| `jreleaser.yml`                    | Release distribution configuration    |

---

## Permissions Summary

| Workflow          | contents | pull-requests | packages | actions | security-events |
| ----------------- | -------- | ------------- | -------- | ------- | --------------- |
| release           | write    | write         | write    | write   | -               |
| ci                | read     | write         | write    | write   | -               |
| backport          | write    | write         | -        | -       | -               |
| suggest-backports | read     | write         | -        | -       | -               |
| commitlint        | read     | read          | -        | -       | -               |
| sonar             | read     | -             | -        | -       | -               |
| lint-workflows    | read     | -             | -        | read    | write           |
| cleanup-registry  | -        | read          | write    | -       | -               |

---

## Related Documentation

- [Release Please Documentation](https://github.com/googleapis/release-please)
- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Docker BuildKit Documentation](https://docs.docker.com/build/buildkit/)
- [JReleaser Documentation](https://jreleaser.org/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
