# TODO

## General

- Make the subseason duration configurable; it is currently hard-coded at 10,000 ticks.
- Decide whether grass and foliage colors should transition gradually between subseasons instead of changing immediately.
- Make the maximum snow/thaw schedule duration configurable; it is currently hard-coded at 1,000 ticks.
- Decide whether cached live-chunk biomes should refresh when a chunk's biome changes (world editors, biome-changing mods); they are currently cached until the chunk object is dropped.
- Decide how precipitation interacts with biomes that normally have no rain (e.g. deserts); some seasons might bring rain there. Seasonal snow currently uses world-wide rain and does not check per-biome precipitation.

## Distant Horizons

- Reimplement the Distant Horizons snow integration as optional Seasonal Horizons mixins instead of maintaining a directly edited DH build.
- Make the DH integration load only for supported DH versions and remain optional when DH is absent.
- Carry snow state, snow eligibility, climate category, pattern coordinates, and LOD last-update time through DH data conversion, reduction, quad building, and vertex generation.
- Add a non-rendered synthetic block above snowable surfaces while DH captures full-resolution terrain, using Minecraft's normal snow-placement rules. Treat an existing snow layer as inherently snowable.
- Replace the prototype's topmost-rendered-surface approximation with snow eligibility derived from the synthetic marker or an existing snow layer.
- Store the real last-update time in DH render data; the prototype patch currently writes zero into every vertex.
- Synchronize the four snow/thaw timestamp grids to clients and upload updates to the DH shader on login, dimension change, and subsequent snow/thaw progress.
- Compare complete 64-bit timestamps in the DH shader; the prototype patch carries high and low halves but compares only the low halves.
- Add DH snow metadata without replacing vertex attributes required by DH or Iris.
- Ensure snow-sensitive DH surfaces are not merged in ways that discard snow boundaries.
- Limit the DH snow covering to the intended surface faces and integrate it with DH lighting and shading.
