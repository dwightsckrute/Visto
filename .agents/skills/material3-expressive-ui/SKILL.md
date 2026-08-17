---
name: material3-expressive-ui
description: Design or review Visto UI using Material 3 Expressive, including themes, navigation, pills, chips, toolbars, carousels, app bars, cards, adaptive layout, or Essentials-inspired styling. Trigger for any material visual change. Do not use for data-only behavior or backend work with no UI impact.
---

# Material 3 Expressive UI

Apply Visto's cinematic visual system while treating Essentials as inspiration and official Material/Android documentation as the API authority.

## When to use

- Visual changes to screens or reusable components.
- Floating navigation, toolbars, pills, filters, cards, app bars, carousels, color, shape, type, elevation, or adaptive layout.
- Evaluating a Material 3 Expressive API or dependency version.

## When not to use

- Pure persistence, networking, backup, worker, or business-rule changes.
- Animation-only review without a broader visual decision; add `compose-motion-review` when motion is involved.

## Procedure

1. Read `AGENTS.md`, `docs/DESIGN_SYSTEM.md`, and `docs/DESIGN_REFERENCE_ESSENTIALS.md` before editing.
2. Inspect existing `core/ui/theme`, `core/ui/components`, `feature/shared`, and a similar production component. Reuse before creating.
3. Read `gradle/libs.versions.toml` and `app/build.gradle.kts`. Confirm the resolved API/version when uncertain:

```bash
./gradlew :app:dependencyInsight --dependency androidx.compose.material3:material3 --configuration debugRuntimeClasspath
```

4. Consult the official Android, Material, Kotlin, or AndroidX source documentation for the exact component. Never infer an API name from a visual mockup.
5. If an API is experimental, alpha/beta, or needs a dependency, document current version, required version, stability, benefit, risk, and compatible alternative before changing dependencies.
6. Use semantic color pairs, shared typography/shapes/spacing/motion/elevation, and a minimum 48 dp target. Keep posters visually dominant.
7. Distinguish `NavigationBar`, `BottomAppBar`, `FloatingToolbar`, and FAB by function. A custom navigation component is internal, not an official Material API.
8. Treat Essentials choices as principles. Do not copy code, dimensions, resources, privileged features, blur, or Liquid Glass-like effects.
9. Review compact, medium, and expanded behavior as relevant; validate Spanish/English, light/dark, dynamic color, OLED, font scale, and gesture/IME insets.
10. Add previews for isolated important components and compile/lint in proportion to risk.

## Checklist

- [ ] Existing tokens/components were inspected first.
- [ ] Every custom container has an explicit readable semantic content color.
- [ ] Shape communicates function; pills are functional and remain rounded in all states.
- [ ] No nested-card, border, shadow, gradient, transparency, or animation excess was added.
- [ ] Navigation and action components use the correct semantics.
- [ ] The exact API exists in the installed version or a version proposal is documented.
- [ ] Light, dark, dynamic, OLED, long labels, font scale, and window sizes were considered.
- [ ] Touch targets, descriptions, selection semantics, contrast, and performance were reviewed.
- [ ] Essentials remains inspiration, not copied implementation.

## Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew lintDebug
./gradlew assembleDebug
git diff --check
```

Use `./gradlew test lint assembleRelease` for navigation/theme/release-risk changes. Report unavailable device or preview checks explicitly.

## Required references

- `docs/DESIGN_SYSTEM.md`
- `docs/DESIGN_REFERENCE_ESSENTIALS.md`
- `docs/ARCHITECTURE.md` when navigation or structure changes
- [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Material navigation bar](https://m3.material.io/components/navigation-bar/overview)
- [Material toolbars](https://m3.material.io/components/toolbars/overview)
- Official documentation or AndroidX source for the exact API used
