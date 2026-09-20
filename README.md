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

The artifact is published to GitHub Packages when a `v<version>` tag is pushed, for example
`v0.1.0`. The release workflow derives the Maven version from the tag and publishes with the
repository-scoped `GITHUB_TOKEN`.

## Build

```shell
./gradlew --no-daemon build
```
