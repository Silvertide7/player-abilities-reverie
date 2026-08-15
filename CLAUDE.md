# Player Abilities: Reverie

A Forge mod (Minecraft 1.20.1, Forge 47.4.10) implementing the Realms of Reverie ability set on top of the Player Abilities framework mod. Ships the 18 Reverie abilities (see `claude_reference/reverie_port_specs.md` in the player_abilities repo for exact specs) and serves as the public example of building against the Player Abilities API.

## Project info

- Mod id: `pa_reverie`
- Base package / group: `net.silvertide.pa_reverie`
- Main mod class: `src/main/java/net/silvertide/pa_reverie/PAReverie.java`
- Depends on `player_abilities` (required, declared in `src/main/resources/META-INF/mods.toml`). Pulled from CurseMaven via `player_abilities_version` (a CurseForge file id) in `gradle.properties`; bump that id to move to a new framework build.

## Conventions

- All mod id, name, version, and dependency ranges live in `gradle.properties`.
- Assets go under `src/main/resources/assets/pa_reverie/`.

## Deviations from the original Realms of Reverie spells

Everything is an exact port of the Realms of Reverie source (effects, entities, managers, scanners, recipe type, configs, particles, sounds, formulas), with only these deviations:

- No mana costs (Iron's Spellbooks pipeline) and no mana-regen attribute modifier on Restful Meditation (Iron's attribute). Everything else about the effects is identical, including Peaceful Reverie's +1 Luck.
- Iron's `getSpellPower(level, caster)` maps to `HarvestAbility.spellPower(caster, base, perLevel, level)`: the spell's base power scaled by the framework's `player_abilities:ability_power` attribute via `support/AbilityPower` (multiplier capped at 2.0).
- Iron's cast pipeline (MagicData/AdditionalCastData) maps to the framework's use pipeline (`AbilityAPI.setUseData`/`getUseData`, `onUseTick`, `onUseReleased`/`onUseComplete`).
- Cooldowns were rebalanced by the user after the port and no longer match the Realms of Reverie source. Current values, in minutes (per level where the ability scales): Canopy Leap 4/3/2, Caisson 5, Feast of Life 5, Hunter's Mark 5, Excavate 8/6/4, Conjure Food 10, Peaceful Reverie 10, Shepherd's Aura 10, Tremor Sense 10, Restful Meditation 15/12/10, Woodsong 15, Mend 30, Rain Dance 30, Verdant Cascade 30, Deepsight 5/8/15, Fathom's Eye 5/8/15, Escape Shaft 120/90/60. Deepsight and Fathom's Eye rise with level because their effect duration scales too; Transmute takes its cooldown from the matched recipe.
- Farmer's Delight and Quality Food are compileOnly + localRuntime at build time (ephemeral foods use FD's Nourishment effect; Feast of Life reads Quality Food quality). Both are optional at runtime: Farmer's Delight is declared `mandatory=false` and Quality Food is untracked in mods.toml; each is reached only through a `ModList.isLoaded`-gated bridge (`FarmersDelightCompat`/`QualityFoodCompat`) whose real dependency reference lives in a nested class touched only when the mod is present, so the pack runs standalone.

## 1.20.1 port notes

Ported from NeoForge 1.21.1. Places where 1.20.1 has no equivalent API, and what was done instead:

- Data components do not exist. Ephemeral food expiry is stored as an `ExpiresAtGameTime` long on the stack's NBT tag (`EphemeralFoodItem`).
- `MobEffect.applyEffectTick` returns `void` on 1.20.1, so it cannot signal "end this effect". Peaceful Reverie and Restful Meditation broke on movement via a `false` return; they now queue the removal with `MinecraftServer.tell(new TickTask(...))`, which drains after `tickChildren` and so lands outside `LivingEntity.tickEffects`'s iteration. Do not use `MinecraftServer.execute` here: on the server thread outside `doRunTask`, `scheduleExecutables()` is false and `execute` runs the task inline, which mutates `activeEffects` mid-iteration (vanilla catches the `ConcurrentModificationException` but the remaining effects silently lose that tick).
- The 1.21 `false` return also routed through the expiry path, firing `MobEffectEvent.Expired`; `removeEffect` fires `MobEffectEvent.Remove` instead, so the break path calls `onReverieEnded`/`onMeditationEnded` explicitly, guarded on `removeEffect`'s return so it cannot double-fire alongside natural expiry.
- `MobEffect.onEffectStarted` does not exist. `VisionEffect` seeds its start-duration map on the first `applyEffectTick` instead.
- `LiquidBlockContainer.canPlaceLiquid` has no player parameter. `DryAirBlock` returned `player != null` (letting a bucket fill it while blocking natural flow); it now returns `false` so Caisson's air pocket still resists water.
- Recipes predate codecs: `TransmuteRecipe.Serializer` hand-writes `fromJson`/`fromNetwork`/`toNetwork`, and the recipe carries its own id (no `RecipeHolder`). The `result` field now uses the 1.20.1 `{"item": ..., "count": ...}` form rather than 1.21's `{"id": ...}`.
- Networking uses a Forge `SimpleChannel` (`ReverieNetworking`) instead of payload registration; the 1.21 `ByteBufCodecs.list(max)` bounds are preserved as explicit decode-side range checks.
- Block tags live under `data/pa_reverie/tags/blocks/` (plural), and `#c:ores` became `#forge:ores`.
- `Math.clamp` is Java 21; all uses are `Mth.clamp`.
- Attribute modifiers are UUID-keyed, so Peaceful Reverie's +1 Luck uses a fixed UUID constant.

## Build & run

- `./gradlew build` — build the mod jar (output in `build/libs/`).
- `./gradlew runClient` / `./gradlew runServer` — launch a dev instance.

---

# Reusable Engineering Standards

The sections below are project-agnostic.

## Code Style

**Never write comments.** No inline `//` comments, no `/* */` blocks, no javadoc, no leading explanatory headers on methods or fields. Code must be self-documenting through naming alone.

- Variable names describe what the value *is* (e.g. `armorCoveragePercent`, not `acp` with a comment).
- Method names describe what they *do* and under what conditions (e.g. `applyMultiplierIfAttackerIsPlayer`, not `applyBonus` with a comment explaining the player check).
- Extract a well-named helper method instead of writing a comment to explain a block.
- Constants get descriptive names that encode their meaning and unit (e.g. `KNIGHTMETAL_BONUS_DAMAGE_AT_FULL_ARMOR`, not `MAX` with a `// 2.0 vs fully-armored target` comment).
- If a name would need a comment to explain it, rename it until it doesn't.

Existing files may still contain comments and javadoc — leave them in place when editing unrelated code, but do not add new ones and prefer to delete obsolete ones when touching the surrounding code.

**Never leave dead code.** No unused methods, fields, classes, parameters, or imports. No "escape hatch" or "just in case" code. No commented-out blocks. If it's not called, delete it — the git history is the archive.

## Code Review

When asked to review code, do a "pass", check for issues, or otherwise audit a recent change, do **two** passes in order:

1. **Self-audit first.** Read the diff yourself. Fix the obvious — dead code, comments, naming, anything that violates the Code Style rules above. Report findings.
2. **Then spawn an independent reviewer** via the `/code-review` skill or a fresh agent. Give it only the diff and the goal, no context about why you made the choices you did. That catches the bugs you would otherwise rationalize away.
3. **Write to audit.md** Write the findings from the audit to audit.md in the claude_reference folder so we can checkmark them as we complete them.
4. **Project Problems check** by running a whole project search in the problems / project tab. Have the user download and put this file into the claude_reference/problems to check. Ask before deploys if we should do this.
   Don't skip step 2 because step 1 looked clean — the value of the independent reviewer is exactly that it doesn't share your blind spots.

## Version Control

**The user handles commits in git.** Never run `git add`, `git commit`, or `git push` — and don't suggest doing so — unless the user explicitly asks. Wrap up work by reporting what changed; staging and pushing are the user's job.
