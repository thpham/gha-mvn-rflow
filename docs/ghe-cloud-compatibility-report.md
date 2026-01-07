# GitHub Actions Compatibility Report

## GHE Cloud & Data Residency Impact Assessment

This report provides a comprehensive inventory of all GitHub Actions used in this repository and their compatibility with GitHub Enterprise Cloud (GHE Cloud) and data residency environments.

---

## Executive Summary

| Risk Level   | Count | Actions                                                                      |
| ------------ | ----- | ---------------------------------------------------------------------------- |
| **Critical** | 3     | docker/login-action, docker/build-push-action, dataaxiom/ghcr-cleanup-action |
| **High**     | 3     | jreleaser/release-action, korthout/backport-action, zizmorcore/zizmor-action |
| **Medium**   | 2     | googleapis/release-please-action, docker/metadata-action                     |
| **Low**      | 10    | All core GitHub actions (checkout, cache, artifacts, etc.)                   |

### Key Findings

1. **3 actions require configuration changes** for GHE Cloud/data residency
2. **3 actions lack enterprise URL inputs** and may need forks or alternatives
3. **2 actions have built-in enterprise support** via optional parameters
4. **10 actions are fully compatible** with no changes required

---

## GHE Cloud & Data Residency Background

### URL Endpoint Changes

| Environment        | API URL                              | Registry URL        | Web URL                          |
| ------------------ | ------------------------------------ | ------------------- | -------------------------------- |
| **GitHub.com**     | `https://api.github.com`             | `https://ghcr.io`   | `https://github.com`             |
| **GHE Cloud**      | `https://api.YOUR-SUBDOMAIN.ghe.com` | Enterprise-specific | `https://YOUR-SUBDOMAIN.ghe.com` |
| **GHE Cloud (EU)** | `https://api.YOUR-SUBDOMAIN.ghe.com` | EU data center      | `https://YOUR-SUBDOMAIN.ghe.com` |

### Data Residency Regions

- **European Union (EU)** - Generally available (Oct 2024)
- **United States (US)** - Available
- **Australia** - Coming soon
- **Japan** - Coming soon

### Important Considerations

- Enterprise Managed Users (EMUs) required for data residency
- Some metadata may still be stored globally
- IP ranges and SSH fingerprints differ from github.com
- Network access rules need updating for client systems

---

## Detailed Action Inventory

### Critical Risk Actions

These actions interact with container registries or external services and **require configuration changes** for GHE Cloud.

#### 1. docker/login-action

| Property       | Value                                                                            |
| -------------- | -------------------------------------------------------------------------------- |
| **Version**    | v3 (SHA: `5e57cd118135c172c3672efd75eb46360885c0ef`)                             |
| **Used In**    | [ci.yml](.github/workflows/ci.yml), [release.yml](.github/workflows/release.yml) |
| **Risk Level** | **CRITICAL**                                                                     |

**Current Configuration:**

```yaml
uses: docker/login-action@5e57cd118135c172c3672efd75eb46360885c0ef
with:
  registry: ${{ env.REGISTRY }} # Currently: ghcr.io
  username: ${{ github.actor }}
  password: ${{ secrets.GITHUB_TOKEN }}
```

**GHE Cloud Impact:**

- Registry URL must be updated to enterprise-specific endpoint
- `GITHUB_TOKEN` scope may need adjustment for enterprise context

**Required Changes:**

```yaml
env:
  REGISTRY: ghcr.YOUR-SUBDOMAIN.ghe.com # Update for GHE Cloud
```

**Enterprise Support:** ✅ Supported via `registry` input parameter

---

#### 2. docker/build-push-action

| Property       | Value                                                                            |
| -------------- | -------------------------------------------------------------------------------- |
| **Version**    | v5/v6 (SHA: `263435318d21b8e681c14492fe198d362a7d2c83`)                          |
| **Used In**    | [ci.yml](.github/workflows/ci.yml), [release.yml](.github/workflows/release.yml) |
| **Risk Level** | **CRITICAL**                                                                     |

**Current Configuration:**

```yaml
uses: docker/build-push-action@263435318d21b8e681c14492fe198d362a7d2c83
with:
  outputs: type=image,name=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }},push-by-digest=true,name-canonical=true,push=true
  cache-from: type=gha,scope=${{ github.repository }}-${{ github.ref_name }}-${{ matrix.platform }}
  cache-to: type=gha,scope=${{ github.repository }}-${{ github.ref_name }}-${{ matrix.platform }},mode=max
```

**GHE Cloud Impact:**

- Registry URL in outputs must point to enterprise registry
- GitHub Actions cache (`type=gha`) should work automatically with GHE Cloud

**Required Changes:**

- Update `REGISTRY` environment variable to GHE Cloud registry endpoint

**Enterprise Support:** ✅ Supported via environment variable configuration

---

#### 3. dataaxiom/ghcr-cleanup-action

| Property       | Value                                                          |
| -------------- | -------------------------------------------------------------- |
| **Version**    | v1.0.16 (SHA: `cd0cdb900b5dbf3a6f2cc869f0dbb0b8211f50c4`)      |
| **Used In**    | [cleanup-registry.yml](.github/workflows/cleanup-registry.yml) |
| **Risk Level** | **CRITICAL**                                                   |

**Current Configuration:**

```yaml
uses: dataaxiom/ghcr-cleanup-action@cd0cdb900b5dbf3a6f2cc869f0dbb0b8211f50c4
with:
  token: ${{ secrets.GITHUB_TOKEN }}
  package: ${{ github.event.repository.name }}/${{ env.IMAGE_NAME }}
  # For GHE Cloud or data residency, change these URLs accordingly
  # registry-url: https://ghcr.io
  # github-api-url: https://api.github.com
```

**GHE Cloud Impact:**

- Both `registry-url` and `github-api-url` must be updated
- Action will fail without correct API endpoint

**Required Changes:**

```yaml
with:
  registry-url: https://ghcr.YOUR-SUBDOMAIN.ghe.com
  github-api-url: https://api.YOUR-SUBDOMAIN.ghe.com
```

**Enterprise Support:** ✅ Full support via `registry-url` and `github-api-url` inputs

---

### High Risk Actions

These actions lack built-in enterprise URL configuration and may require workarounds.

#### 4. jreleaser/release-action

| Property       | Value                                                |
| -------------- | ---------------------------------------------------- |
| **Version**    | v2 (SHA: `90ac653bb9c79d11179e65d81499f3f34527dcd5`) |
| **Used In**    | [release.yml](.github/workflows/release.yml)         |
| **Risk Level** | **HIGH**                                             |

**Current Configuration:**

```yaml
uses: jreleaser/release-action@90ac653bb9c79d11179e65d81499f3f34527dcd5
with:
  arguments: full-release
  setup-java: false
env:
  JRELEASER_GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

**GHE Cloud Impact:**

- No built-in inputs for GitHub API URL or server URL
- JReleaser CLI may use environment variables internally
- May require `GITHUB_API_URL` environment variable

**Potential Workaround:**

```yaml
env:
  GITHUB_API_URL: https://api.YOUR-SUBDOMAIN.ghe.com
  JRELEASER_GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

**Enterprise Support:** ⚠️ Partial - relies on environment variables, not explicit inputs

**Recommendation:** Test thoroughly; may need to use JReleaser CLI directly with explicit configuration

---

#### 5. korthout/backport-action

| Property       | Value                                                |
| -------------- | ---------------------------------------------------- |
| **Version**    | v3 (SHA: `c656f5d5851037b2b38fb5db2691a03fa229e3b2`) |
| **Used In**    | [backport.yml](.github/workflows/backport.yml)       |
| **Risk Level** | **HIGH**                                             |

**Current Configuration:**

```yaml
uses: korthout/backport-action@c656f5d5851037b2b38fb5db2691a03fa229e3b2
with:
  github_token: ${{ secrets.GITHUB_TOKEN }}
  label_pattern: "^backport ([^ ]+)$"
```

**GHE Cloud Impact:**

- **No inputs for GitHub API URL or server URL**
- Action uses `@actions/github` toolkit internally
- Should work if toolkit respects `GITHUB_API_URL` environment variable

**Potential Workaround:**

```yaml
env:
  GITHUB_API_URL: https://api.YOUR-SUBDOMAIN.ghe.com
```

**Enterprise Support:** ⚠️ Unknown - depends on `@actions/github` toolkit behavior

**Recommendation:** Test on GHE Cloud; if fails, consider forking or alternative action

---

#### 6. zizmorcore/zizmor-action

| Property       | Value                                                      |
| -------------- | ---------------------------------------------------------- |
| **Version**    | v0.3.0 (SHA: `e639db99335bc9038abc0e066dfcd72e23d26fb4`)   |
| **Used In**    | [lint-workflows.yml](.github/workflows/lint-workflows.yml) |
| **Risk Level** | **HIGH**                                                   |

**Current Configuration:**

```yaml
uses: zizmorcore/zizmor-action@e639db99335bc9038abc0e066dfcd72e23d26fb4
with:
  token: ${{ secrets.GITHUB_TOKEN }}
```

**GHE Cloud Impact:**

- **No inputs for GitHub API URL**
- Uploads findings to GitHub Advanced Security
- Requires GHAS to be enabled on GHE Cloud

**Dependencies:**

- GitHub Advanced Security must be configured on GHE Cloud instance
- SARIF upload functionality must be available

**Enterprise Support:** ⚠️ Unknown - may work if GHAS is configured

**Recommendation:** Verify GHAS availability on GHE Cloud instance before migration

---

### Medium Risk Actions

These actions have enterprise support or minimal external dependencies.

#### 7. googleapis/release-please-action

| Property       | Value                                                |
| -------------- | ---------------------------------------------------- |
| **Version**    | v4 (SHA: `16a9c90856f42705d54a6fda1823352bdc62cf38`) |
| **Used In**    | [release.yml](.github/workflows/release.yml)         |
| **Risk Level** | **MEDIUM**                                           |

**Current Configuration:**

```yaml
uses: googleapis/release-please-action@16a9c90856f42705d54a6fda1823352bdc62cf38
with:
  token: ${{ secrets.GITHUB_TOKEN }}
  config-file: release-please-config.json
  manifest-file: .release-please-manifest.json
```

**GHE Cloud Impact:**

- Action has built-in enterprise support inputs

**Available Enterprise Inputs:**

```yaml
with:
  github-api-url: https://api.YOUR-SUBDOMAIN.ghe.com
  github-graphql-url: https://api.YOUR-SUBDOMAIN.ghe.com/graphql
  changelog-host: https://YOUR-SUBDOMAIN.ghe.com
```

**Enterprise Support:** ✅ Full support via optional inputs

---

#### 8. docker/metadata-action

| Property       | Value                                                                            |
| -------------- | -------------------------------------------------------------------------------- |
| **Version**    | v5 (SHA: `c299e40c65443455700f0fdfc63efafe5b349051`)                             |
| **Used In**    | [ci.yml](.github/workflows/ci.yml), [release.yml](.github/workflows/release.yml) |
| **Risk Level** | **MEDIUM**                                                                       |

**Current Configuration:**

```yaml
uses: docker/metadata-action@c299e40c65443455700f0fdfc63efafe5b349051
with:
  images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
```

**GHE Cloud Impact:**

- Uses workflow context variables (automatically adjusted by GHE Cloud)
- Image names derived from `REGISTRY` environment variable

**Required Changes:**

- Update `REGISTRY` environment variable

**Enterprise Support:** ✅ Supported via environment variable

---

### Low Risk Actions

These are core GitHub actions that work seamlessly across all GitHub deployments.

| Action                              | Version | Used In                        | Notes                                  |
| ----------------------------------- | ------- | ------------------------------ | -------------------------------------- |
| **actions/checkout**                | v6      | All workflows                  | Core action, fully compatible          |
| **actions/setup-java**              | v5      | ci.yml, release.yml, sonar.yml | Core action, fully compatible          |
| **actions/cache/restore**           | v4      | ci.yml, sonar.yml              | Uses GitHub Actions cache service      |
| **actions/cache/save**              | v4      | ci.yml, sonar.yml              | Uses GitHub Actions cache service      |
| **actions/upload-artifact**         | v6      | ci.yml, release.yml, sonar.yml | Uses GitHub Actions artifact storage   |
| **actions/download-artifact**       | v4      | ci.yml, release.yml            | Uses GitHub Actions artifact storage   |
| **actions/github-script**           | v7      | ci.yml, suggest-backports.yml  | Uses `github` object (auto-configured) |
| **wagoid/commitlint-github-action** | v6      | commitlint.yml                 | Local linting, no external calls       |
| **rhysd/actionlint**                | v1.7.10 | lint-workflows.yml             | Local linting, no external calls       |
| **docker/setup-buildx-action**      | v3      | ci.yml, release.yml            | Local Docker setup                     |

---

## Migration Checklist

### Before Migration

- [ ] Verify GHE Cloud instance details (subdomain, region)
- [ ] Confirm GitHub Advanced Security availability
- [ ] Document current IP allowlists and network rules
- [ ] Identify enterprise-specific registry URL

### Configuration Changes Required

#### 1. Update Environment Variables

In all workflow files, update the `REGISTRY` variable:

```yaml
env:
  REGISTRY: ghcr.YOUR-SUBDOMAIN.ghe.com # Replace with actual GHE registry
```

**Files to modify:**

- `.github/workflows/ci.yml`
- `.github/workflows/release.yml`

#### 2. Update cleanup-registry.yml

Uncomment and configure the GHE Cloud URLs:

```yaml
with:
  registry-url: https://ghcr.YOUR-SUBDOMAIN.ghe.com
  github-api-url: https://api.YOUR-SUBDOMAIN.ghe.com
```

#### 3. Update release.yml (Release Please)

Add enterprise URL inputs:

```yaml
uses: googleapis/release-please-action@16a9c90856f42705d54a6fda1823352bdc62cf38
with:
  token: ${{ secrets.GITHUB_TOKEN }}
  config-file: release-please-config.json
  manifest-file: .release-please-manifest.json
  github-api-url: https://api.YOUR-SUBDOMAIN.ghe.com
  github-graphql-url: https://api.YOUR-SUBDOMAIN.ghe.com/graphql
  changelog-host: https://YOUR-SUBDOMAIN.ghe.com
```

#### 4. Test High-Risk Actions

The following actions need testing on GHE Cloud:

- jreleaser/release-action
- korthout/backport-action
- zizmorcore/zizmor-action

Consider setting `GITHUB_API_URL` environment variable as a workaround.

### Post-Migration Verification

- [ ] CI workflow builds successfully
- [ ] Docker images push to GHE registry
- [ ] Release Please creates PRs correctly
- [ ] Backport automation functions
- [ ] Registry cleanup executes without errors
- [ ] Security scanning (zizmor) uploads to GHAS

---

## Recommendations

### Immediate Actions

1. **Create organization-level variables** for GHE Cloud URLs to centralize configuration
2. **Fork high-risk actions** if testing reveals compatibility issues
3. **Document GHE-specific configuration** in repository README

### Long-term Considerations

1. **Monitor action updates** for improved enterprise support
2. **Consider alternatives** for actions lacking enterprise inputs:
   - `korthout/backport-action` → GitHub's built-in auto-merge or custom script
   - `zizmorcore/zizmor-action` → Run zizmor CLI directly in workflow
3. **Engage with action maintainers** to request enterprise support features

---

## References

- [GitHub Enterprise Cloud with Data Residency](https://docs.github.com/en/enterprise-cloud@latest/admin/data-residency/about-github-enterprise-cloud-with-data-residency)
- [Network Details for GHE.com](https://docs.github.com/en/enterprise-cloud@latest/admin/data-residency/network-details-for-ghecom)
- [GitHub Actions Variables](https://docs.github.com/en/enterprise-cloud@latest/actions/reference/workflows-and-actions/variables)
- [Using Actions in Enterprise](https://docs.github.com/en/enterprise-cloud@latest/admin/github-actions/getting-started-with-github-actions-for-your-enterprise/introducing-github-actions-to-your-enterprise)

---

_Report generated: January 2026_
