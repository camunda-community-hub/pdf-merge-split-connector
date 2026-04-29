# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Camunda 8 outbound connector that merges and splits PDF documents inside BPMN workflows. Built on the Camunda Connector SDK (8.9.1) and Apache PDFBox (3.0.7), targeting Java 21 (`.tool-versions` pins `temurin-21.0.8` + `maven 3.8.4`).

> **Runtime note:** the `camunda/connectors:8.9.1` base image ships **Java 25 + Spring Boot 4.0.5 + Tomcat 11**. We compile to Java 21 bytecode and rely on JVM backward compatibility — don't be surprised when container logs report Java 25.

## Common commands

```bash
mvn clean package          # Build thin + shaded fat JAR; also regenerates element-templates/pdf-connector.json
mvn clean verify           # Run all tests + JaCoCo report (target/site/jacoco/)
mvn -Pcoverage-enforced verify   # Same, but fails build under 80% instruction / 75% branch coverage
mvn test -Dtest=PdfConnectorTest                   # Single test class
mvn test -Dtest=PdfConnectorTest#shouldMergeTwoPdfs # Single test method
```

The full Camunda runtime integration test (`integration/PdfConnectorIntegrationTest`) is `@Disabled` by default — it pulls `camunda/camunda:8.9.1` via Docker. Enable manually when Docker is available.

Run the connector locally inside a Spring Boot runtime by starting the **test-scope** main class `io.camunda.example.classic.LocalConnectorRuntime` (Camunda config lives in `src/main/resources/application.properties`, copied from the `.template` file — never commit the real one).

Enable the secret-scan pre-commit hook once per clone: `git config core.hooksPath .githooks`. Bypass with `SKIP_SECRET_CHECK=1` only if absolutely necessary.

## Architecture

This connector uses the **Operations API** pattern (`OutboundConnectorProvider` + `@Operation`), which is newer than the classic `OutboundConnectorFunction` single-entry-point style. One class exposes multiple operations and the SDK routes by `operationId`:

- [src/main/java/io/camunda/example/operations/PdfConnectorProvider.java](src/main/java/io/camunda/example/operations/PdfConnectorProvider.java) — the `@OutboundConnector` + `@ElementTemplate` entry point. Each `@Operation`-annotated method (mergePdfs, splitByPage, splitByRange, splitByBookmark, splitBySize) is a thin delegate to `PdfOperations`. The `@Variable` parameter is a discriminated request record nested under `PdfConnectorRequest`.
- [src/main/java/io/camunda/example/operations/PdfOperations.java](src/main/java/io/camunda/example/operations/PdfOperations.java) — all PDFBox logic. Static methods load PDFs via `Loader.loadPDF(RandomAccessReadBuffer)`, manipulate them, and create new `Document` objects via `context.create(DocumentCreationRequest...)`. Errors must be thrown as `ConnectorException` with one of the documented codes (`PDF_MERGE_ERROR`, `PDF_SPLIT_ERROR`, `NO_BOOKMARKS`, `INVALID_PAGE_RANGE`).
- [src/main/java/io/camunda/dto/PdfConnectorRequest.java](src/main/java/io/camunda/dto/PdfConnectorRequest.java) and [PdfConnectorResult.java](src/main/java/io/camunda/dto/PdfConnectorResult.java) — Java records with `@TemplateProperty` and Jakarta Bean Validation annotations. **The element template is generated from these records at `mvn package` time** by the `element-template-generator-maven-plugin`, so any field/label/group change must be made in the record (do not hand-edit `element-templates/pdf-connector.json`).
- [src/main/resources/META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider](src/main/resources/META-INF/services/io.camunda.connector.api.outbound.OutboundConnectorProvider) — SPI registration; must list `PdfConnectorProvider` for the runtime to discover the connector.

### Shading (important)

`maven-shade-plugin` relocates `org.apache.pdfbox` → `io.camunda.connector.pdf.shaded.pdfbox` (and fontbox similarly) in the fat JAR. This avoids classpath clashes with other connectors in the same runtime. The connector SDK is `<scope>provided</scope>` and supplied by the runtime — do not bundle it.

### Test layout quirk

`LocalConnectorRuntime` (the dev entry point) lives under `src/test/java/io/camunda/example/classic/`, not `src/main/java`. It is a Spring Boot starter that pulls in `spring-boot-starter-camunda-connectors` (test scope). Don't move it to main without also moving its dependencies out of test scope.

## Conventions

- Versions in `pom.xml`, `Dockerfile` (`COPY target/pdf-merge-split-connector-<version>.jar`), and the README release notes must stay in sync. Bump all three on release.
- The Dockerfile copies the connector JAR to `/opt/custom/`, not `/opt/app/`. The 8.9.x base image's `start.sh` sets `-Dloader.path=/opt/custom/`; `/opt/app/` is reserved for the runtime jar itself and putting the connector there will crash with a `NoClassDefFoundError` for `OutboundConnectorProvider`. (Earlier 8.8.x images accepted `/opt/app/`.)
- `element-template-generator-core` is a build-time-only dependency and must remain `<scope>provided</scope>` — it transitively pulls in slf4j-api, Jackson, scala-library, etc., which collide with the runtime if shaded into the fat JAR.
- README documents user-facing behavior; keep operation input/output JSON schemas there in sync with the DTOs.
- Coverage is reported on every `verify` but only enforced under the `coverage-enforced` profile — CI runs the unenforced variant.
