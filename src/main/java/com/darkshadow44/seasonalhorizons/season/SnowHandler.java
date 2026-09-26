package com.darkshadow44.seasonalhorizons.season;

import java.util.ArrayList;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.network.NetworkHandler;
import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class SnowHandler {

    private static final int MAX_SEASON_LENGTH = 10000;
    private static final int MAX_TICKS_FOR_CHUNK_UPDATE = 1000;
    private static final int MAX_BLOCK_REPEAT = 4;

    // Of format [(chunkX % 16) << 4 + (chunkY % 16))] [(blockX % 16) << 4 + (blockY % 16)] []
    private final int[][][] chunkSchedules = new int[256][MAX_TICKS_FOR_CHUNK_UPDATE][];

    private final WeakHashMap<Chunk, BiomeGenBase[]> chunkBiomeCache = new WeakHashMap<>();

    private final World world;

    // Saved season data
    SeasonWorldData seasonWorldData;

    public SnowHandler(World world, SeasonWorldData seasonWorldData) {
        this.world = world;
        this.seasonWorldData = seasonWorldData;

        if (!seasonWorldData.scheduleInitialized) {
            seasonWorldData.scheduleInitialized = true;
            seasonWorldData.scheduleRaining = world.isRaining();
            seasonWorldData.scheduleSeed = createScheduleSeed(world.getTotalWorldTime(), world.isRaining());
            // -1 is safe: handleSnowServerGlobal runs before handleSnowServerTick each tick and advances it first
            seasonWorldData.schedulePos = -1;
            seasonWorldData.markDirty();
        } else if (seasonWorldData.schedulePos < 0 || seasonWorldData.schedulePos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
            seasonWorldData.schedulePos = -1;
            seasonWorldData.markDirty();
        }

        generateBlockSchedules(seasonWorldData.scheduleSeed);
    }

    private long createScheduleSeed(long tick, boolean raining) {
        long weatherSalt = raining ? 0x9E3779B97F4A7C15L : 0xC2B2AE3D27D4EB4FL;
        return new Random(world.getSeed() ^ seasonWorldData.scheduleSeed ^ tick ^ weatherSalt).nextLong();
    }

    private void generateBlockSchedules(long seed) {
        Random random = new Random(seed);
        for (int chunk = 0; chunk < 256; chunk++) {
            int[][] schedule = generateBlockSchedule(random.nextInt());
            chunkSchedules[chunk] = schedule;
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

    /**
     * Get the block schedule. Length of the array is the maximum time (in ticks) where a chunk should be completely
     * processed. schedule[i] is the array of coordinates of the block (x * 16 + z) to process at tick i.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int[][] generateBlockSchedule(int seed) {
        ArrayList[] schedule = new ArrayList[MAX_TICKS_FOR_CHUNK_UPDATE];

        for (int repeat = 0; repeat < MAX_BLOCK_REPEAT; repeat++) {
            for (int i = 0; i < 256; i++) {
                long hash = mix(mix(seed ^ mix(i) ^ mix(repeat)));
                int slot = (int) (hash % MAX_TICKS_FOR_CHUNK_UPDATE);
                if (slot < 0) slot += MAX_TICKS_FOR_CHUNK_UPDATE;

                if (schedule[slot] == null) {
                    schedule[slot] = new ArrayList();
                }
                schedule[slot].add(i);
            }
        }

        int[][] ret = new int[MAX_TICKS_FOR_CHUNK_UPDATE][];

        for (int i = 0; i < MAX_TICKS_FOR_CHUNK_UPDATE; i++) {
            if (schedule[i] == null) {
                ret[i] = new int[0];
            } else {
                ret[i] = schedule[i].stream()
                    .mapToInt(x -> (int) x)
                    .toArray();
            }
        }
        return ret;
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

    // Writes go straight to the chunk for speed and so no neighbour updates load adjacent chunks
    private void processBlock(Chunk chunk, int x, int z, boolean snow) {
        int relX = x & 0xf;
        int relZ = z & 0xf;
        int y = chunk.getHeightValue(relX, relZ);
        if (snow) {
            // Freeze the whole surface at once so the result doesn't depend on processing order
            if (world.canBlockFreeze(x, y - 1, z, false)) {
                chunk.func_150807_a(relX, y - 1, relZ, Blocks.ice, 0);
                world.markBlockForUpdate(x, y - 1, z);
            }
            if (world.func_147478_e(x, y, z, true)) {
                chunk.func_150807_a(relX, y, relZ, Blocks.snow_layer, 0);
                world.markBlockForUpdate(x, y, z);
            }
            // Snow under trees
            boolean cont = true;
            while (cont && y > 0) {
                y--;
                Block block = chunk.getBlock(relX, y, relZ);
                cont = block instanceof BlockLeavesBase || block.isAir(world, x, y, z);
            }
            if (world.func_147478_e(x, y + 1, z, true)) {
                chunk.func_150807_a(relX, y + 1, relZ, Blocks.snow_layer, 0);
                world.markBlockForUpdate(x, y + 1, z);
            }
        } else {
            Block block = chunk.getBlock(relX, y, relZ);
            if (block == Blocks.snow_layer) {
                chunk.func_150807_a(relX, y, relZ, Blocks.air, 0);
                world.markBlockForUpdate(x, y, z);
            }
            if (y > 1 && chunk.getBlock(relX, y - 1, relZ) == Blocks.ice) {
                // Don't melt floating ice; liquid counts so frozen deep water still melts
                Material below = chunk.getBlock(relX, y - 2, relZ).getMaterial();
                if (below.blocksMovement() || below.isLiquid()) {
                    // Same result as vanilla ice melting from light
                    Blocks.ice.dropBlockAsItem(world, x, y - 1, z, world.getBlockMetadata(x, y - 1, z), 0);
                    Block melted = world.provider.isHellWorld ? Blocks.air : Blocks.water;
                    chunk.func_150807_a(relX, y - 1, relZ, melted, 0);
                    world.markBlockForUpdate(x, y - 1, z);
                    // Update only the water itself so it starts flowing; notifying neighbours could load adjacent chunks
                    world.notifyBlockOfNeighborChange(x, y - 1, z, Blocks.ice);
                }
            }
            // Snow under trees
            boolean cont = true;
            while (cont && y > 0) {
                y--;
                block = chunk.getBlock(relX, y, relZ);
                cont = block instanceof BlockLeavesBase || block.isAir(world, x, y, z);
                if (block == Blocks.snow_layer) {
                    chunk.func_150807_a(relX, y, relZ, Blocks.air, 0);
                    world.markBlockForUpdate(x, y, z);
                }
            }
        }
    }

    public void processChunk(Chunk chunk, long lastUpdateTime) {
        int chunkIndex = getBlockScheduleIndex(chunk.xPosition, chunk.zPosition);
        chunkIndex = chunkIndex << 8;

        long[] lastSnowTicksWinter = seasonWorldData.lastSnowTicksWinter;
        long[] lastSnowTicksAny = seasonWorldData.lastSnowTicksAny;
        long[] lastThawTicksSummer = seasonWorldData.lastThawTicksSummer;
        long[] lastThawTicksAny = seasonWorldData.lastThawTicksAny;

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int index = chunkIndex + (i << 4) + j;
                int x = (chunk.xPosition << 4) + i;
                int z = (chunk.zPosition << 4) + j;
                BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
                int y = chunk.getHeightValue(i, j);
                boolean isPermaSnow = Season.SUMMER_MID.getAdjustedTemperatureFloat(biome, x, y, z) <= 0.15F;
                boolean isPermaThaw = Season.WINTER_MID.getAdjustedTemperatureFloat(biome, x, y, z) > 0.15F;

                if (isPermaThaw) {
                    if (lastThawTicksAny[index] > lastUpdateTime) {
                        processBlock(chunk, x, z, false);
                    }
                    continue;
                }

                if (isPermaSnow) {
                    if (lastSnowTicksAny[index] > lastUpdateTime) {
                        processBlock(chunk, x, z, true);
                    }
                    continue;
                }

                if (lastThawTicksSummer[index] > lastSnowTicksWinter[index]) {
                    if (lastThawTicksSummer[index] > lastUpdateTime) {
                        processBlock(chunk, x, z, false);
                    }
                } else {
                    if (lastSnowTicksWinter[index] > lastUpdateTime) {
                        processBlock(chunk, x, z, true);
                    }
                }
            }
        }
    }

    public void handleSnowServerTick(Chunk chunk) {
        int index = getBlockScheduleIndex(chunk.xPosition, chunk.zPosition);
        int[] schedule = chunkSchedules[index][seasonWorldData.schedulePos];

        BiomeGenBase[] biomes = chunkBiomeCache.computeIfAbsent(chunk, (dummy) -> {
            BiomeGenBase[] ret = new BiomeGenBase[256];
            for (int i = 0; i < 256; i++) {
                ret[i] = chunk.worldObj.getBiomeGenForCoords((chunk.xPosition << 4) + (i >> 4), (chunk.zPosition << 4) + (i & 0xf));
            }
            return ret;
        });

        for (int i = 0; i < schedule.length; i++) {
            int blockPos = schedule[i];
            int x = (chunk.xPosition << 4) + (blockPos >> 4);
            int z = (chunk.zPosition << 4) + (blockPos & 0xf);

            BiomeGenBase biome = biomes[blockPos];
            int y = chunk.getHeightValue(blockPos >> 4, blockPos & 0xf);
            float temperature = seasonWorldData.season.getAdjustedTemperatureFloat(biome, x, y, z);
            boolean canSnow = temperature <= 0.15F;

            if (canSnow) {
                if (world.isRaining()) {
                    processBlock(chunk, x, z, true);
                }
            } else {
                processBlock(chunk, x, z, false);
            }
        }
    }

    public void handleSnowServerGlobal() {
        // Resolve a season boundary before updating either the global pattern or active chunks.
        if (seasonWorldData.seasonTicks >= MAX_SEASON_LENGTH) {
            seasonWorldData.seasonTicks = 0;
            seasonWorldData.season = seasonWorldData.season.nextSeason();
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
            if (seasonWorldData.schedulePos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
                seasonWorldData.schedulePos = 0;
            }
        }

        long tick = world.getTotalWorldTime();
        boolean winter = seasonWorldData.season.isWinter();

        for (int chunk = 0; chunk < 256; chunk++) {
            int[] schedule = chunkSchedules[chunk][seasonWorldData.schedulePos];
            for (int i = 0; i < schedule.length; i++) {
                int blockPos = schedule[i];
                int pos = (chunk << 8) + blockPos;
                if (raining) {
                    seasonWorldData.lastSnowTicksAny[pos] = tick;
                    if (winter) {
                        seasonWorldData.lastSnowTicksWinter[pos] = tick;
                    }
                }
                seasonWorldData.lastThawTicksAny[pos] = tick;
                if (!winter) {
                    seasonWorldData.lastThawTicksSummer[pos] = tick;
                }
            }
        }

        seasonWorldData.seasonTicks++;
        seasonWorldData.markDirty();
    }
}
