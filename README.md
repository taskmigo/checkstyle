# Taskmigo Checkstyle

Taskmigo-specific Checkstyle extensions packaged as a standalone Java library.

## Requirements

- Java 26
- Gradle 9.7.1
- Checkstyle 14.1.0

## Included rule

- `JSpecifyNullMarked` — validates that an existing `package-info.java` opts into JSpecify null-marked semantics with `org.jspecify.annotations.NullMarked`.

The packaged default `checkstyle.xml` also enables Checkstyle's built-in `JavadocPackage` rule. Consumers remain free to provide their own repository-level Checkstyle policy and load only the custom rule from this artifact.

## Coordinates

```text
io.taskmigo:checkstyle:<version>
```

Every pull request targeting `next` must increase the hard-coded project SemVer. Releases can be started manually with the Release workflow, or by pushing a matching `v<version>` tag. The release workflow publishes the version declared in `build.gradle.kts` with the repository-scoped `GITHUB_TOKEN`.

## Build

```shell
./gradlew --no-daemon build
```
