# Seasons

- A year consists of four seasons—spring, summer, autumn, and winter—each divided into early, mid, and late subseasons.
- Each subseason lasts a configurable number of ticks, after which the cycle advances to the next subseason. After late winter, the cycle returns to early spring.

# Coloring

- Each subseason has separate 256×256 color maps for grass and foliage.
- Grass and foliage colors are selected from the active subseason's maps using the season-adjusted biome temperature and biome rainfall.
- A subseason change switches to the new color maps immediately and rebuilds all rendered chunks.

Season Snow Logic:
- Seasons apply to a configurable whitelist of dimensions, defaulting to the Overworld. Each whitelisted dimension keeps and advances its own season. A dimension's season advances only while that dimension is loaded; it pauses while unloaded, so seasons in different dimensions may drift apart. Dimensions outside the whitelist keep vanilla temperatures, colors and snow.
- Only care for global weather, local weather comes later.
- Snow behavior distinguishes winter from all other seasons. Winter lowers the temperature by 0.7. The final season- and altitude-adjusted temperature is clamped to the range -0.5 through 2.0.
- During global precipitation, snow accumulates where the season- and altitude-adjusted temperature is at or below 0.15. Snow placement follows Minecraft's normal placement rules.
- Existing snow thaws where the adjusted temperature is above 0.15. Where the temperature is at or below 0.15 without precipitation, the column remains unchanged, so snow does not thaw in permafrost.
- Snow accumulation and thawing place and remove physical snow-layer blocks rather than using a texture overlay.
- Surface water freezes and thaws with the same logic: where snow would accumulate, the topmost water freezes to ice (following Minecraft's normal freezing rules, applied to the whole surface rather than spreading from the shore), and where snow would thaw, surface ice melts the way Minecraft melts ice (into water, or into nothing in dimensions where water evaporates). In dimensions with seasons, Minecraft's random freezing is disabled, like its random snowfall.
- Optionally (configurable for performance, enabled by default), snow also accumulates on the ground beneath leaf canopies and water there freezes, and both thaw there under the same conditions.
- Snow and ice changes do not trigger block updates in neighbouring blocks, for performance and so that changes at chunk edges never load adjacent chunks. Melted ice is the exception: the resulting water is updated itself so it can flow.
- Thawing affects all snow layers and ice in the processed positions, including player-placed ones.
- Note: Temperature gets colder the higher a block is, this is accounted for. A column uses the temperature at its topmost surface for all its decisions, including snow and ice beneath leaf canopies.
- We divide each column into 3 states: Perma snow, perma thaw and normal, normal snows in winter and thaws in other seasons.
- Snow/Thaw is tracked by a pseudo random 256x256 block pattern. This pattern is repeated over the entire world.
- Whenever the global raining state starts or stops, a new randomized processing schedule is generated and reset to its first step.
- Each schedule spans a configurable maximum number of ticks. Every column appears four times, distributed pseudo-randomly across the schedule; several appearances may fall on the same tick.
- On each tick, the scheduled columns update their state in the global pattern and in the chunks currently included in Minecraft's active tick set.
- Other chunks catch up when they are loaded, populated, or re-enter the active tick set.
- Each dimension with seasons has its own season time, a tick counter that advances only while the dimension is loaded. All snow/thaw timestamps and chunk update times use it, so a dimension that was unloaded continues where it stopped without any catch-up.
- Each position in the repeating pattern tracks four season-time timestamps:
    - The latest precipitation tick in any season.
    - The latest precipitation tick during winter.
    - The latest thaw-processing tick in any season.
    - The latest thaw-processing tick outside winter.
- Each chunk stores the season time at which its snow state was last brought up to date.
- Newly generated chunks catch up with the global snow state after terrain population. Snow and ice created by world generation are not suppressed; the catch-up corrects them afterwards.
- When a chunk catches up, each column compares the chunk's stored update time directly with the relevant timestamps:
    - Permanent-snow columns use the latest precipitation timestamp from any season.
    - Permanent-thaw columns use the latest thaw-processing timestamp from any season.
    - Normal columns compare the latest winter precipitation with the latest non-winter thaw-processing timestamp; the newer event determines whether snow is added or removed.
    - A column is changed only when the relevant event happened after the chunk's stored update time.

# Distant Horizons

- Distant Horizons (DH) snow uses the same repeating 256×256 pattern and the same four snow/thaw timestamps as normal terrain.
- The server synchronizes the timestamp grids and the dimension's season time to the client when the player connects or changes dimensions. The client keeps them current as snow and thaw processing advances.
- When DH captures full-resolution terrain, a non-rendered synthetic marker is added above a surface where Minecraft permits snow placement. An existing snow layer serves as both the current snow state and proof that the surface is snowable, so it does not need a separate marker.
- DH render data retains the snow information needed for each rendered surface:
    - Whether the source data already contained a snow layer.
    - Whether a snow layer or synthetic marker identifies the surface as snowable.
    - Whether the column is permanent snow, permanent thaw, or normal.
    - The season time when the LOD data last reflected the full-resolution world.
- The client uploads the timestamp grids to repeating GPU textures. Each LOD surface uses its world position to sample the matching entry.
- The shader compares the LOD surface's last-update time with the relevant snow and thaw timestamps, using the same catch-up rules as a loaded chunk. Existing snow remains until a newer thaw event removes it, and eligible surfaces gain snow only after a newer applicable precipitation event.
- Snow is rendered as a visual covering on the affected LOD surface without modifying the stored DH terrain or rebuilding every visible LOD chunk.
- Because the timestamp pattern advances gradually, distant snow and thaw also advance gradually. Updating the timestamp textures makes the affected LOD surfaces change without maintaining per-chunk snow, thaw, or rebuild queues.
- Snow metadata must survive DH data conversion and reduction. Render data with snow-sensitive boundaries must not be merged in a way that loses those boundaries.

# With a local weather mod like SimpleClouds:

- We have a global snow logic - we can't snow just where clouds are.
- Frost accumulates randomly, just slower than when it snows
- Special effect to show "frost weather" is in effect
