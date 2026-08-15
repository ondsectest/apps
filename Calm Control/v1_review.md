# Calm Control — v1 Review

Scope: `Calm Control/` only, within `ondsectest/apps`. Sibling folders (`SureStep`,
`WhatsApp_Parse_And_Find_Checker`) were not reviewed.

## Greptile usage

Greptile's MCP tools (`list_code_reviews`, `list_pull_requests`, `trigger_code_review`) were
checked first, both account-wide and scoped to `ondsectest/apps` and `ondsectest/learn`. Result:
**zero pull requests and zero code reviews on record** — the account has no PRs open on this repo
for Greptile to review, and `trigger_code_review` requires a PR number to attach to.

Rather than block on that, the review was done by direct source inspection instead:

- Sparse-cloned only `Calm Control/` (`git sparse-checkout` with `--filter=blob:none`, honoring
  the instruction not to traverse sibling folders).
- Read every tracked file in the folder by hand — all Kotlin sources, Gradle config, resources,
  and docs.
- Verified claims against a real build rather than static reading alone: `./gradlew
  compileDebugKotlin` and `./gradlew testDebugUnitTest` were run at each stage, not just proposed.

So this review should be read as a **manual review**, not a Greptile-generated one. If the repo is
later connected to Greptile with an open PR, `trigger_code_review` would be a genuinely
independent second pass worth running against the same diff — see
[How to verify this yourself](#how-to-verify-this-yourself).

## Findings (initial pass)

| Severity | Finding |
| --- | --- |
| Medium | Release APK committed to the repo, signed with the public Android debug keystore — anyone could sign an update over a sideloaded install. |
| Low | `Converters.kt` silently defaulted an unrecognized `Outcome`/`TriggerCategory` to `CONTROLLED`/`OTHER` instead of surfacing the corruption. |
| Low | `LogViewModel` read the full 90-day Reports window just to compute today's tally. |
| Nit | Fully-qualified `DrawScope` receiver types instead of imports, in two chart components. |
| Nit | No Compose UI test coverage — only the pure domain layer was tested. |

Initial score: **9/10**.

## Fixes applied

**Commit `8360b92`** — fixes all five findings above:
- Removed the debug-signed APK, gitignored `*.apk`, corrected `INSTALL.md`.
- `Converters.kt` now throws on an unrecognized value instead of silently miscoding it.
- Added `ReportsRepository.observeDay()`; `LogViewModel` uses it instead of the full window.
- Replaced fully-qualified `DrawScope` receivers with imports.
- Added Robolectric + Compose UI testing, with 12 new tests across `DailySummaryCard`,
  `WeeklyBarChart`, `TriggerBreakdownCard`, `ReflectionCard`, `QuoteDialog`, and `MomentOrb`.

**Commit `8dbf3fd`** — closes a gap in the fix commit itself: neither logic-level fix
(`Converters.kt`'s throw path, `observeDay()`'s query boundaries) had a test that would fail if
the fix were reverted. Added:
- `ConvertersTest` — round-trips every enum value, and asserts an unrecognized one throws.
- `ReportsRepositoryTest` — a recording fake `TriggerEventDao` that asserts `observeDay()` queries
  exactly one calendar day, and that `observeRecentWindow()` is unchanged.

**Proof, not just assertion:** before committing, `Converters.kt`'s fix was temporarily reverted
to the old silent-default behavior and the new test suite was re-run. `ConvertersTest` failed
(`AssertionError at ConvertersTest.kt:35`) — confirming the test actually detects the defect
rather than passing regardless. The real fix was then restored and the full suite re-verified
green before pushing.

## Current state

- 45/45 unit tests pass (`./gradlew testDebugUnitTest`), 0 failures.
- `./gradlew compileDebugKotlin` clean.
- Score: **10/10** — every finding from the initial pass fixed and independently re-verified
  after the fact, not just fixed and trusted.

## What was not verified

- No manual/runtime QA on a device or emulator — everything above is build- and test-level
  verification only.
- The two people writing the fixes and the tests that check them were the same reviewer (this
  session). That is a real source of bias; an independent second reviewer has not looked at this
  diff.

## How to verify this yourself

1. Read the diff: `git log --stat c344179..8dbf3fd -- "Calm Control"`, or view commits `8360b92`
   and `8dbf3fd` on GitHub.
2. Reproduce the build: `./gradlew clean compileDebugKotlin testDebugUnitTest` from
   `Calm Control/`.
3. Get a second opinion Greptile can't currently give without a PR: open a pull request against
   this diff and run `trigger_code_review` on it, or point another reviewer (human or AI) at the
   same two commits with no context from this review.
4. Sideload a release build on an actual device — the one thing this review could not do.
