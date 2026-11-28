# Thaumaturge Research Gates

<img src="docs/logo.png" alt="Research Gates logo: an arcane stone gate barred shut, a crafting grid behind it, locked with a golden research seal" width="128">

A NeoForge 1.21.1 library that locks **any** recipe behind [Thaumaturge](https://modrinth.com/mod/thaumaturge) research.

Thaumaturge only locks its own recipes: the arcane workbench, the crucible and infusion carry `"research": {"entry", "stage", "negate"}`. The crafting table, furnaces and stonecutter are not locked. This library locks them the same way. [RecipeStages](https://github.com/jaredlll08/RecipeStages) was the model, but here the stages are Thaumaturge research. The check is Thaumaturge's own (`ResearchGate.passes`), so `entry`, `stage` and `negate` mean what they mean in Thaumaturge's recipes.

Requires Thaumaturge. Create is optional; with it, Create's machines obey gates too.

## Gates

A gate is a JSON file in a datapack at `data/<namespace>/research_gates/<name>.json`:

```json
{
  "research": {"entry": "thaumaturge:metallurgy", "stage": 1, "negate": false},
  "recipes": ["minecraft:iron_ingot_from_smelting_raw_iron"],
  "outputs": ["minecraft:iron_pickaxe", "#c:gears"],
  "recipe_types": ["minecraft:smithing_transform"],
  "namespaces": ["mymod"],
  "except": ["minecraft:charcoal"]
}
```

- `research`: what opens the recipes. `stage` and `negate` are optional.
- `recipes`: recipe ids.
- `outputs`: every recipe that makes one of these items or tags.
- `recipe_types`: every recipe of these types.
- `namespaces`: every recipe of these mods.
- `except`: recipes left free.

A recipe matching any of the lists is locked. A recipe under several gates needs all of them. Gates reload with `/reload`.

## Built-in datapack: Mechanoturgy

With Create installed, the library brings a datapack of its own, **Mechanoturgy**. It adds a Thaumonomicon chapter of 21 research entries, from the first look at a waterwheel to trains, and 20 gates that lock Create's recipes behind them. For example, a shaft needs the Kinetics research.

The datapack switches itself on in new worlds and in old worlds that never had it. It appears in the world's data pack list as "Mechanoturgy", and you can switch it off there or with a command:

```
/datapack disable "mod/researchgates:datapacks/mechanoturgy"
```

To change a gate or a research entry, put your own datapack above it with a file at the same path: `data/createthaumaresearch/research_gates/<name>.json` or `data/createthaumaresearch/thaumaturge/research_entry/<name>.json`. The ids are the same as in the Create: Thauma Research mod, which this datapack replaces, so research already done keeps counting. Remove that mod: it would add its own copy of the chapter, and its machine pollution, which the datapack does not have.

## Where gates apply

| Where | Whose research | What happens |
|---|---|---|
| Crafting table, inventory grid | the player | the result slot stays empty (also on the player's screen) |
| Stonecutter, smithing table | the player | the result slot stays empty |
| Furnace, smoker, blast furnace | the player who placed it | the recipe does not burn |
| Create: item application (casings) | the player | the use is refused with a message |
| Create: deployer | its owner | the recipe is not found |
| Create: mechanical crafter | the player who placed the crafter | the recipe is not allowed |

Items whose recipes are locked name the research in their tooltip.

Machines remember who placed them. While the owner is away, the answer from when they were last online is used. A machine nobody placed works unless `lock_unowned_machines` is on.

## Config

`config/researchgates-common.toml`:
- `lock_unowned_machines` (default `false`);
- `gate_furnaces` (default `true`);
- `tooltips` (default `true`).

## For other mods

```java
ResearchGates.mayUse(player, recipeHolder, output);             // a player at a station
ResearchGates.mayUse(level, ownerUuid, recipeHolder, output);   // a machine with an owner
ResearchGates.refusal(player, recipeHolder, output);            // the message to show, or null
Owners.ownerOf(blockEntity); Owners.setOwner(blockEntity, uuid);
```

Your mod can ship its own gates as data and needs no code for that. Declare `researchgates` as a dependency in your `neoforge.mods.toml`.

## Development

`./gradlew runCheck` starts a server with test gates and prints what a player without research gets from:
- the crafting grid;
- the stonecutter;
- furnaces (one owned by that player, one owned by nobody).

Add `-PnoCreate` to run it without Create. Thaumaturge is on no public maven: put its jar at `libs/thaumaturge.jar`.
