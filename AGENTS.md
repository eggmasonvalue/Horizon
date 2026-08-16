# Agent Guidelines

Universal entry point and operating standards for agents working on Perspective - Live.

## Guiding Principle

Only document what an agent **cannot quickly recover by reading the code**. Code is the source of truth for *what the code does*. Docs exist for *where things live* ([`context/MAP.md`](context/MAP.md)) and *why major tradeoffs were made* ([`context/DECISIONS.md`](context/DECISIONS.md)). Everything else rots, so do not write it.

## Hard Guardrails

- **Branch Protection**: Never commit directly to the default branch (`main`). Always create a scoped branch and open a pull request.
- **Scope Discipline**: Keep changes strictly scoped to the task. Avoid unsolicited refactors or unrelated formatting changes.
- **Verification First**: Verify code behavior, test suites, and linters before documenting claims or declaring completion.

## Read Routing

- **Layout & Data Flow**: Read [`context/MAP.md`](context/MAP.md) before modifying module boundaries, adding components, or changing data flow.
- **Architectural Tradeoffs**: Read [`context/DECISIONS.md`](context/DECISIONS.md) before altering recorded tradeoffs or making architectural decisions.
- **Code Standards**: Read [`context/CONVENTIONS.md`](context/CONVENTIONS.md) while writing code and before running verification checks.
- **Task Orchestration**: Run `todo list` at task start and `todo claim <id>` before editing orchestrated task items.

## Event-Based Write Triggers

- **`context/MAP.md`**: Update when files, packages, module structures, dependencies, or runtime data flows change.
- **`context/DECISIONS.md`**: Update only when a choice satisfies the Decision-Log Bar below.
- **`context/CONVENTIONS.md`**: Update when project-wide imperative conventions, patterns, or commands are introduced or revised.
- **`README.md`**: Update when user-facing features, installation, requirements, or instructions change.

## Decision-Log Bar

[`context/DECISIONS.md`](context/DECISIONS.md) is a curated Architecture Decision Record (ADR) file, not a worklog or implementation journal. Append a decision only when **all** of the following conditions are met:

1. The choice changes architecture, public behavior, data shape, dependency ownership, or an irreversible/expensive migration path.
2. A future agent is likely to choose a different plausible path without the recorded rationale.
3. The rejected alternative and its cost are non-obvious from code.
4. The decision will still matter after the current branch or task is merged.

Before appending, check whether an existing decision should be amended or marked superseded instead. When in doubt, do not append; keep task-local rationale in commit messages or PR descriptions.

### Explicit Exclusions

Do not append decisions for:

- Bug fixes, cleanup, or dead-code removal.
- File renames, package moves, or mechanical refactors.
- Tactical implementation choices contained within a single feature.
- Routine test or lint chores.
- Changelogs, release notes, or feature status checklists (git history is the changelog).
- Choices where the rejected alternative is merely the default absence of the feature.

## Conventions vs Decisions Boundary

- **Conventions** ([`context/CONVENTIONS.md`](context/CONVENTIONS.md)) are terse, actionable imperatives with **zero rationale**.
- **Decisions** ([`context/DECISIONS.md`](context/DECISIONS.md)) capture the context, choice, and tradeoffs behind architectural divergences.
- If a convention rule requires explanation or "because", move the underlying rationale to `DECISIONS.md` only if it crosses the Decision-Log Bar; otherwise keep the convention rule concise.

## Todos ↔ Decisions Rule

- Todos and issue trackers hold transient working context during active development.
- Before completing or closing a task, evaluate if any architectural tradeoff made during implementation crosses the Decision-Log Bar.
- Graduate durable tradeoffs to [`context/DECISIONS.md`](context/DECISIONS.md). Discard ephemeral implementation notes.

## Definition of Done

A task is complete only when:

1. Code passes all build, test, and lint verifications.
2. Durable documentation ([`context/MAP.md`](context/MAP.md), [`context/DECISIONS.md`](context/DECISIONS.md), [`context/CONVENTIONS.md`](context/CONVENTIONS.md), [`README.md`](README.md)) matches the actual code state.
3. Any architectural tradeoff crossing the Decision-Log Bar is recorded in [`context/DECISIONS.md`](context/DECISIONS.md).
