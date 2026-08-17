# AGENTS.md

## Project

Visto is a single-module Android application written in Kotlin and Jetpack Compose. It is a personal fork of WatchMaster for tracking movies, TV shows, seasons, episodes, ratings, notes, and custom lists. The UI supports Spanish and English.

The main source tree is `app/src/main/java/com/dwightsckrute/visto`:

- `core/`: dependency injection, preferences, networking, navigation, shared UI, localization, and theme.
- `data/`: Room database, entities, DAOs, mappers, and repositories.
- `feature/`: feature-oriented screens and ViewModels.

Permanent project references:

- Read `docs/ARCHITECTURE.md` before structural, navigation, persistence, dependency, or package changes.
- Read `docs/DESIGN_SYSTEM.md` and `docs/DESIGN_REFERENCE_ESSENTIALS.md` before visual, component, theme, or motion changes.
- Essentials is visual inspiration only. Do not copy its code, privileged features, architecture, strings, resources, dimensions, or dependency versions.

## Technology

- Kotlin and Java 17
- Jetpack Compose and Material 3 Expressive
- Coroutines, Flow, and StateFlow
- Room with KSP and explicit migrations
- Hilt
- Navigation Compose
- Retrofit and OkHttp
- Coil
- WorkManager
- MaterialKolor and Android dynamic colors
- Gradle Kotlin DSL and version catalogs
- Android SDK 36, minimum SDK 24

Do not introduce another framework, dependency, Gradle plugin, or code-generation tool without explaining why the existing stack cannot reasonably solve the problem.

Material 3 is pinned to an explicit `1.5.0-alpha` version in `gradle/libs.versions.toml` instead of being governed by the Compose BOM, because Visto depends on the Expressive APIs. Declare it once. MaterialKolor also lifts part of the Compose graph above the BOM; check `dependencyInsight` before changing Compose or Material 3 versions.

## Product invariants

- Never change the Android `applicationId` from `com.dwightsckrute.visto`. A different ID creates a separate application and makes the existing private data unavailable.
- The Kotlin package and the `applicationId` are both `com.dwightsckrute.visto`. The move off the upstream author's `com.pranshulgg.watchmaster` is done; do not rename either again as incidental cleanup.
- The Room file is `visto.db`. `VistoDatabase` renames a `watchmaster.db` left by older installs before opening it. Keep that step: opening Room under a name whose file does not exist creates an empty database and orphans the real one.
- Preserve the permanent release signing identity. Never regenerate, replace, print, or commit signing material.
- Never use destructive Room migration fallbacks. Any schema change must increment the database version, add an explicit migration in `VistoDatabase.kt`, and preserve existing user data.
- Preserve the export/import and automatic-backup formats unless a compatible versioned migration is provided.
- Keep the application usable in both Spanish and English. New user-facing text must provide both variants through `localized(es, en)` or the existing localization mechanism.
- TMDB requests and stored metadata refreshes must use `AppLanguage.tmdbLanguage()` where language is relevant.

## Secrets and local configuration

- The build expects a TMDB v3 API key in `local.properties` as `TMDB_API_KEY`. It does not expect the TMDB API read-access token.
- `local.properties`, `keystore.properties`, `*.jks`, `*.keystore`, passwords, API keys, and GitHub secrets must never be committed or printed in logs or responses.
- Do not replace a real local configuration file with an example file.
- GitHub Actions reconstructs private configuration from repository secrets. Keep secret names and release-signing behavior compatible with `.github/workflows/release.yml`.

## Architecture

- Organize new code by feature and reuse shared code from `core/` and `data/`.
- Use unidirectional data flow: immutable UI state flows down and user events flow up.
- Prefer an immutable `UiState` exposed as `StateFlow<UiState>` from ViewModels.
- Collect flows in Compose with `collectAsStateWithLifecycle()`.
- Composables must not access Room DAOs, repositories, Retrofit services, or WorkManager directly. Route through a ViewModel or an appropriate coordinator.
- Keep persistence, network, mapping, filtering, and business rules outside composables.
- Route/screen composables may connect navigation and ViewModels. Reusable visual composables should prefer state and callbacks instead of receiving a ViewModel.
- Prefer composition over inheritance.
- Do not add interfaces, wrappers, use cases, or abstractions used only once unless they materially simplify testing or behavior.
- Preserve existing public APIs unless the requested change requires modifying them.

Apply these conventions to touched code without turning a focused task into an unrelated architecture rewrite.

## Compose conventions

- Composable functions use PascalCase. Non-composable functions and properties use idiomatic Kotlin naming.
- Use `rememberSaveable` only for local UI state that should survive recreation. Persist durable state through the existing ViewModel/data layer.
- Use `LaunchedEffect` only for a real lifecycle-bound side effect, not to compensate for incorrectly owned state.
- Include appropriate loading, empty, content, and error states for asynchronous screens.
- Use lazy layouts for potentially large collections and provide stable, collision-resistant keys.
- Avoid unstable collection/object parameters and unnecessary recompositions in frequently updated UI.
- Add a `@Preview` for a reusable isolated visual component when it can be rendered without DI, navigation, network, or database setup.
- Icon-only actions require a meaningful `contentDescription`/`desc`, except purely decorative icons.
- Interactive targets must be at least 48 dp unless they are non-interactive decoration.
- Test layouts with long Spanish labels, English labels, increased font scale, and narrow screens. Do not allow button text to wrap into clipped or broken actions.

## Design system and color

- Use Material 3 components and existing components/tokens under `core/ui` before creating another implementation.
- Use semantic `MaterialTheme.colorScheme` roles. Pair every custom container with its matching content role, for example `primaryContainer` with `onPrimaryContainer`.
- Do not hardcode interface colors outside the theme. Fixed black/white is acceptable only for functional image scrims or content drawn over unpredictable artwork.
- Support light, dark, Android dynamic color, generated seed palettes, and the pure-black OLED option.
- Do not derive content color implicitly from a translucent or modified container color; specify a readable semantic content color explicitly.
- Use the shared tokens in `core/ui/theme`: `Spacing`, `Elevation`, `AppMotion`, `ShapeRadius`, `AppShapes`, and `MediaStatusColors`. Do not reintroduce a local literal when a token already carries that value.
- Use the spacing scale 4, 8, 12, 16, 24, and 32 dp (`Spacing.xs`…`Spacing.xxl`) by default. Other values require a component/layout reason.
- Do not add a token without at least one real consumer.
- Use pill shapes for filters, tags, statuses, compact segmented actions, and the floating navigation selection. If a control must remain a pill, explicitly keep normal, pressed, and selected shapes rounded; Material Expressive defaults may morph a selected control into a square.
- Maintain accessible contrast and do not communicate status using color alone.
- Preserve Visto's cinematic identity: posters lead, typography is editorial, metadata is secondary, and containers must not compete with artwork.
- Avoid cards inside cards, strong shadows, universal borders, decorative gradients, excessive transparency, and pills without a functional role.
- Evolve the existing `core/ui/theme` and `core/ui/components`; do not create a parallel `core/designsystem` tree.

## Navigation

- Use `NavigationBar` semantics for three to five persistent primary destinations, `BottomAppBar` for bottom actions, `FloatingToolbar` for contextual actions, and `FloatingActionButton` for the primary action.
- A custom floating navigation component may be named `AppFloatingNavigationBar`; never present invented names as official Material APIs.
- The compact navigation design is one outer floating pill with one smaller selected indicator. Do not wrap every inactive icon in its own pill.
- Preserve destination state and avoid rebuilding navigation state on every selection. Keep reusable visual components independent of `NavController` when callbacks suffice.
- Respect gesture/navigation-bar insets and provide an adaptive rail or suite proposal before stretching compact navigation across large windows.

## Motion

- Prefer Material motion tokens from `MaterialTheme.motionScheme` and standard Compose animation APIs.
- Motion must explain state, hierarchy, or interaction. Avoid decorative infinite animation and excessive simultaneous movement.
- Respect the platform animator-duration scale; do not implement animation with manual timers or frame loops.
- Keep press feedback short and spatial screen transitions deliberate. Use stable keys for animated lazy-list items.
- Never pass a spring-animated value directly to an API that rejects overshoot. Clamp animated padding, size, alpha, progress, and other constrained values to a valid range. Visto previously crashed because a spring produced negative padding during search motion.
- Check animation modifier order. In particular, place `animateContentSize()` before fixed size modifiers when it is intended to animate layout size.
- Coordinate related selection properties with one transition/state so indicator, icon, color, label, size, and shape feel like one motion.
- Prefer interruptible springs for physical/spatial changes and tweens for controlled temporal transitions. Avoid exaggerated bounce and permanent blur.
- Do not introduce shared elements or predictive-back customization across the whole app at once; validate one route pair with unique keys and a no-motion fallback first.

## Data, backup, and networking

- Room is the source of truth for the library. Avoid maintaining a competing mutable copy in UI code.
- Database entities and migrations live under `data/local`; repositories mediate data access for ViewModels.
- When changing Room entities, update the database version, add/register a migration, keep exported schemas coherent, and test upgrading existing data.
- Treat backup compatibility as a public API. New backup fields should be optional or versioned, and old supported backups must continue to import safely.
- Automatic backup uses WorkManager and a persisted Storage Access Framework URI. Preserve URI permissions, unique-work behavior, and retry semantics.
- TMDB calls belong in the network/repository layer. Keep people out of the general movie/TV search results unless a feature explicitly requests them.
- Do not log full API responses or user library/backup contents.

## Code quality

- Prefer clear, idiomatic Kotlin over clever code.
- Search for similar components, routes, repositories, and utilities before creating new ones.
- Reuse `NavRoutes` for navigation and avoid ad-hoc route strings.
- Remove unused imports and dead code in touched files.
- Do not suppress compiler or lint warnings without a comment explaining the specific reason.
- Keep changes scoped to the request and preserve unrelated work in a dirty worktree.
- Do not mass-format or mechanically rewrite unrelated files.

## Before editing

1. Read the relevant feature, ViewModel, repository, and shared UI/theme components.
2. Read `docs/DESIGN_SYSTEM.md` for visual work and `docs/ARCHITECTURE.md` for structural work.
3. Search for an existing implementation or token that can be reused.
4. Identify effects on stored data, backups, localization, themes, navigation, and release compatibility.
5. Explain any architectural, dependency, schema, permission, experimental-API, or public-API change.
6. Keep the implementation limited to the requested outcome.

Repository-local skills live in `.agents/skills/<name>/SKILL.md`. Read the matching file directly if your runtime does not load them automatically. Use them when their descriptions match:

- `compose-screen` for creating or modifying a Compose screen.
- `material3-expressive-ui` for visual, navigation, pill, toolbar, carousel, or Essentials-inspired work.
- `compose-motion-review` for creating or reviewing animation.
- `android-quality-review` before handing off relevant Android changes.

If Codex makes the same mistake twice, propose a specific `AGENTS.md` rule or correction that would prevent a third occurrence.

## Verification

Use the smallest useful check while iterating, then run checks proportional to risk.

- Fast Kotlin compile: `./gradlew :app:compileDebugKotlin`
- Unit tests: `./gradlew testDebugUnitTest`
- Android lint: `./gradlew lintDebug`
- Debug APK: `./gradlew assembleDebug`
- Full release-equivalent validation: `./gradlew test lint assembleRelease`
- Always run `git diff --check` before handoff.

This repository does not currently configure `ktlint` or `detekt`; do not claim to have run those tasks. If a command cannot run because the Android SDK, JDK 17, `TMDB_API_KEY`, signing files, device, or network is unavailable, report that clearly and run the strongest available alternative.

For UI changes, also inspect the affected screen in Spanish and English and in light, dark, dynamic-color, and OLED modes when a device or preview is available. For database or backup changes, verify upgrade/import behavior rather than checking only a fresh install.

A code task is complete only when the relevant build/tests pass, or the remaining verification blocker is explicitly reported.

## Releases and Git

- `origin` is the Visto fork and `upstream` is the original WatchMaster project. Do not push personal changes to `upstream`.
- The active development branch is `personal-es` unless the user explicitly chooses another branch.
- Before a release, increment both `versionCode` (monotonically) and `versionName` in `app/build.gradle.kts`.
- Release tags use `visto-X.Y.Z`. Pushing such a tag triggers `.github/workflows/release.yml`, which runs `test lint assembleRelease` and publishes the signed APK plus SHA-256 checksum.
- Do not reuse, move, or delete a published release tag. Create a new patch version for a release correction.
- Never force-push or rewrite published history unless the user explicitly requests it and understands the consequences.
- A locally signed release must use the same permanent certificate as previous Visto releases; otherwise Android will reject it as an update.
