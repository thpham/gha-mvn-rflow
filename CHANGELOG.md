# Changelog

## [1.1.2](https://github.com/thpham/gha-mvn-rflow/compare/v1.1.1...v1.1.2) (2026-01-07)


### Bug Fixes

* **release:** update jreleaser.yml for v2.0.0 compatibility ([#49](https://github.com/thpham/gha-mvn-rflow/issues/49)) ([#50](https://github.com/thpham/gha-mvn-rflow/issues/50)) ([85f4888](https://github.com/thpham/gha-mvn-rflow/commit/85f48881a658ed69c164ca5c454dcd0be9ad0884))

## [1.1.1](https://github.com/thpham/gha-mvn-rflow/compare/v1.1.0...v1.1.1) (2026-01-07)


### Bug Fixes

* **api:** validate delayMs parameter in chaos endpoint ([#35](https://github.com/thpham/gha-mvn-rflow/issues/35)) ([#36](https://github.com/thpham/gha-mvn-rflow/issues/36)) ([84b597a](https://github.com/thpham/gha-mvn-rflow/commit/84b597aba026b1c0e3fae815f39602fc0fa69d9f))
* **ci:** add target-branch to release-please for patch releases ([b920311](https://github.com/thpham/gha-mvn-rflow/commit/b920311aeec2928b71c8af3024a4f4cc551855e0))
* **ci:** auto-merge SNAPSHOT bump PRs after release ([ffd8d81](https://github.com/thpham/gha-mvn-rflow/commit/ffd8d813fd6f66b9e94c787bb03e9e6f8eac2b3d))
* **ci:** correct jq filter for release-please labels array ([8bb1568](https://github.com/thpham/gha-mvn-rflow/commit/8bb1568a9adcd3da80e23a973bde81137a7d4716))

## [1.1.0](https://github.com/thpham/gha-mvn-rflow/compare/myproject-v1.0.0...myproject-v1.1.0) (2026-01-07)


### Features

* add optional enhancements for code quality and testing ([a805ab8](https://github.com/thpham/gha-mvn-rflow/commit/a805ab830abe402f683c1b9aaa3d42054e52aff9))
* **api:** add /version endpoint for build information ([#30](https://github.com/thpham/gha-mvn-rflow/issues/30)) ([a825fca](https://github.com/thpham/gha-mvn-rflow/commit/a825fca93701fa995c9651ef2cbc9fd0eb8cf877))
* **ci:** add auto-suggest backport labels for fix/security PRs ([e7ecdd7](https://github.com/thpham/gha-mvn-rflow/commit/e7ecdd70759189417ad57e83558ad3ebc9e9845a))
* **dev:** add Nix flake with development shell ([102ecc3](https://github.com/thpham/gha-mvn-rflow/commit/102ecc3d46ed8d7f98f30ca7226e5d3ae265d39f))
* initial project setup with CI/CD pipeline ([a8f9b74](https://github.com/thpham/gha-mvn-rflow/commit/a8f9b746da91b886493bba1c1386a39ea74d11a9))
* **observability:** add OpenTelemetry observability stack with Grafana LGTM ([#26](https://github.com/thpham/gha-mvn-rflow/issues/26)) ([49c2c5e](https://github.com/thpham/gha-mvn-rflow/commit/49c2c5eaee50eefa1037ef0353805dca51b5eed7))


### Bug Fixes

* **ci:** add cooldown to dependabot for supply chain security ([c342181](https://github.com/thpham/gha-mvn-rflow/commit/c342181fbadc409e019165d9645ec2cfa28fb1be))
* **ci:** address zizmor security findings ([c6814a5](https://github.com/thpham/gha-mvn-rflow/commit/c6814a53cad4b74db24252b3d33d8fd0f61206db))
* **ci:** bump actionlint to v1.7.10 and add ARM runner label ([aed694a](https://github.com/thpham/gha-mvn-rflow/commit/aed694af06d3b886f873c4e4c512ae21949c8632))
* **ci:** replace broken snok cleanup action with dataaxiom ([da3e7a6](https://github.com/thpham/gha-mvn-rflow/commit/da3e7a6260b73a76de65b6767d0126049a996090))
* **ci:** resolve template injection in cleanup-registry workflow ([04f6664](https://github.com/thpham/gha-mvn-rflow/commit/04f66640a1b3e9fbef2d606e8a4a96caf0c8bcba))
* **ci:** simplify zizmor workflow to use built-in SARIF upload ([f26ad56](https://github.com/thpham/gha-mvn-rflow/commit/f26ad5635d300670f5e9a8dc7c59f821c341d913))
* **ci:** skip PR image cleanup when no images exist ([c68e280](https://github.com/thpham/gha-mvn-rflow/commit/c68e280264cbb291d843292ce9e93923f02e9bb1))
* **ci:** skip SonarQube analysis when SONAR_TOKEN is not configured ([e829411](https://github.com/thpham/gha-mvn-rflow/commit/e829411ba8a0ea26ecc094a4e3269482f503ee89))
* **ci:** update cleanup-registry workflow for snok/container-retention-policy v3 ([8e62c35](https://github.com/thpham/gha-mvn-rflow/commit/8e62c359a0940252d9dfd13a2a93c231463f45cf))
* **docker:** use UBI 10 base image for ARM64 support ([34c1396](https://github.com/thpham/gha-mvn-rflow/commit/34c1396bf50042afba202db63db477447a198708))
* **release:** reset manifest to generate initial changelog ([e7ab262](https://github.com/thpham/gha-mvn-rflow/commit/e7ab26201e8f2957876249231804c8ad775f0dac))


### Performance Improvements

* **ci:** use native ARM runners for multi-arch Docker builds ([cd088f3](https://github.com/thpham/gha-mvn-rflow/commit/cd088f3164f81554ac3d055e0a25b63e6fece848))
* **docker:** optimize image layers for registry storage efficiency ([d531a47](https://github.com/thpham/gha-mvn-rflow/commit/d531a4702e67da339fdc65c8c79bf814fd9f7ce9))


### Code Refactoring

* **ci:** rename deploy-preview label to preview-image and add PR cleanup ([4654415](https://github.com/thpham/gha-mvn-rflow/commit/4654415ef056ea67408e81992a453dbf545da686))


### Documentation

* add GHE Cloud and data residency compatibility report ([c6d25cb](https://github.com/thpham/gha-mvn-rflow/commit/c6d25cb2ac994885979c004f8843d5e05544be90))
* add GitHub repository settings section to README ([4affd67](https://github.com/thpham/gha-mvn-rflow/commit/4affd67a7976eb14c21ef783a56784cf8c71637d))
* **ci:** add GHE Cloud/data residency URL comments to cleanup workflow ([2142ff2](https://github.com/thpham/gha-mvn-rflow/commit/2142ff2c5e5365d97eac4b9562c3cb231f77ad52))
* update README with current dependency versions ([8460de9](https://github.com/thpham/gha-mvn-rflow/commit/8460de992f5591b9f3db10e0390cf9a8a2df0d09))


### Build System

* **deps:** upgrade GitHub Actions and Maven plugins ([d7ce324](https://github.com/thpham/gha-mvn-rflow/commit/d7ce324bac173d0ba2b77f3d28c55b10e20406b3))
* **deps:** upgrade to Spring Boot 4.0.1, Kotlin 2.3.0, and other major deps ([#19](https://github.com/thpham/gha-mvn-rflow/issues/19)) ([828b2da](https://github.com/thpham/gha-mvn-rflow/commit/828b2dac4c77e0ca41ede6b7a7d3e7c7d13be45d))
* enable reproducible builds for Docker layer cache optimization ([f29d13f](https://github.com/thpham/gha-mvn-rflow/commit/f29d13f803b8253d6d951312b32c3295e8c6f091))

## 1.0.0 (2026-01-07)


### Features

* add optional enhancements for code quality and testing ([a805ab8](https://github.com/thpham/gha-mvn-rflow/commit/a805ab830abe402f683c1b9aaa3d42054e52aff9))
* **ci:** add auto-suggest backport labels for fix/security PRs ([e7ecdd7](https://github.com/thpham/gha-mvn-rflow/commit/e7ecdd70759189417ad57e83558ad3ebc9e9845a))
* **dev:** add Nix flake with development shell ([102ecc3](https://github.com/thpham/gha-mvn-rflow/commit/102ecc3d46ed8d7f98f30ca7226e5d3ae265d39f))
* initial project setup with CI/CD pipeline ([a8f9b74](https://github.com/thpham/gha-mvn-rflow/commit/a8f9b746da91b886493bba1c1386a39ea74d11a9))
* **observability:** add OpenTelemetry observability stack with Grafana LGTM ([#26](https://github.com/thpham/gha-mvn-rflow/issues/26)) ([49c2c5e](https://github.com/thpham/gha-mvn-rflow/commit/49c2c5eaee50eefa1037ef0353805dca51b5eed7))


### Bug Fixes

* **ci:** add cooldown to dependabot for supply chain security ([c342181](https://github.com/thpham/gha-mvn-rflow/commit/c342181fbadc409e019165d9645ec2cfa28fb1be))
* **ci:** address zizmor security findings ([c6814a5](https://github.com/thpham/gha-mvn-rflow/commit/c6814a53cad4b74db24252b3d33d8fd0f61206db))
* **ci:** bump actionlint to v1.7.10 and add ARM runner label ([aed694a](https://github.com/thpham/gha-mvn-rflow/commit/aed694af06d3b886f873c4e4c512ae21949c8632))
* **ci:** replace broken snok cleanup action with dataaxiom ([da3e7a6](https://github.com/thpham/gha-mvn-rflow/commit/da3e7a6260b73a76de65b6767d0126049a996090))
* **ci:** resolve template injection in cleanup-registry workflow ([04f6664](https://github.com/thpham/gha-mvn-rflow/commit/04f66640a1b3e9fbef2d606e8a4a96caf0c8bcba))
* **ci:** simplify zizmor workflow to use built-in SARIF upload ([f26ad56](https://github.com/thpham/gha-mvn-rflow/commit/f26ad5635d300670f5e9a8dc7c59f821c341d913))
* **ci:** skip PR image cleanup when no images exist ([c68e280](https://github.com/thpham/gha-mvn-rflow/commit/c68e280264cbb291d843292ce9e93923f02e9bb1))
* **ci:** skip SonarQube analysis when SONAR_TOKEN is not configured ([e829411](https://github.com/thpham/gha-mvn-rflow/commit/e829411ba8a0ea26ecc094a4e3269482f503ee89))
* **ci:** update cleanup-registry workflow for snok/container-retention-policy v3 ([8e62c35](https://github.com/thpham/gha-mvn-rflow/commit/8e62c359a0940252d9dfd13a2a93c231463f45cf))
* **docker:** use UBI 10 base image for ARM64 support ([34c1396](https://github.com/thpham/gha-mvn-rflow/commit/34c1396bf50042afba202db63db477447a198708))
* **release:** reset manifest to generate initial changelog ([e7ab262](https://github.com/thpham/gha-mvn-rflow/commit/e7ab26201e8f2957876249231804c8ad775f0dac))


### Performance Improvements

* **ci:** use native ARM runners for multi-arch Docker builds ([cd088f3](https://github.com/thpham/gha-mvn-rflow/commit/cd088f3164f81554ac3d055e0a25b63e6fece848))
* **docker:** optimize image layers for registry storage efficiency ([d531a47](https://github.com/thpham/gha-mvn-rflow/commit/d531a4702e67da339fdc65c8c79bf814fd9f7ce9))


### Code Refactoring

* **ci:** rename deploy-preview label to preview-image and add PR cleanup ([4654415](https://github.com/thpham/gha-mvn-rflow/commit/4654415ef056ea67408e81992a453dbf545da686))


### Documentation

* add GHE Cloud and data residency compatibility report ([c6d25cb](https://github.com/thpham/gha-mvn-rflow/commit/c6d25cb2ac994885979c004f8843d5e05544be90))
* add GitHub repository settings section to README ([4affd67](https://github.com/thpham/gha-mvn-rflow/commit/4affd67a7976eb14c21ef783a56784cf8c71637d))
* **ci:** add GHE Cloud/data residency URL comments to cleanup workflow ([2142ff2](https://github.com/thpham/gha-mvn-rflow/commit/2142ff2c5e5365d97eac4b9562c3cb231f77ad52))
* update README with current dependency versions ([8460de9](https://github.com/thpham/gha-mvn-rflow/commit/8460de992f5591b9f3db10e0390cf9a8a2df0d09))


### Build System

* **deps:** upgrade GitHub Actions and Maven plugins ([d7ce324](https://github.com/thpham/gha-mvn-rflow/commit/d7ce324bac173d0ba2b77f3d28c55b10e20406b3))
* **deps:** upgrade to Spring Boot 4.0.1, Kotlin 2.3.0, and other major deps ([#19](https://github.com/thpham/gha-mvn-rflow/issues/19)) ([828b2da](https://github.com/thpham/gha-mvn-rflow/commit/828b2dac4c77e0ca41ede6b7a7d3e7c7d13be45d))
* enable reproducible builds for Docker layer cache optimization ([f29d13f](https://github.com/thpham/gha-mvn-rflow/commit/f29d13f803b8253d6d951312b32c3295e8c6f091))

## Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<!-- Release Please will automatically update this file -->
