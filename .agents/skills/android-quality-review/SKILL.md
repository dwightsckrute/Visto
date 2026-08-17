---
name: android-quality-review
description: Review relevant Visto Android changes before handoff for architecture, Kotlin, Compose, state, navigation, dependencies, accessibility, tests, lint, build, data safety, and release compatibility. Trigger before completing nontrivial code changes or when explicitly asked for a quality audit. Do not use as a substitute for implementing the requested change.
---

# Android Quality Review

Perform a risk-proportional final audit using Visto's real constraints and Gradle tasks.

## When to use

- Before handing off a nontrivial Android code change.
- After navigation, theme, screen, ViewModel, repository, Room, backup, WorkManager, dependency, or release changes.
- For an explicit code-quality or regression review.

## When not to use

- Documentation-only typo changes unless verification is requested.
- As a reason to broaden a focused task into unrelated refactoring.

## Procedure

1. Read `AGENTS.md`; consult `docs/ARCHITECTURE.md` and/or `docs/DESIGN_SYSTEM.md` according to touched scope.
2. Review the complete diff and dirty worktree. Separate user changes from task changes and do not rewrite unrelated files.
3. Check product invariants: application ID, signing, namespace, Room migrations, backup compatibility, language/TMDB behavior, branch and release rules.
4. Check architecture: feature placement, immutable state, lifecycle collection, repository boundaries, explicit events/effects, navigation callbacks, and no incidental abstractions.
5. Check Compose/Kotlin: stable keys/parameters, state ownership, effects, lazy layouts, localization, semantic color roles, accessibility, font scale, and no dead code/imports.
6. Check dependencies and experimental APIs. Require a reason and compatibility analysis for every addition/update.
7. Run the smallest checks while iterating, then the strongest proportionate suite. Never claim ktlint or Detekt; they are not configured.
8. For UI, report which of Spanish/English, light/dark/dynamic/OLED, width/font scale, device/preview, insets, and motion were actually inspected.
9. For Room/backup, verify upgrade/import from existing data, not only a fresh install.
10. Report commands, warnings, failures, unavailable environment checks, residual risks, and every changed file.

## Checklist

- [ ] Scope matches the request and unrelated work is preserved.
- [ ] Architecture and data ownership remain clear.
- [ ] Kotlin is idiomatic; no unexplained suppression, dead code, or duplicate utility exists.
- [ ] Compose state, lifecycle, keys, recomposition, localization, and accessibility were reviewed.
- [ ] Navigation semantics and state restoration are appropriate.
- [ ] No dependency/plugin/API was invented or added without analysis.
- [ ] Application ID, signing, Room, backups, TMDB language, and release invariants are safe.
- [ ] Relevant unit/UI/migration tests exist or the gap is reported.
- [ ] Gradle checks and `git diff --check` passed, or exact blockers are reported.
- [ ] Final handoff separates applied changes, proposals, pending decisions, verification, and risks.

## Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew test lint assembleRelease
git diff --check
```

Choose commands proportional to risk; the last Gradle command is release-equivalent. Report missing Android SDK, JDK 17, TMDB key, signing, device, or network. Do not run nonexistent `ktlintCheck` or `detekt` tasks.

## Required references

- `AGENTS.md`
- `docs/ARCHITECTURE.md` for structural/data/navigation changes
- `docs/DESIGN_SYSTEM.md` for visual/Compose changes
- Relevant Gradle files, manifest, workflow, source, tests, Room schemas, and full diff
