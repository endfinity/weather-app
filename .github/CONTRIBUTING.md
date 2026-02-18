# Contributing to ClearSky Weather

## Branch Strategy

- `main`  Production-ready code. Protected branch.
- `develop`  Integration branch for features.
- `feature/*`  New features (branch from `develop`).
- `fix/*`  Bug fixes (branch from `develop`).

## Setting Up Branch Protection

Go to **Settings > Branches > Add rule** on GitHub:

1. Branch name pattern: `main`
2. Enable:
   - Require a pull request before merging
   - Require status checks to pass (select: `Backend - Tests`, `Backend - Lint & Format`, `Backend - Security Audit`)
   - Require branches to be up to date before merging
   - Do not allow force pushes
3. Save changes

## Development Workflow

1. Fork / clone the repo
2. Create a feature branch: `git checkout -b feature/my-feature develop`
3. Make changes, commit with [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat: add new weather card`
   - `fix: correct temperature conversion`
   - `docs: update API documentation`
4. Push and open a PR to `develop`
5. Wait for CI to pass, then request review

## Local Setup

See the [README](../README.md) for full setup instructions.

## Code Standards

- **Backend**: ESLint + Prettier (run `npm run lint` and `npm run format:check`)
- **Android**: Kotlin coding conventions, ktlint
- **Commits**: Conventional Commits format
- **Tests**: All new features must include tests
