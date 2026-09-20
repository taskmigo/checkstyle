#!/usr/bin/env python3

import re
import sys
from pathlib import Path

SEMVER = re.compile(
    r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)"
    r"(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?"
    r"(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?$"
)

HARD_CODED = re.compile(
    r'^version\s*=\s*"(?P<version>[^"]+)"\s*$',
    re.MULTILINE,
)

LEGACY = re.compile(
    r'^version\s*=\s*providers\.gradleProperty\("version"\)'
    r'\.orElse\("(?P<version>[^"]+)"\)\.get\(\)\s*$',
    re.MULTILINE,
)


def parse_semver(value: str) -> tuple[int, int, int, tuple]:
    match = SEMVER.fullmatch(value)
    if match is None:
        raise ValueError(f"Invalid SemVer: {value}")

    major, minor, patch, prerelease = match.groups()
    prerelease_key: tuple
    if prerelease is None:
        prerelease_key = (1, ())
    else:
        parts = []
        for identifier in prerelease.split("."):
            if identifier.isdigit():
                if len(identifier) > 1 and identifier.startswith("0"):
                    raise ValueError(
                        f"Invalid SemVer numeric prerelease identifier with leading zero: {value}"
                    )
                parts.append((0, int(identifier)))
            else:
                parts.append((1, identifier))
        prerelease_key = (0, tuple(parts))

    return int(major), int(minor), int(patch), prerelease_key


def hard_coded_version(text: str, source: str) -> str:
    match = HARD_CODED.search(text)
    if match is None:
        raise ValueError(
            f'{source} must contain a hard-coded SemVer assignment like version = "0.1.0"'
        )
    value = match.group("version")
    parse_semver(value)
    return value


def base_version(text: str) -> str:
    match = HARD_CODED.search(text)
    if match is not None:
        value = match.group("version")
        parse_semver(value)
        return value

    # Bootstrap compatibility for the base revision that predates the hard-coded
    # version policy. Once this change reaches next, future bases use HARD_CODED.
    legacy = LEGACY.search(text)
    if legacy is not None:
        value = legacy.group("version")
        parse_semver(value)
        return value

    raise ValueError("Base build.gradle.kts does not declare a recognizable SemVer")


def main() -> int:
    if len(sys.argv) != 3:
        print("usage: check_version.py <base-build.gradle.kts> <head-build.gradle.kts>")
        return 2

    try:
        base = base_version(Path(sys.argv[1]).read_text(encoding="utf-8"))
        head = hard_coded_version(
            Path(sys.argv[2]).read_text(encoding="utf-8"),
            "PR build.gradle.kts",
        )
    except ValueError as error:
        print(f"::error::{error}")
        return 1

    if parse_semver(head) <= parse_semver(base):
        print(
            "::error::Version must increase for every PR merged to next: "
            f"base={base}, head={head}"
        )
        return 1

    print(f"Version increment is valid: {base} -> {head}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
