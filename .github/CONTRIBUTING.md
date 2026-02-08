# Contributing

See [Build from source](../README.md#build) for setup.

KtLint enforces `.editorconfig` rules on every build. Fix issues with:

```bash
./gradlew ktlintFormat
```

## Commits

Follow conventional commits: `type(scope): description`

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Scopes: `ui`, `xposed`, `selectors`, `prefs`, `http`, `theme`, `build`

Examples:

```
feat(ui): add selector count badge
fix(xposed): prevent double injection
```
