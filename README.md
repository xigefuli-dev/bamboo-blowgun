# Bamboo Blowgun / 竹制吹箭

Minecraft Java Edition **26.2**, Java **25**, Fabric Loader **0.19.5**, Fabric API **0.161.0+26.2**.

## Gameplay

- Two bamboo, vertically arranged: one bamboo blowgun.
- Two sticks, vertically arranged: one wooden dart. Both fit the 2x2 inventory crafting grid.
- Hold the blowgun in the main hand. Each left click immediately fires one dart; no charging.
- Holding left click automatically repeats fire at the server-controlled cadence and does not mine blocks. Clicking entities does not add a melee hit.
- Survival consumes one dart from inventory or offhand. Ordinary arrows are not ammunition.
- Creative follows vanilla ammunition behavior: no ammunition is required or consumed.
- Blowgun durability is 192 and loses one point per shot. Vanilla Quick Charge I-III reduces its server cooldown to 13/12/11 ticks; the 0.64-second firing sound speeds up with a small pitch rise at each level, keeping the complete clip aligned with the next shot.
- Compatible enchantments: Unbreaking, Mending, Quick Charge, and the blowgun-only Lightness I-III, Limping I-III, and Venom Tip I. Lightness reduces dart gravity to 0.12/0.09/0.06 (vanilla arrows: 0.05). Limping adds 5/10/15% slowness while venom stacks remain. Venom Tip converts ordinary darts into venom darts when fired.
- Dart initial speed: 1.8 blocks/tick; gravity: 0.15 blocks/tick squared (vanilla arrows: 0.05).
- Base arrow damage coefficient: 2; final impact damage after vanilla rounding is multiplied by 0.95. Default knockback strength is multiplied by 0.75.
- Darts embedded in blocks can be recovered in survival; creative darts cannot create survival ammunition.

## Install

Use the release JAR with a Fabric installation that matches Minecraft 26.2. Put the JAR in that installation's `mods` folder. For multiplayer, both the client and server need this mod and Fabric API.

### In-game commands

The creative inventory combat tab includes the blowgun and both dart types. With cheats enabled:

```
/give @s bamboo_blowgun:blowgun
/give @s bamboo_blowgun:dart 64
/give @s bamboo_blowgun:venom_dart 64
```

## Development

Set JAVA_HOME to a JDK 25 directory, then:

```
./gradlew build
./gradlew runClientGameTest
```

`build/libs/bamboo-blowgun-0.5.0.jar` is the installable mod. Do not install the sources jar.
Game tests are in a separate source set and are not packaged in the production mod.

## Assets

Version 0.2.0 uses solid cuboid JSON models with vanilla bamboo/wood block textures. The hollow tube is held at the mouth with a static raised-arm pose. The dart renderer follows projectile pitch/yaw instead of facing the camera. Original inventory PNGs were created with the built-in imagegen tool and remain in source as artwork references.
Final files: `src/main/resources/assets/bamboo_blowgun/textures/item/blowgun.png` and `dart.png`.
Prompts are recorded in `ARTWORK.md`.
Version 0.3.0 adds a fixed positional firing sound joining segments 2 then 3 without the intervening silence and a victim-position impact sound; see AUDIO.md for source segments.

## Venom darts (0.4.0)
- Shapeless batch recipe: put a stack of at least 16 ordinary darts in one crafting slot, a drinkable Poison potion in another, and a drinkable Harming potion in a third. Consumes exactly 16 darts and both potions, yields 16 venom darts and returns two glass bottles. Long/strong Poison and strong Harming are also accepted. Works in the 2x2 grid, crafting table, and automated Crafter.
- Offhand ammunition has priority; otherwise the first ordinary or venom dart stack in inventory is used. Creative can also select venom darts with an offhand stack without consuming it.
- Each successful venom hit adds one stack AFTER its impact damage. Existing stacks increase incoming external damage by 3% each; venom damage itself is not amplified. Ordinary darts never add stacks.
- Stages: 1-4 stacks = purple cloud / 0.5 HP per second; 5-11 stacks = magenta cloud / 1 HP per second; 12 stacks = red cloud / 4 HP per second.
- Below stage three, one stack decays every 80 game ticks (4 seconds); new hits do not reset that countdown. At stage three, stacks lock at exactly 12 and never increase or decay before death. External damage amplification remains +36%.
- Venom is independent entity state, not a vanilla potion effect. Drinking milk does not remove it. State and timers persist through saving/loading; death clears it. Unloaded entities do not tick.
- Real animated smoke surrounds the victim. First-person victims receive a light colored veil and stage/stack readout. Flight trails use dark purple falling droplets, and the solid dart model has a purple coating and bead on the tip, including its inventory icon.
- The supplied impact sound now belongs only to venom darts. Ordinary darts use normal arrow impact and victim hurt sounds. Firing remains the joined fixed sound, with a 0.7-second cooldown.

## Balance update (0.4.1)
Venom and ordinary darts retain identical base impact damage (3.8 HP at the controlled close-range velocity). Each venom stack adds 3% incoming external damage, capped at +36% with 12 locked stacks. Poison damage is 0.5 / 1 / 4 HP per second across the three stages. The permanent third stage and 0.7-second firing cadence are unchanged.
The earlier proposal to reduce direct impact damage was withdrawn before installation. Only the extra benefits of venom are adjusted, preserving its role as a potion-upgraded dart.

The poison tick uses vanilla magic damage: it bypasses armor and armor toughness, but protection enchantments and resistance still apply. Venom-stack amplification explicitly excludes the venom tick itself. Verification covers all three stages with 30 armor / 20 toughness and checks exact 0.5 / 1 / 4 HP loss, including the 12-stack +36% state.
Controlled 0.4.1 comparison: 100 HP golem ordinary 27 / venom 22 shots; 20 HP target ordinary 6 / venom 5, with identical basic impact damage.
