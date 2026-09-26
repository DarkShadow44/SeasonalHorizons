# TODO

## General

- Future: gradual grass and foliage color transitions between subseasons instead of changing immediately, possibly via Angelica integration.
- Future: decide whether cached live-chunk biomes should refresh when a chunk's biome changes (world editors, biome-changing mods); they are currently cached until the chunk object is dropped.
- Deferred (needs a concept): decide how precipitation interacts with biomes that normally have no rain (e.g. deserts); some seasons might bring rain there. Seasonal snow currently uses world-wide rain and does not check per-biome precipitation.
- Biomes with custom color logic (swamp, mesa, roofed forest, BOP biomes) don't fire Forge's color events, so they keep their own colors but get no seasonal effect. Open idea: redirect the biome color calls in block `colorMultiplier`s and apply a multiplicative tint (`original × seasonal / vanilla colormap`, per channel) — identical to today for normal biomes; modded blocks need their own targets.
- Icicles: the texture is a placeholder.
- Future: falling leaf particles in autumn, coloured from the foliage color map.
- Leaf litter piles (autumn only, see Concept.md), open decisions:
    - Modded leaves in the list: pile blocks are registered in preInit, when other mods' leaves may not exist yet.
    - Modded leaves may not use vanilla's metadata layout (type in `meta & 3`, decay flags in bits 4 and 8), may use tile entities, or may read the world in `getIcon`/`colorMultiplier`, which piles delegate to at their own position.

## Distant Horizons

- Reimplement the Distant Horizons snow integration as optional Seasonal Horizons mixins instead of maintaining a directly edited DH build.
- Make the DH integration load only for supported DH versions and remain optional when DH is absent.
- Carry snow state, snow eligibility, climate category, pattern coordinates, and LOD last-update time through DH data conversion, reduction, quad building, and vertex generation.
- Add a non-rendered synthetic block above snowable surfaces while DH captures full-resolution terrain, using Minecraft's normal snow-placement rules. Treat an existing snow layer as inherently snowable.
- Replace the prototype's topmost-rendered-surface approximation with snow eligibility derived from the synthetic marker or an existing snow layer.
- Store the real last-update time in DH render data; the prototype patch currently writes zero into every vertex.
- Synchronize the four snow/thaw timestamp grids to clients and upload updates to the DH shader on login, dimension change, and subsequent snow/thaw progress.
- Synchronize the dimension's season time to clients alongside the timestamp grids and keep it in step, so LOD last-update times are recorded in season time rather than world time.
- Compare complete 64-bit timestamps in the DH shader; the prototype patch carries high and low halves but compares only the low halves.
- Add DH snow metadata without replacing vertex attributes required by DH or Iris.
- Ensure snow-sensitive DH surfaces are not merged in ways that discard snow boundaries.
- Limit the DH snow covering to the intended surface faces and integrate it with DH lighting and shading.
