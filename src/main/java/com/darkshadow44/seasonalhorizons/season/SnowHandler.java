package com.darkshadow44.seasonalhorizons.season;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class SnowHandler {

    private static final int MAX_SEASON_LENGTH = 10000;
    private static final int MAX_TICKS_FOR_CHUNK_UPDATE = 1000;
    private static final int MAX_BLOCK_REPEAT = 4;

    // Of format [(chunkX % 16) << 4 + (chunkY % 16))] [(blockX % 16) << 4 + (blockY % 16)] []
    private final int[][][] chunkSchedules = new int[256][MAX_TICKS_FOR_CHUNK_UPDATE][];

    private final World world;

    // Saved season data
    SeasonWorldData seasonWorldData;

    public SnowHandler(World world, SeasonWorldData seasonWorldData) {
        this.world = world;
        this.seasonWorldData = seasonWorldData;

        Random random = new Random(world.getSeed());
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

    private void processBlock(Chunk chunk, int x, int z, boolean snow) {
        int relX = x & 0xf;
        int relZ = z & 0xf;
        int y = chunk.getHeightValue(relX, relZ);
        if (snow) {
            if (chunk.getBlock(relX, y -1, relZ).isAir(world, x, y, z)) {
                return;
            }
            if (world.func_147478_e(x, y, z, true)) {
                chunk.func_150807_a(relX, y, relZ, Blocks.snow_layer, 0);
                world.markBlockForUpdate(x, y, z);
            }
            // Snow under trees
            boolean cont = true;
            while (cont) {
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
            // Snow under trees
            boolean cont = true;
            while (cont) {
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
                boolean isPermaSnow = biome.temperature <= 0.15;
                boolean isPermaThaw = biome.temperature - 0.7 > 0.15;

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

        for (int i = 0; i < schedule.length; i++) {
            int blockPos = schedule[i];
            int x = (chunk.xPosition << 4) + (blockPos >> 4);
            int z = (chunk.zPosition << 4) + (blockPos & 0xf);

            BiomeGenBase biome = chunk.worldObj.getBiomeGenForCoords(x, z);
            float temperature = seasonWorldData.season.getAdjustedTemperature(biome.temperature);
            boolean canSnow = temperature <= 0.15;

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
        // Advance pattern
        seasonWorldData.schedulePos++;
        if (seasonWorldData.schedulePos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
            seasonWorldData.schedulePos = 0;
        }

        long tick = world.getTotalWorldTime();

        for (int chunk = 0; chunk < 256; chunk++) {
            int[] schedule = chunkSchedules[chunk][seasonWorldData.schedulePos];
            for (int i = 0; i < schedule.length; i++) {
                int blockPos = schedule[i];
                int pos = (chunk << 8) + blockPos;
                if (world.isRaining()) {
                    seasonWorldData.lastSnowTicksAny[pos] = tick;
                    if (seasonWorldData.season.isWinter()) {
                        seasonWorldData.lastSnowTicksWinter[pos] = tick;
                    }
                }
                seasonWorldData.lastThawTicksAny[pos] = tick;
                if (!seasonWorldData.season.isWinter()) {
                    seasonWorldData.lastThawTicksSummer[pos] = tick;
                }
            }
        }

        // Advance season

        seasonWorldData.seasonTicks++;
        if (seasonWorldData.seasonTicks >= MAX_SEASON_LENGTH) {
            seasonWorldData.seasonTicks = 0;
            seasonWorldData.season = seasonWorldData.season.nextSeason();
        }
        seasonWorldData.markDirty();
    }
}
