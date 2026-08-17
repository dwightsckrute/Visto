---
name: compose-screen
description: Create or modify a Visto Jetpack Compose screen, route, screen-level state, or ViewModel-connected UI. Trigger for new screens and substantial edits to existing feature screens. Do not use for a standalone theme token, non-UI data change, or a tiny isolated component that does not affect screen behavior.
---

# Compose Screen

Build screens that fit Visto's feature structure, unidirectional state flow, localization, design system, and verification rules.

## When to use

- Creating a route or screen under `feature/`.
- Changing screen state, loading/content/error behavior, navigation callbacks, or ViewModel collection.
- Extracting a large screen into stateless content and reusable components.

## When not to use

- Repository, DAO, migration, worker, or network-only work.
- A theme-token-only change; use `material3-expressive-ui` when visual review is the focus.
- Animation-only review; use `compose-motion-review` as well.

## Procedure

1. Read `AGENTS.md` and `docs/ARCHITECTURE.md`. For any visual change, also read `docs/DESIGN_SYSTEM.md`.
2. Inspect the feature's screen, ViewModel, repository, routes, shared UI, and one similar screen before editing.
3. Identify data ownership, durable state, local UI state, effects, navigation, localization, and backup/schema implications.
4. Prefer this boundary when practical:
   - Route obtains ViewModels, uses `collectAsStateWithLifecycle()`, and connects navigation.
   - Screen receives immutable state and callbacks.
   - Reusable components receive only needed values, `modifier`, and events.
5. Represent asynchronous UI with explicit loading, empty, content, and error states where each is possible.
6. Keep repositories, DAO, Retrofit, WorkManager, filtering, mapping, and persistence outside composables.
7. Use lazy collections for unbounded content and stable collision-resistant keys.
8. Add isolated light/dark previews with sample data when the component can render without DI, navigation, database, or network.
9. Check Spanish and English, 320 dp width, font scale 1.3, 48 dp targets, semantics, insets, dynamic color, and OLED behavior.
10. Run the smallest useful verification, then checks proportional to risk.

## Checklist

- [ ] State flows down and events flow up.
- [ ] Durable state belongs to ViewModel/data; `rememberSaveable` is local UI state only.
- [ ] Effects have a real lifecycle reason and are not hiding ownership problems.
- [ ] New text has Spanish and English variants through the existing localization mechanism.
- [ ] Reusable UI does not receive a ViewModel or `NavController` when callbacks suffice.
- [ ] Lazy items have stable keys and parameters avoid needless instability.
- [ ] Loading, empty, content, and error paths are addressed.
- [ ] Accessibility, long labels, font scale, insets, theme modes, and previews were reviewed.
- [ ] No application ID, signing, Room, or backup invariant changed incidentally.

## Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
git diff --check
```

For release-risk work, use `./gradlew test lint assembleRelease`. Report blockers. This repository has no ktlint or Detekt tasks.

## Required references

- `AGENTS.md`
- `docs/ARCHITECTURE.md`
- `docs/DESIGN_SYSTEM.md` for visual changes
- Relevant route, screen, ViewModel, repository, navigation, and shared component files
