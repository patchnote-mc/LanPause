<!-- last updated: 2026-07-01 -- tracks the codebase; refresh (and bump the date) when the workflow/doc layout below changes. -->

# CLAUDE.md — instructions for Claude Code

This is the committed, pushable copy of the working instructions for this repo.
The detailed source of truth (keep it authoritative; this file just points
to it and states the essentials):

- **`.github/copilot-instructions.md`** — full working strategy.

## Stack

- Minecraft `26.2`, Fabric (Loader `0.19.3`, API `0.153.0+26.2`, Loom `1.17-SNAPSHOT`), Mojang mappings (never Yarn), Java 25. (Gradle wrapper `9.5.1` — loom 1.17 needs Gradle ≥9.5.)
- Decompiled sources vendored locally:
  - `mc_decompiled/sources/26.2/common_src/` + `mc_decompiled/sources/26.2/client_src/` (gitignored, regen via `mc_decompiled/setup.sh 26.2` — CFR-based, NOT `genSources`).
  - `fabric_decompiled/src/` (git submodule of fabric-api @ tag `0.153.0+26.2`, init via `fabric_decompiled/setup.sh`).

## How to work here (essentials)

1. **Implement first** from prior knowledge + the local caches (`*_decompiled/.index/`,
   `*_decompiled/.knowledge/`). Do NOT browse decompiled sources up front by default.
2. **Verify by compiling:** `./gradlew build`. No need to verify GUI/runtime —
   the user provides screenshots/output when needed.
3. **Only inspect the decompiled `*_src/` (MC) or `src/` (Fabric) if it does not
   compile** or behavior is genuinely unclear. Up-front browsing is allowed for
   niche APIs, but not preferred.
4. **Cache as you go:** when you DO consult sources, record just-enough verified
   facts (plain text, terse) so the research isn't repeated:
   - MC/Fabric: `*_decompiled/.index/{version}/{fully.qualified.ClassName}.txt`
     (flat, no package dirs; version `26.2` for MC, `0.153.0+26.2` for
     Fabric) and `*_decompiled/.knowledge/{topic}.txt`. See each dir's `_GUIDE.txt`.
5. **Match Mojang mappings exactly**; never use Yarn names or mix namespaces;
   don't invent APIs — verify against the decompilation if unsure.

## Keep docs fresh

Every doc that tracks the codebase (this file and
`.github/copilot-instructions.md`) carries a date. When you change code
a doc describes, update the doc and bump its date in the same pass.
Park features as `DISABLED` with a date instead of deleting their docs.
