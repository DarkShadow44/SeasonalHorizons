# Project Agent Instructions

## Project concept

- Read `Concept.md` before planning or implementing project changes.
- Treat `Concept.md` as the current source of truth for the intended seasons, coloring, snow/thaw, Distant Horizons (DH) behavior.
- Keep `Concept.md` focused on intended behavior. Do not include references to the current implementation or use implementation details to override the concept.
- Use `Todo.md` to track unresolved decisions and remaining implementation work; keep it updated as items are added, resolved, or completed.
- Do not invent values for unresolved placeholders such as `X`, or resolve explicit TODOs without user direction.
- If implementation details conflict with `Concept.md`, point out the conflict before changing the documented behavior.

## Workflow

- Do not stage files in git unless explicitly requested.
- Do not compile or build this Minecraft project unless explicitly requested.

## Codebase notes

- In this 1.7.10 setup, network message handlers run on the main thread; no need to schedule their work onto it.
