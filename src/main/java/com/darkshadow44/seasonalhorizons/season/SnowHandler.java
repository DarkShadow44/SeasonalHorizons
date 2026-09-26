package com.darkshadow44.seasonalhorizons.season;

import java.util.Arrays;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.Config;
import com.darkshadow44.seasonalhorizons.block.BlockLeafPile;
import com.darkshadow44.seasonalhorizons.block.ModBlocks;
import com.darkshadow44.seasonalhorizons.network.NetworkHandler;
import com.darkshadow44.seasonalhorizons.save.IMixinChunk;
import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class SnowHandler {

    private static final int MAX_BLOCK_REPEAT = 4;

    /**
     * 256x256 entries * MAX_BLOCK_REPEAT
     * Format is (chunkIndex << 8) | blockPos
     * Ordered by the tick they are due, ordered by chunk index within a tick
     */
    private final int[] scheduleEntries = new int[256 * 256 * MAX_BLOCK_REPEAT];

    /**
     * scheduleLength + 1 entries
     * Index into scheduleEntries for each tick
     * Each tick has scheduleTickStart[i + 1] - scheduleTickStart[i] entries
     */
    private int[] scheduleTickStart;

    /**
     * 256 + 1 entries
     * Index into scheduleEntries for each chunk in current tick
     * Each chunk has scheduleChunkStart[i + 1] - scheduleChunkStart[i] entries
     */
    private final int[] scheduleChunkStart = new int[256 + 1];

    private final WeakHashMap<Chunk, BiomeGenBase[]> chunkBiomeCache = new WeakHashMap<>();

    private final World world;

    // World seed folded to 32 bits for the icicle column hash
    private final int icicleSeed;
    // Derived from icicleSeed so pile columns differ from icicle columns
    private final int leafPileSeed;

    // Saved season data
    SeasonWorldData seasonWorldData;

    public SnowHandler(World world, SeasonWorldData seasonWorldData) {
        this.world = world;
        this.seasonWorldData = seasonWorldData;
        long seed = world.getSeed();
        this.icicleSeed = mix((int) (seed ^ (seed >>> 32)));
        this.leafPileSeed = mix(icicleSeed ^ 0x5BD1E995);

        if (!seasonWorldData.scheduleInitialized) {
            seasonWorldData.scheduleInitialized = true;
            seasonWorldData.scheduleRaining = world.isRaining();
            seasonWorldData.scheduleSeed = createScheduleSeed(world.getTotalWorldTime(), world.isRaining());
            // -1 is safe: handleSnowServerGlobal runs before handleSnowServerTick each tick and advances it first
            seasonWorldData.schedulePos = -1;
            seasonWorldData.markDirty();
        } else if (seasonWorldData.schedulePos < 0 || seasonWorldData.schedulePos >= Config.getSnowScheduleLength()) {
            seasonWorldData.schedulePos = -1;
            seasonWorldData.markDirty();
        }

        generateBlockSchedules(seasonWorldData.scheduleSeed);
    }

    private long createScheduleSeed(long tick, boolean raining) {
        long weatherSalt = raining ? 0x9E3779B97F4A7C15L : 0xC2B2AE3D27D4EB4FL;
        return new Random(world.getSeed() ^ tick ^ weatherSalt).nextLong();
    }

    private void generateBlockSchedules(long seed) {
        int scheduleLength = Config.getSnowScheduleLength();
        Random random = new Random(seed);
        if (scheduleTickStart == null || scheduleTickStart.length != scheduleLength + 1) {
            scheduleTickStart = new int[scheduleLength + 1];
        } else {
            Arrays.fill(scheduleTickStart, 0);
        }

        // Drawn up front so both passes use the same seed per chunk
        int[] chunkSeeds = new int[256];
        for (int chunk = 0; chunk < 256; chunk++) {
            chunkSeeds[chunk] = random.nextInt();
        }

        // Count the entries of each tick one index ahead, so the prefix sum yields the start indices
        for (int chunk = 0; chunk < 256; chunk++) {
            for (int i = 0; i < 256; i++) {
                for (int repeat = 0; repeat < MAX_BLOCK_REPEAT; repeat++) {
                    int tick = getScheduleTick(chunkSeeds[chunk], i, repeat, scheduleLength);
                    scheduleTickStart[tick + 1]++;
                }
            }
        }
        // Finalize scheduleTickStart
        for (int tick = 0; tick < scheduleLength; tick++) {
            scheduleTickStart[tick + 1] += scheduleTickStart[tick];
        }

        // Chunk outermost keeps the entries of each tick ordered by chunk index
        int[] nextEntry = Arrays.copyOf(scheduleTickStart, scheduleLength);
        for (int chunk = 0; chunk < 256; chunk++) {
            for (int i = 0; i < 256; i++) {
                for (int repeat = 0; repeat < MAX_BLOCK_REPEAT; repeat++) {
                    int tick = getScheduleTick(chunkSeeds[chunk], i, repeat, scheduleLength);
                    scheduleEntries[nextEntry[tick]++] = (chunk << 8) | i;
                }
            }
        }
    }

    private static int mix(int x) {
        x ^= x >>> 16;
        x *= 0x85ebca6b;
        x ^= x >>> 13;
        x *= 0xc2b2ae35;
        x ^= x >>> 16;
        return x;
    }

    // The tick at which the block is processed for the given repeat
    private static int getScheduleTick(int chunkSeed, int blockPos, int repeat, int scheduleLength) {
        long hash = mix(chunkSeed ^ mix(blockPos) ^ mix(repeat));
        int tick = (int) (hash % scheduleLength);
        if (tick < 0) tick += scheduleLength;
        return tick;
    }

    private int getBlockScheduleIndex(int chunkX, int chunkZ) {
        chunkX = chunkX % 16;
        if (chunkX < 0) {
            chunkX += 16;
        }
        chunkZ = chunkZ % 16;
        if (chunkZ < 0) {
            chunkZ += 16;
        }
        return (chunkX << 4) + chunkZ;
    }

    // Vanilla canSnowAtBody without the temperature check; callers already decided from the timestamps,
    // and the vanilla check would use the current season, which is wrong when a chunk catches up
    private void processBlockPlaceSnow(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= 256 || chunk.getSavedLightValue(EnumSkyBlock.Block, x & 0xf, y, z & 0xf) >= 10) {
            return;
        }
        // Snow replaces leaf piles
        Block block = chunk.getBlock(x & 0xf, y, z & 0xf);
        if ((block.getMaterial() == Material.air || block instanceof BlockLeafPile)
            && Blocks.snow_layer.canPlaceBlockAt(world, x, y, z)) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, Blocks.snow_layer, 0);
            world.markBlockForUpdate(x, y, z);
        }
    }

    private void processBlockRemoveSnow(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x & 0xf, y, z & 0xf) == Blocks.snow_layer) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, Blocks.air, 0);
            world.markBlockForUpdate(x, y, z);
        }
    }

    // Vanilla canBlockFreezeBody without the temperature check (see processBlockPlaceSnow), and with
    // byWater=false so the water freezes independent of neighbors
    private void processBlockPlaceIce(Chunk chunk, int x, int y, int z) {
        if (y < 0 || y >= 256 || chunk.getSavedLightValue(EnumSkyBlock.Block, x & 0xf, y, z & 0xf) >= 10) {
            return;
        }
        Block block = chunk.getBlock(x & 0xf, y, z & 0xf);
        if ((block == Blocks.water || block == Blocks.flowing_water)
            && chunk.getBlockMetadata(x & 0xf, y, z & 0xf) == 0) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, Blocks.ice, 0);
            world.markBlockForUpdate(x, y, z);
        }
    }

    private void processBlockRemoveIce(Chunk chunk, int x, int y, int z) {
        int relX = x & 0xf;
        int relZ = z & 0xf;
        if (y <= 0 || chunk.getBlock(relX, y, relZ) != Blocks.ice) {
            return;
        }
        // Don't melt floating ice; liquid counts so frozen deep water still melts
        Material below = chunk.getBlock(relX, y - 1, relZ)
            .getMaterial();
        if (below.blocksMovement() || below.isLiquid()) {
            // Same result as vanilla ice melting from light
            Blocks.ice.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            Block melted = world.provider.isHellWorld ? Blocks.air : Blocks.water;
            chunk.func_150807_a(relX, y, relZ, melted, 0);
            world.markBlockForUpdate(x, y, z);
            // Update only the water itself so it starts flowing; notifying neighbours could load adjacent chunks
            world.notifyBlockOfNeighborChange(x, y, z, Blocks.ice);
        }
    }

    // Fixed per column and world, so the same columns are selected every year
    private static boolean isSelectedColumn(int seed, int x, int z, float chance) {
        // Top 24 bits of the hash as a uniform value in [0, 1)
        float value = (mix(seed ^ mix(x * 0x9E3779B9 ^ mix(z))) >>> 8) / (float) (1 << 24);
        return value < chance;
    }

    // Hangs from leaves directly above, in air not lit by block light of 10 or more (the same limit as snow)
    private void processBlockPlaceIcicle(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x & 0xf, y, z & 0xf)
            .isAir(world, x, y, z) && chunk.getSavedLightValue(EnumSkyBlock.Block, x & 0xf, y, z & 0xf) < 10
            && chunk.getBlock(x & 0xf, y + 1, z & 0xf)
                .isLeaves(world, x, y + 1, z)) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, ModBlocks.icicle, 0);
            world.markBlockForUpdate(x, y, z);
        }
    }

    private void processBlockRemoveIcicle(Chunk chunk, int x, int y, int z) {
        if (chunk.getBlock(x & 0xf, y, z & 0xf) == ModBlocks.icicle) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, Blocks.air, 0);
            world.markBlockForUpdate(x, y, z);
        }
    }

    // Blocks the canopy walk passes through on its way down to the ground
    private boolean isCanopyPassable(Block block, int x, int y, int z) {
        return block == ModBlocks.icicle || block instanceof BlockLeafPile
            || block.isLeaves(world, x, y, z)
            || block.isAir(world, x, y, z);
    }

    // Walks down from the surface through the canopy, growing icicles in air directly below leaves in icicle
    // columns, then freezes water and places snow on the ground; snow replaces leaf piles
    private void processCanopySnow(Chunk chunk, int x, int y, int z) {
        boolean icicles = Config.isIcicles() && isSelectedColumn(icicleSeed, x, z, Config.getIcicleChance());
        while (y > 0) {
            y--;
            Block block = chunk.getBlock(x & 0xf, y, z & 0xf);
            if (icicles) {
                processBlockPlaceIcicle(chunk, x, y, z);
            }
            if (!isCanopyPassable(block, x, y, z)) {
                break;
            }
        }

        if (Config.isSnowUnderCanopies()) {
            processBlockPlaceIce(chunk, x, y, z);
            processBlockPlaceSnow(chunk, x, y + 1, z);
        }
    }

    // Walks down from the surface through the canopy, removing snow and icicles on the way, and leaf piles outside
    // autumn, then melts ice on the ground. In autumn, places a pile of the lowest leaves on the ground
    private void processCanopyThaw(Chunk chunk, int x, int y, int z, boolean autumn) {
        Block lowestLeaves = null;
        int lowestLeavesMeta = 0;
        while (y > 0) {
            y--;
            Block block = chunk.getBlock(x & 0xf, y, z & 0xf);
            if (block.isLeaves(world, x, y, z)) {
                lowestLeaves = block;
                lowestLeavesMeta = chunk.getBlockMetadata(x & 0xf, y, z & 0xf);
            }
            processBlockRemoveSnow(chunk, x, y, z);
            processBlockRemoveIcicle(chunk, x, y, z);
            if (!autumn && block instanceof BlockLeafPile) {
                chunk.func_150807_a(x & 0xf, y, z & 0xf, Blocks.air, 0);
                world.markBlockForUpdate(x, y, z);
            }
            if (!isCanopyPassable(block, x, y, z)) {
                break;
            }
        }

        processBlockRemoveIce(chunk, x, y, z);
        if (autumn && Config.isLeafPiles()) {
            processBlockPlaceLeafPile(chunk, x, y + 1, z, lowestLeaves, lowestLeavesMeta);
        }
    }

    // Like vanilla, use the precipitation height: the light height map passes through glass and similar blocks.
    // Empty columns report -1, clamp so block lookups stay in range
    private static int getSurfaceHeight(Chunk chunk, int relX, int relZ) {
        return Math.max(chunk.getPrecipitationHeight(relX, relZ), 0);
    }

    // Places a pile of the lowest canopy leaves on the ground, in air only
    private void processBlockPlaceLeafPile(Chunk chunk, int x, int y, int z, Block leaves, int leavesMeta) {
        if (leaves == null || !isSelectedColumn(leafPileSeed, x, z, Config.getLeafPileChance())) {
            return;
        }
        BlockLeafPile pile = ModBlocks.getLeafPile(leaves, leavesMeta);
        if (pile != null && chunk.getBlock(x & 0xf, y, z & 0xf)
            .isAir(world, x, y, z) && pile.canPlaceBlockAt(world, x, y, z)) {
            chunk.func_150807_a(x & 0xf, y, z & 0xf, pile, leavesMeta);
            world.markBlockForUpdate(x, y, z);
        }
    }

    // Writes go straight to the chunk for speed and so no neighbour updates load adjacent chunks.
    // Leaf piles are handled on thaw only: placed in autumn, removed otherwise; snow replaces them.
    // autumn is the season of the thaw processing being applied and is ignored when snowing
    private void processColumn(Chunk chunk, int x, int z, boolean snow, boolean autumn) {
        int y = getSurfaceHeight(chunk, x & 0xf, z & 0xf);
        if (snow) {
            processBlockPlaceIce(chunk, x, y - 1, z);
            processBlockPlaceSnow(chunk, x, y, z);
            if (Config.isWalkCanopies()) {
                processCanopySnow(chunk, x, y, z);
            }
        } else {
            processBlockRemoveSnow(chunk, x, y, z);
            processBlockRemoveIce(chunk, x, y - 1, z);
            if (Config.isWalkCanopies()) {
                processCanopyThaw(chunk, x, y, z, autumn);
            }
        }
    }

    // Indexed by (x & 15) << 4 | (z & 15)
    private BiomeGenBase[] getChunkBiomes(Chunk chunk) {
        return chunkBiomeCache.computeIfAbsent(chunk, (dummy) -> {
            BiomeGenBase[] ret = new BiomeGenBase[256];
            for (int i = 0; i < 256; i++) {
                ret[i] = chunk.worldObj
                    .getBiomeGenForCoords((chunk.xPosition << 4) + (i >> 4), (chunk.zPosition << 4) + (i & 0xf));
            }
            return ret;
        });
    }

    public void processChunkPartial(Chunk chunk, long lastUpdateTime, int minX, int maxX, int minZ, int maxZ) {
        int chunkIndex = getBlockScheduleIndex(chunk.xPosition, chunk.zPosition);
        chunkIndex = chunkIndex << 8;

        long[] lastSnowTicksWinter = seasonWorldData.lastSnowTicksWinter;
        long[] lastSnowTicksAny = seasonWorldData.lastSnowTicksAny;
        long[] lastThawTicksSummer = seasonWorldData.lastThawTicksSummer;
        long[] lastThawTicksAny = seasonWorldData.lastThawTicksAny;
        BiomeGenBase[] biomes = getChunkBiomes(chunk);

        for (int currentX = minX; currentX < maxX; currentX++) {
            for (int currentZ = minZ; currentZ < maxZ; currentZ++) {
                int index = chunkIndex + (currentX << 4) + currentZ;
                int x = (chunk.xPosition << 4) + currentX;
                int z = (chunk.zPosition << 4) + currentZ;
                BiomeGenBase biome = biomes[(currentX << 4) + currentZ];
                int y = getSurfaceHeight(chunk, currentX, currentZ);
                boolean isPermaSnow = Season.SUMMER_MID.getAdjustedTemperatureFloat(biome, x, y, z) <= 0.15F;
                boolean isPermaThaw = Season.WINTER_MID.getAdjustedTemperatureFloat(biome, x, y, z) > 0.15F;
                boolean lastOutsideWinterThawWasAutumn = seasonWorldData.lastThawSummerWasAutumn[index] != 0;

                if (isPermaThaw) {
                    if (lastThawTicksAny[index] > lastUpdateTime) {
                        // The autumn flag describes lastThawTicksSummer. A newer winter thaw updates only
                        // lastThawTicksAny, so require both timestamps to match before applying that flag.
                        boolean autumn = lastThawTicksAny[index] == lastThawTicksSummer[index]
                            && lastOutsideWinterThawWasAutumn;
                        processColumn(chunk, x, z, false, autumn);
                    }
                    continue;
                }

                if (isPermaSnow) {
                    if (lastSnowTicksAny[index] > lastUpdateTime) {
                        processColumn(chunk, x, z, true, false);
                    }
                    continue;
                }

                // These columns never thaw in winter, so their latest thaw processing is the latest outside winter,
                // even if lastThawTicksAny has since been overwritten by winter ticks
                if (lastThawTicksSummer[index] > lastSnowTicksWinter[index]) {
                    if (lastThawTicksSummer[index] > lastUpdateTime) {
                        processColumn(chunk, x, z, false, lastOutsideWinterThawWasAutumn);
                    }
                } else {
                    if (lastSnowTicksWinter[index] > lastUpdateTime) {
                        processColumn(chunk, x, z, true, false);
                    }
                }
            }
        }
    }

    public void processChunk(Chunk chunk, long lastUpdateTime) {
        processChunkPartial(chunk, lastUpdateTime, 0, 16, 0, 16);
        ((IMixinChunk) chunk).seasonalHorizons$setLastUpdateTime(seasonWorldData.seasonTime);
    }

    /**
     * Catch up the area vanilla population places snow and ice in: 16x16 blocks offset by 8, spanning four chunks
     * that population guarantees to be loaded. The generated snow has no real event behind it, so the whole area
     * catches up from the beginning, including columns in already-populated neighbours.
     */
    public void processPopulatedArea(int chunkX, int chunkZ) {
        // The populated chunk only needs x/z 8-16 here, the rest is covered when its neighbours populate.
        // Process it fully anyway, in case a chunk generator places snow outside vanilla's area.
        processChunk(world.getChunkFromChunkCoords(chunkX, chunkZ), 0);
        processChunkPartial(world.getChunkFromChunkCoords(chunkX + 1, chunkZ), 0, 0, 8, 8, 16);
        processChunkPartial(world.getChunkFromChunkCoords(chunkX, chunkZ + 1), 0, 8, 16, 0, 8);
        processChunkPartial(world.getChunkFromChunkCoords(chunkX + 1, chunkZ + 1), 0, 0, 8, 0, 8);
    }

    public void handleSnowServerTick(Chunk chunk) {
        // Chunks can stay loaded outside the active set; catch up what they missed when they re-enter it
        IMixinChunk mixinChunk = (IMixinChunk) chunk;
        long tick = seasonWorldData.seasonTime;
        long lastUpdateTime = mixinChunk.seasonalHorizons$getLastUpdateTime();
        if (lastUpdateTime < tick - 1) {
            processChunk(chunk, lastUpdateTime);
        }

        int index = getBlockScheduleIndex(chunk.xPosition, chunk.zPosition);
        int start = scheduleChunkStart[index];
        int end = scheduleChunkStart[index + 1];
        // Most chunks have nothing scheduled on a given tick; skip the biome cache lookup for them
        BiomeGenBase[] biomes = start < end ? getChunkBiomes(chunk) : null;
        for (int i = start; i < end; i++) {
            int blockPos = scheduleEntries[i] & 0xff;
            int x = (chunk.xPosition << 4) + (blockPos >> 4);
            int z = (chunk.zPosition << 4) + (blockPos & 0xf);

            BiomeGenBase biome = biomes[blockPos];
            int y = getSurfaceHeight(chunk, blockPos >> 4, blockPos & 0xf);
            float temperature = seasonWorldData.season.getAdjustedTemperatureFloat(biome, x, y, z);
            boolean canSnow = temperature <= 0.15F;

            if (canSnow) {
                if (world.isRaining()) {
                    processColumn(chunk, x, z, true, false);
                }
            } else {
                processColumn(chunk, x, z, false, seasonWorldData.season.isAutumn());
            }
        }

        mixinChunk.seasonalHorizons$setLastUpdateTime(tick);
    }

    public void handleSnowServerGlobal() {
        seasonWorldData.seasonTime++;

        // Resolve a season boundary before updating either the global pattern or active chunks.
        if (seasonWorldData.seasonTicks >= Config.getSubseasonLength()) {
            seasonWorldData.seasonTicks = 0;
            seasonWorldData.changeSeason(seasonWorldData.season.nextSeason());
            NetworkHandler.sendSeasonUpdate(world);
        }

        boolean raining = world.isRaining();
        if (raining != seasonWorldData.scheduleRaining) {
            seasonWorldData.scheduleRaining = raining;
            seasonWorldData.scheduleSeed = createScheduleSeed(world.getTotalWorldTime(), raining);
            generateBlockSchedules(seasonWorldData.scheduleSeed);
            seasonWorldData.schedulePos = 0;
        } else {
            seasonWorldData.schedulePos++;
            if (seasonWorldData.schedulePos >= Config.getSnowScheduleLength()) {
                seasonWorldData.scheduleSeed = createScheduleSeed(world.getTotalWorldTime(), raining);
                generateBlockSchedules(seasonWorldData.scheduleSeed);
                seasonWorldData.schedulePos = 0;
            }
        }

        long tick = seasonWorldData.seasonTime;
        boolean winter = seasonWorldData.season.isWinter();
        boolean autumn = seasonWorldData.season.isAutumn();

        // Update the global pattern and record each chunk's range of this tick's entries for the active chunks
        int start = scheduleTickStart[seasonWorldData.schedulePos];
        int end = scheduleTickStart[seasonWorldData.schedulePos + 1];
        int chunk = 0;
        for (int i = start; i < end; i++) {
            // An entry is (chunkIndex << 8) | blockPos, which is also its index into the global pattern
            int pos = scheduleEntries[i];
            int entryChunk = pos >>> 8;
            // Entries are ordered by chunk index, so this chunk and any skipped chunks before it (no entries this tick)
            // start here
            while (chunk <= entryChunk) {
                scheduleChunkStart[chunk++] = i;
            }
            if (raining) {
                seasonWorldData.lastSnowTicksAny[pos] = tick;
                if (winter) {
                    seasonWorldData.lastSnowTicksWinter[pos] = tick;
                }
            }
            seasonWorldData.lastThawTicksAny[pos] = tick;
            if (!winter) {
                seasonWorldData.lastThawTicksSummer[pos] = tick;
                seasonWorldData.lastThawSummerWasAutumn[pos] = (byte) (autumn ? 1 : 0);
            }
        }
        // Fill in remaining chunks that don't get processed this tick
        while (chunk <= 256) {
            scheduleChunkStart[chunk++] = end;
        }

        seasonWorldData.seasonTicks++;
        seasonWorldData.markDirty();
    }
}
