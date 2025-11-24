package com.darkshadow44.seasonalhorizons.season;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

public class SnowHandler {

    private static final int MAX_SEASON_LENGTH = 10000;
    private static final int MAX_TICKS_FOR_CHUNK_UPDATE = 20000;
    private static final int MAX_EVENT_CACHE = 10;

    // To speed up every tick processing
    private final WeakHashMap<Chunk, int[]> chunkSchedulesSnow = new WeakHashMap<>();
    private final WeakHashMap<Chunk, int[]> chunkSchedulesThaw = new WeakHashMap<>();

    // To avoid reallocations
    private final int[] tempSchedule = new int[MAX_TICKS_FOR_CHUNK_UPDATE];

    private final World world;

    // Saved season data
    SeasonWorldData seasonWorldData;

    public SnowHandler(World world, SeasonWorldData seasonWorldData) {
        this.world = world;
        this.seasonWorldData = seasonWorldData;
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

    private void processBlock(int x, int z, boolean snow) {
        int y = world.getHeightValue(x, z);
        if (snow) {
            if (world.func_147478_e(x, y, z, true)) {
                world.setBlock(x, y, z, Blocks.snow_layer, 0, 2);
            }
        } else {
            Block block = world.getBlock(x, y, z);
            if (block == Blocks.snow_layer) {
                world.setBlock(x, y, z, Blocks.air);
            }
        }
    }

    private void calculateChunkLastTicks(List<SeasonEvent> events, long[] lastChangeTick, boolean[] hasChange,
        boolean handlingSnow, Chunk chunk, long lastUpdateTime) {
        int lastFullEventIndex = 0;
        for (int i = events.size() - 1; i >= 0; i--) {
            SeasonEvent event = events.get(i);
            if (event.start >= lastUpdateTime && (event.end - event.start >= MAX_TICKS_FOR_CHUNK_UPDATE)) {
                lastFullEventIndex = i + 1;
                Arrays.fill(lastChangeTick, event.start);
                Arrays.fill(hasChange, true);
                break;
            }
        }

        for (int i = lastFullEventIndex; i < events.size(); i++) {
            SeasonEvent event = events.get(i);

            long end = event.end != 0 ? event.end : world.getTotalWorldTime();

            // Skip events that are not ongoing and have fully happened before chunk was unloaded
            if (end < lastUpdateTime) {
                continue;
            }

            long start = Math.max(lastUpdateTime, event.start);
            int count = (int) (end - start);

            getBlockSchedule(tempSchedule, chunk.xPosition, chunk.zPosition, event.seed);

            int pos = (int) (start % MAX_TICKS_FOR_CHUNK_UPDATE);

            if (event.isWinter == handlingSnow) {
                // Dealing with an event relevant for normal biomes
                // Otherwise we have perma snow/thaw
                for (int k = 0; k < count; k++) {
                    if (pos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
                        pos = 0;
                    }
                    int blockPos = tempSchedule[pos];
                    if (blockPos != -1) {
                        lastChangeTick[blockPos] = start + k;
                        hasChange[blockPos] = true;
                    }
                    pos++;
                }
            } else if (handlingSnow) {
                // Snow in summer (perma snow biomes)
                // We ignore thaw in winter in perma thaw biomes. Can't place snow anyways.
                for (int k = 0; k < count; k++) {
                    if (pos >= MAX_TICKS_FOR_CHUNK_UPDATE) {
                        pos = 0;
                    }
                    int blockPos = tempSchedule[pos];
                    if (blockPos != -1) {
                        hasChange[blockPos] = true;
                    }
                    pos++;
                }
            }
        }
    }

    public void processChunk(Chunk chunk, long lastUpdateTime) {
        long[] lastSnowTick = new long[256];
        long[] lastThawTick = new long[256];
        boolean[] snowChanges = new boolean[256];
        boolean[] thawChanges = new boolean[256]; // Not used as of now

        calculateChunkLastTicks(seasonWorldData.snowEvents, lastSnowTick, snowChanges, true, chunk, lastUpdateTime);
        calculateChunkLastTicks(seasonWorldData.thawEvents, lastThawTick, thawChanges, false, chunk, lastUpdateTime);

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int index = (i << 4) + j;
                int x = (chunk.xPosition << 4) + i;
                int z = (chunk.zPosition << 4) + j;
                BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
                boolean isPermaSnow = biome.temperature <= 0.15;
                boolean isPermaThaw = biome.temperature - 0.7 >= 0.15;

                if (isPermaThaw) {
                    continue;
                }

                if (isPermaSnow) {
                    if (snowChanges[index]) {
                        processBlock(x, z, true);
                    }
                    continue;
                }

                if (lastSnowTick[index] == 0 && lastThawTick[index] == 0) {
                    continue;
                }

                if (lastSnowTick[index] != 0 && lastThawTick[index] != 0) {
                    processBlock(x, z, lastSnowTick[index] > lastThawTick[index]);
                } else {
                    processBlock(x, z, lastSnowTick[index] != 0);
                }
            }
        }
    }

    private void handleSnowServerTickStep(Chunk chunk, WeakHashMap<Chunk, int[]> scheduleCache,
        List<SeasonEvent> eventList, boolean snow) {
        int[] schedule = scheduleCache.computeIfAbsent(chunk, dummy -> {
            SeasonEvent event = eventList.get(eventList.size() - 1);
            int[] ret = new int[MAX_TICKS_FOR_CHUNK_UPDATE];
            getBlockSchedule(ret, event.seed, chunk.xPosition, chunk.zPosition);
            return ret;
        });

        int pos = (int) (chunk.worldObj.getTotalWorldTime() % MAX_TICKS_FOR_CHUNK_UPDATE);

        int blockPos = schedule[pos];
        if (blockPos != -1) {
            int x = (chunk.xPosition << 4) + (blockPos >> 4);
            int z = (chunk.zPosition << 4) + (blockPos & 0xf);
            BiomeGenBase biome = chunk.worldObj.getBiomeGenForCoords(x, z);
            float temperature = seasonWorldData.season.getAdjustedTemperature(biome.temperature);
            boolean canSnow = temperature < 0.15;
            if (canSnow == snow) {
                processBlock(x, z, snow);
            }
        }
    }

    public void handleSnowServerTick(Chunk chunk) {
        handleSnowServerTickStep(chunk, chunkSchedulesThaw, seasonWorldData.thawEvents, false);
        if (seasonWorldData.currentIsRaining) {
            handleSnowServerTickStep(chunk, chunkSchedulesSnow, seasonWorldData.snowEvents, true);
        }
    }

    public void handleSnowServerGlobal() {
        boolean isRaining = world.isRaining();

        if (seasonWorldData.lastSeason != seasonWorldData.season) {
            if (seasonWorldData.lastSeason == null
                || seasonWorldData.season.isWinter() != seasonWorldData.lastSeason.isWinter()) {
                chunkSchedulesThaw.clear();
                List<SeasonEvent> thawEvents = seasonWorldData.thawEvents;
                if (!thawEvents.isEmpty()) {
                    thawEvents.get(thawEvents.size() - 1).end = world.getTotalWorldTime();
                }
                if (!seasonWorldData.season.isWinter()) {
                    thawEvents.add(new SeasonEvent(world, false));
                    if (thawEvents.size() > MAX_EVENT_CACHE) {
                        thawEvents.remove(0);
                    }
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
            chunkSchedulesSnow.clear();
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
}
