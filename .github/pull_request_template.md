<!--
Thanks for the change. Please keep this PR small, focused and atomic.
Title format: `<type>: <short description>` (Conventional Commits).
Allowed types: feat, fix, chore, refactor, docs, test, build, ci, perf.
-->

## Context

<!-- Why is this change needed? Link the issue if applicable. -->

## What changed

<!-- Bullet list of the concrete changes. -->

-
-

## How it was tested

- [ ] Unit tests (`mvn test`)
- [ ] Integration tests (`mvn verify -Pintegration`)
- [ ] Manual check: describe …

## Quality checklist

- [ ] Conventional Commit title and squashable history
- [ ] `mvn verify` clean (Spotless, SpotBugs, JaCoCo, Enforcer)
- [ ] JaCoCo thresholds met for the impacted module(s)
- [ ] No PAN, CVV or secret material in code, tests, fixtures or logs
- [ ] Relevant module `README` updated if the public surface changed

## Screenshots / diagrams

<!-- Attach if the change affects architecture, UI or operator-facing docs. -->
