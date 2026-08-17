---
name: compose-motion-review
description: Create or review Jetpack Compose animation in Visto, including selection, container transforms, visibility, content changes, shared elements, predictive back, and animated navigation. Trigger whenever motion behavior or animation APIs change. Do not use for static styling with no motion.
---

# Compose Motion Review

Ensure motion has a clear purpose, uses compatible APIs, remains interruptible, and cannot produce invalid layout values or excessive recomposition.

## When to use

- Adding or changing `AnimatedContent`, `AnimatedVisibility`, `animate*AsState`, `updateTransition`, `Animatable`, spring, tween, shared elements, or predictive back.
- Reviewing navigation selection, shape/size changes, list item motion, or search transitions.

## When not to use

- Static color, typography, spacing, or shape changes.
- Loading indicators that only use an unchanged official Material component.

## Procedure

1. Read `AGENTS.md` and the Motion section of `docs/DESIGN_SYSTEM.md`. Read `docs/ARCHITECTURE.md` for navigation transitions.
2. State the purpose: selection, state change, continuity, insertion/removal, expansion, or confirmation. Remove motion with no communicative purpose.
3. Inspect similar project motion and `MaterialTheme.motionScheme`; reuse a shared spec before adding duration/easing literals.
4. Verify the exact animation API in the installed Compose/Material version and document any experimental opt-in.
5. Choose spring for physical, spatial, interruptible changes; choose tween for controlled temporal sequencing.
6. Coordinate related properties through one target state/transition. Avoid six unrelated animations for one selection.
7. Clamp spring-driven padding, size, alpha, progress, corner constraints, and other bounded inputs before passing them to rejecting APIs.
8. Check modifier order, stable lazy keys, animation labels, cancellation/interruption, and recomposition scope.
9. Confirm no manual timer/frame loop bypasses system animator-duration scale. Provide a low/no-motion fallback for custom canvas or nonstandard motion.
10. Test repeated rapid interaction, reversed direction, initial state, restoration, and disposal. Compile and run relevant tests.

## Checklist

- [ ] Motion explains a user-visible change.
- [ ] Spec is shared or intentionally justified.
- [ ] Related properties are coordinated by one transition state.
- [ ] Animation can reverse or be interrupted safely.
- [ ] Overshoot cannot reach an invalid API input.
- [ ] Modifier order and lazy keys are correct.
- [ ] No infinite decorative motion or permanent costly blur exists.
- [ ] System duration scale and reduced/no-motion behavior are respected.
- [ ] Recomposition and allocation scope are bounded.
- [ ] Light/dark, font scale, insets, and rapid repeated interaction were checked where relevant.

## Commands

```bash
./gradlew :app:compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew lintDebug
git diff --check
```

Use `./gradlew assembleDebug` for UI integration and `./gradlew test lint assembleRelease` when navigation or release behavior changes.

## Required references

- `docs/DESIGN_SYSTEM.md`
- `docs/ARCHITECTURE.md` for navigation
- [Compose animation](https://developer.android.com/develop/ui/compose/animation/introduction)
- [Shared elements](https://developer.android.com/develop/ui/compose/animation/shared-elements)
- [Predictive back](https://developer.android.com/develop/ui/compose/system/predictive-back-setup)
- Official API reference/source for every experimental animation API used
