package com.darkshadow44.seasonalhorizons.season;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

public class SnowHandler {

    private static final int MAX_SEASON_LENGTH = 10000;
    private static final int MAX_TICKS_FOR_CHUNK_UPDATE = 1000;
    private static final int MAX_CHUNK_SCHEDULES_MASK = 0x7F;
    private static final int MAX_EVENT_CACHE = 10;

    private final List<int[]> chunkSchedules = new ArrayList<>();
    private final List<ScheduleEntry[]> chunkSchedulesSparse = new ArrayList<>();

    private final World world;

    // Saved season data
    SeasonWorldData seasonWorldData;

    public SnowHandler(World world, SeasonWorldData seasonWorldData) {
        this.world = world;
        this.seasonWorldData = seasonWorldData;

        Random random = new Random(world.getSeed());
        for (int i = 0; i < MAX_CHUNK_SCHEDULES_MASK + 1; i++) {
            int[] schedule = generateBlockSchedule(random.nextInt());
            chunkSchedules.add(schedule);
            chunkSchedulesSparse.add(generateBlockScheduleSparse(schedule));
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
    private int[] generateBlockSchedule(int seed) {
        int[] schedule = new int[MAX_TICKS_FOR_CHUNK_UPDATE];
        final int length = schedule.length;
        Arrays.fill(schedule, -1);

        for (int i = 0; i < 256; i++) {
            long hash = mix(seed ^ mix(i));
            int slot = (int) (hash % length);
            if (slot < 0) slot += length;

            while (schedule[slot] != -1) {
                slot++;
                if (slot == length) slot = 0;
            }

            schedule[slot] = i;
        }
        return schedule;
    }

    private ScheduleEntry[] generateBlockScheduleSparse(int[] schedule) {
        ScheduleEntry[] ret = new ScheduleEntry[256];
        int pos = 0;
        for (int i = 0; i < MAX_TICKS_FOR_CHUNK_UPDATE; i++) {
            int blockPos = schedule[i];
            if (blockPos != -1) {
                ret[pos++] = new ScheduleEntry(i, blockPos);
            }
        }
        return ret;
    }

    private int[] getBlockSchedule(int seed, int chunkX, int chunkZ) {
        int scheduleIndex = mix(seed ^ mix(chunkX) ^ mix(chunkZ)) & MAX_CHUNK_SCHEDULES_MASK;
        return chunkSchedules.get(scheduleIndex);
    }

    private ScheduleEntry[] getBlockScheduleSparse(int seed, int chunkX, int chunkZ) {
        int scheduleIndex = mix(seed ^ mix(chunkX) ^ mix(chunkZ)) & MAX_CHUNK_SCHEDULES_MASK;
        return chunkSchedulesSparse.get(scheduleIndex);
    }

    private void processBlock(Chunk chunk, int x, int z, boolean snow) {
        int relX = x & 0xf;
        int relZ = z & 0xf;
        int y = chunk.getHeightValue(relX, relZ);
        if (snow) {
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

    private void calculateChunkLastTicksApply(ScheduleEntry[] schedule, boolean handlingWinterBlock, long start,
        int startIndex, int endIndex, long[] lastChangeTick, boolean[] hasChange) {
        for (int k = 0; k < 256; k++) {
            ScheduleEntry entry = schedule[k];
            if (entry.tick >= startIndex && entry.tick < endIndex) {
                if (handlingWinterBlock) {
                    // This gets skipped when it snows in summer, since perma snow only needs that
                    lastChangeTick[entry.blockPos] = start + entry.tick - startIndex;
                }
                hasChange[entry.blockPos] = true;
            }
        }
    }

    private void calculateChunkLastTicks(List<SeasonEvent> events, long[] lastChangeTick, boolean[] hasChange,
        boolean handlingSnow, Chunk chunk, long lastUpdateTime) {

        int lastFullEventIndex = 0;
        for (int i = events.size() - 1; i >= 0; i--) {
            SeasonEvent event = events.get(i);
            long end = event.end != 0 ? event.end : world.getTotalWorldTime();
            if (event.start >= lastUpdateTime && (end - event.start >= MAX_TICKS_FOR_CHUNK_UPDATE)) {
                lastFullEventIndex = i + 1;
                Arrays.fill(lastChangeTick, event.start + 1); // Must not set to 0
                Arrays.fill(hasChange, true);
                break;
            }
        }

        for (int i = lastFullEventIndex; i < events.size(); i++) {
            SeasonEvent event = events.get(i);

            // Either snowing in winter or thaw in summer works for all blocks.
            boolean handlingWinterBlock = event.isWinter == handlingSnow;
            if (!handlingWinterBlock && !handlingSnow) {
                // We ignore thaw in winter in perma thaw biomes. Can't place snow anyways.
                continue;
            }

            long end = event.end != 0 ? event.end : world.getTotalWorldTime();

            // Skip events that are not ongoing and have fully happened before chunk was unloaded
            if (end < lastUpdateTime) {
                continue;
            }

            long start = Math.max(lastUpdateTime, event.start);

            ScheduleEntry[] schedule = getBlockScheduleSparse(event.seed, chunk.xPosition, chunk.zPosition);

            int startIndex1 = (int) (start % MAX_TICKS_FOR_CHUNK_UPDATE);
            int endIndex1 = (int) (end % MAX_TICKS_FOR_CHUNK_UPDATE);

            int startIndex2 = -1, endIndex2 = -1;
            if (endIndex1 < startIndex1) {
                startIndex2 = 0;
                endIndex2 = endIndex1;
                endIndex1 = MAX_TICKS_FOR_CHUNK_UPDATE;
            }

            calculateChunkLastTicksApply(
                schedule,
                handlingWinterBlock,
                start,
                startIndex1,
                endIndex1,
                lastChangeTick,
                hasChange);
            if (startIndex2 != -1) {
                // Next block, wraparound
                start += MAX_TICKS_FOR_CHUNK_UPDATE - startIndex1;
                calculateChunkLastTicksApply(
                    schedule,
                    handlingWinterBlock,
                    start,
                    startIndex2,
                    endIndex2,
                    lastChangeTick,
                    hasChange);
            }
        }
    }

    public void processChunk(Chunk chunk, long lastUpdateTime) {
        long[] lastSnowTick = new long[256];
        long[] lastThawTick = new long[256];
        boolean[] snowChanges = new boolean[256];
        boolean[] thawChanges = new boolean[256];

        calculateChunkLastTicks(seasonWorldData.snowEvents, lastSnowTick, snowChanges, true, chunk, lastUpdateTime);
        calculateChunkLastTicks(seasonWorldData.thawEvents, lastThawTick, thawChanges, false, chunk, lastUpdateTime);

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int index = (i << 4) + j;
                if (!snowChanges[index] && !thawChanges[index]) {
                    continue;
                }

                int x = (chunk.xPosition << 4) + i;
                int z = (chunk.zPosition << 4) + j;
                BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
                boolean isPermaSnow = biome.temperature <= 0.15;
                boolean isPermaThaw = biome.temperature - 0.7 > 0.15;

                if (isPermaThaw) {
                    continue;
                }

                if (isPermaSnow) {
                    if (snowChanges[index]) {
                        processBlock(chunk, x, z, true);
                    }
                    continue;
                }

                if (lastSnowTick[index] == 0 && lastThawTick[index] == 0) {
                    continue;
                }

                if (lastSnowTick[index] != 0 && lastThawTick[index] != 0) {
                    processBlock(chunk, x, z, lastSnowTick[index] > lastThawTick[index]);
                } else {
                    processBlock(chunk, x, z, lastSnowTick[index] != 0);
                }
            }
        }
    }

    private void handleSnowServerTickStep(Chunk chunk, List<SeasonEvent> eventList, boolean snow) {
        if (eventList.isEmpty()) {
            return;
        }

        SeasonEvent event = eventList.get(eventList.size() - 1);

        int[] schedule = getBlockSchedule(event.seed, chunk.xPosition, chunk.zPosition);

        int pos = (int) (chunk.worldObj.getTotalWorldTime() % MAX_TICKS_FOR_CHUNK_UPDATE);

        int blockPos = schedule[pos];
        if (blockPos != -1) {
            int x = (chunk.xPosition << 4) + (blockPos >> 4);
            int z = (chunk.zPosition << 4) + (blockPos & 0xf);
            BiomeGenBase biome = chunk.worldObj.getBiomeGenForCoords(x, z);
            float temperature = seasonWorldData.season.getAdjustedTemperature(biome.temperature);
            boolean canSnow = temperature <= 0.15;
            if (canSnow == snow) {
                processBlock(chunk, x, z, snow);
            }
        }
    }

    public void handleSnowServerTick(Chunk chunk) {
        handleSnowServerTickStep(chunk, seasonWorldData.thawEvents, false);
        if (seasonWorldData.currentIsRaining) {
            handleSnowServerTickStep(chunk, seasonWorldData.snowEvents, true);
        }
    }

    public void handleSnowServerGlobal() {
        boolean isRaining = world.isRaining();

        if (seasonWorldData.lastSeason != seasonWorldData.season) {
            if (seasonWorldData.lastSeason == null
                || seasonWorldData.season.isWinter() != seasonWorldData.lastSeason.isWinter()) {
                List<SeasonEvent> thawEvents = seasonWorldData.thawEvents;
                if (!thawEvents.isEmpty()) {
                    thawEvents.get(thawEvents.size() - 1).end = world.getTotalWorldTime();
                }
                thawEvents.add(new SeasonEvent(world, seasonWorldData.season.isWinter()));
                if (thawEvents.size() > MAX_EVENT_CACHE) {
                    thawEvents.remove(0);
                }
                if (isRaining) {
                    // Force new snow event, since it might have changed from snow to rain or vice versa
                    seasonWorldData.currentIsRaining = false;
                }
            }
            seasonWorldData.lastSeason = seasonWorldData.season;
        }

        // Process snowing
        if (seasonWorldData.currentIsRaining != isRaining) {
            List<SeasonEvent> snowEvents = seasonWorldData.snowEvents;
            if (isRaining) {
                snowEvents.add(new SeasonEvent(world, seasonWorldData.season.isWinter()));
                if (snowEvents.size() > MAX_EVENT_CACHE) {
                    snowEvents.remove(0);
                }
            } else if (!snowEvents.isEmpty()) {
                snowEvents.get(snowEvents.size() - 1).end = world.getTotalWorldTime();
            }
            seasonWorldData.currentIsRaining = isRaining;
        }

        seasonWorldData.seasonTicks++;
        if (seasonWorldData.seasonTicks >= MAX_SEASON_LENGTH) {
            seasonWorldData.seasonTicks = 0;
            seasonWorldData.season = seasonWorldData.season.nextSeason();
        }
        seasonWorldData.markDirty();
    }

    private static class ScheduleEntry {

        public final int tick;
        public final int blockPos;

        public ScheduleEntry(int tick, int blockPos) {
            this.tick = tick;
            this.blockPos = blockPos;
        }
    }

}
