package com.darkshadow44.seasonalhorizons.season;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class SnowHandler {

    private static final int MAX_TICKS_FOR_CHUNK_UPDATE = 20000;

    private static class SnowEvent {

        public long start;
        public long end;

        public SnowEvent(long start) {
            this.start = start;
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
     * processed. schedule[i] is the coordinate of the block (x * 16 + z) to process at tick i, or -1 for none.
     */
    public static void getBlockSchedule(int[] schedule, int seed, int chunkX, int chunkZ) {
        final int length = schedule.length;
        Arrays.fill(schedule, -1);

        int seedBase = mix(seed ^ mix(chunkX) ^ mix(chunkZ));

        for (int i = 0; i < 256; i++) {
            long hash = mix(seedBase ^ mix(i));
            int slot = (int) (hash % length);
            if (slot < 0) slot += length;

            while (schedule[slot] != -1) {
                slot++;
                if (slot == length) slot = 0;
            }

            schedule[slot] = i;
        }
    }

    private static void processBlock(Chunk chunk, int x, int z) {
        int blockX = (chunk.xPosition << 4) + x;
        int blockZ = (chunk.zPosition << 4) + z;

        if (chunk.xPosition == 0 && chunk.zPosition == 0) {
            int k = 0;
        }

        BiomeGenBase biome = chunk.worldObj.getBiomeGenForCoords(blockX, blockZ);
        int y = chunk.getHeightValue(x, z);
        float temperature = lastSeason.getAdjustedTemperature(biome.temperature);

        if (temperature >= 0.15) {
            Block block = chunk.getBlock(x, y, z);
            if (block == Blocks.snow_layer) {
                chunk.worldObj.setBlock(blockX, y, blockZ, Blocks.air);
            }
            /*
             * if (block == Blocks.ice) {
             * chunk.func_150807_a(x, y, z, Blocks.water, 0);
             * }
             */
        } else {
            if (chunk.worldObj.func_147478_e(blockX, y, blockZ, true)) {
                chunk.worldObj.setBlock(blockX, y, blockZ, Blocks.snow_layer);
            }
        }
    }

    private static void processUpdatesForChunk(Chunk chunk, long lastUpdateTime) {
        long difference = chunk.worldObj.getTotalWorldTime() - lastUpdateTime;
        if (difference > MAX_TICKS_FOR_CHUNK_UPDATE) {
            for (int i = 0; i < 256; i++) {
                processBlock(chunk, i >> 4, i & 0xf);
            }
            return;
        }

        int[] schedule = new int[MAX_TICKS_FOR_CHUNK_UPDATE];

        getBlockSchedule(schedule, 0, chunk.xPosition, chunk.zPosition);

        // Process next ticks
        int pos = (int) (chunk.worldObj.getTotalWorldTime() % MAX_TICKS_FOR_CHUNK_UPDATE);
        for (int i = 0; i < difference; i++) {
            if (pos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
                pos = 0;
            }
            int blockPos = schedule[pos];
            if (blockPos != -1) {
                processBlock(chunk, blockPos >> 4, blockPos & 0xf);
            }
            pos++;
        }
    }

    public static void handleSnowServer(Chunk chunk) {
        int[] schedule = chunkSchedules.computeIfAbsent(chunk, dummy -> {
            int[] ret = new int[MAX_TICKS_FOR_CHUNK_UPDATE];
            getBlockSchedule(ret, 0, chunk.xPosition, chunk.zPosition);
            return ret;
        });

        int pos = (int) (chunk.worldObj.getTotalWorldTime() % MAX_TICKS_FOR_CHUNK_UPDATE);
        int blockPos = schedule[pos];
        if (blockPos != -1) {
            processBlock(chunk, blockPos >> 4, blockPos & 0xf);
        }
    }

    private static final WeakHashMap<Chunk, int[]> chunkSchedules = new WeakHashMap<>();

    // TODO save!
    private static Season lastSeason;
    private static boolean lastIsRaining;
    // Thaw ticks to process.
    private static int thawTicks;
    private static final List<SnowEvent> snowEvents = new ArrayList<>();

    public static void handleSnowServerGlobal(WorldServer world) {
        Season currentSeason = SeasonHandlerServer.getSeasonForWorld(world);
        if (currentSeason != lastSeason) {
            if (lastSeason == null || currentSeason.isWinter() != lastSeason.isWinter()) {
                thawTicks = 0;
                snowEvents.clear(); // TODO only clear snow events once thawTicks reached max, same for thaw. e.g. new chunk in early spring still needs snow from last winter...
            }
            lastSeason = currentSeason;
        }

        // Process thawing
        if (thawTicks < MAX_TICKS_FOR_CHUNK_UPDATE) {
            thawTicks++;
        } else {
            thawTicks = 0;
        }

        // Process snowing
        boolean isRaining = world.isRaining();
        if (lastIsRaining != isRaining) {
            if (isRaining) {
                snowEvents.add(new SnowEvent(world.getTotalWorldTime()));
            } else if (!snowEvents.isEmpty()) {
                snowEvents.get(snowEvents.size() - 1).end = world.getTotalWorldTime();
            }
            lastIsRaining = isRaining;
        }
    }
}
