# Changelog

## Unreleased

- Added `/season get`, which shows the current season and the Minecraft days left until the next one.
- Flowers bloom in spring in a fraction of open columns and are removed in the other seasons; snow replaces them. Seasonal flowers drop the real flower when harvested, and real flowers are never removed. Configurable with `springFlowers`, `springFlowerChance` and `springFlowerBlocks`.
- Sleeping through the night advances the season by the skipped ticks. Snow and thaw end up as if the night had passed normally, with rain until it would have stopped on its own. Can be disabled with `sleepAdvancesSeason`.
- Seasons, snow and thaw pause while no players are online. Can be disabled with `pauseWithoutPlayers`.
- The season can be locked with `lockedSeason`, or follow the real-world month with `realTimeSeasons` (March is early spring, February late winter), rechecked once a minute.
- `subseasonLength` must be at least 12000 ticks and `snowScheduleLength` at least 1000 ticks.

## 0.0.2

- Added the Seasons API (`SeasonsAPI.getTemperature`) for season-adjusted temperatures.
- A new snow/thaw schedule now starts when the main season changes, also when it is set with `/season set`.

## 0.0.1

- Initial release: twelve subseasons with their own grass and foliage colors, independent seasons per dimension, gradual seasonal snow and ice with catch-up for unloaded chunks, snow and icicles beneath leaf canopies, autumn leaf piles, and the `/season set` command.
