package com.darkshadow44.seasonalhorizons.save;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

import com.darkshadow44.seasonalhorizons.season.Season;

public class SeasonWorldData extends WorldSavedData {

    public Season season = Season.SPRING_EARLY;

    public int seasonTicks;

    // Current position in the active snow/thaw schedule
    public int schedulePos;

    public boolean scheduleInitialized;
    public boolean scheduleRaining;
    public long scheduleSeed;

    public final long[] lastSnowTicksWinter = new long[256 * 256];
    public final long[] lastThawTicksSummer = new long[256 * 256];
    public final long[] lastSnowTicksAny = new long[256 * 256];
    public final long[] lastThawTicksAny = new long[256 * 256];

    public SeasonWorldData(String name) {
        super(name);
    }

    private void readSeasonEventList(long[] list, NBTTagCompound tag, String key) {
        if (!tag.hasKey(key + "_hi") && !tag.hasKey(key + "_lo")) {
            // Not saved yet; keep the defaults
            return;
        }
        int[] hi = tag.getIntArray(key + "_hi");
        int[] lo = tag.getIntArray(key + "_lo");
        if (hi.length != list.length || lo.length != list.length) {
            throw new IllegalStateException(
                "Invalid season data for " + key
                    + ": expected "
                    + list.length
                    + " entries, got hi="
                    + hi.length
                    + ", lo="
                    + lo.length);
        }
        for (int i = 0; i < list.length; i++) {
            list[i] = ((long) hi[i] << 32) | (lo[i] & 0xFFFFFFFFL);
        }
    }

    private void writeSeasonEventList(long[] list, NBTTagCompound tag, String key) {
        int[] hi = new int[list.length];
        int[] lo = new int[list.length];
        for (int i = 0; i < list.length; i++) {
            hi[i] = (int) (list[i] >> 32);
            lo[i] = (int) (list[i] & 0xFFFFFFFFL);
        }
        tag.setIntArray(key + "_hi", hi);
        tag.setIntArray(key + "_lo", lo);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        season = Season.values()[tag.getByte("season")];
        seasonTicks = tag.getInteger("seasonTicks");
        schedulePos = tag.getInteger("schedulePos");
        scheduleInitialized = tag.getBoolean("scheduleInitialized");
        scheduleRaining = tag.getBoolean("scheduleRaining");
        scheduleSeed = tag.getLong("scheduleSeed");
        readSeasonEventList(lastSnowTicksWinter, tag, "lastSnowTicksWinter");
        readSeasonEventList(lastSnowTicksAny, tag, "lastSnowTicksAny");
        readSeasonEventList(lastThawTicksSummer, tag, "lastThawTicksSummer");
        readSeasonEventList(lastThawTicksAny, tag, "lastThawTicksAny");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("season", (byte) season.ordinal());
        tag.setInteger("seasonTicks", seasonTicks);
        tag.setInteger("schedulePos", schedulePos);
        tag.setBoolean("scheduleInitialized", scheduleInitialized);
        tag.setBoolean("scheduleRaining", scheduleRaining);
        tag.setLong("scheduleSeed", scheduleSeed);
        writeSeasonEventList(lastSnowTicksWinter, tag, "lastSnowTicksWinter");
        writeSeasonEventList(lastSnowTicksAny, tag, "lastSnowTicksAny");
        writeSeasonEventList(lastThawTicksSummer, tag, "lastThawTicksSummer");
        writeSeasonEventList(lastThawTicksAny, tag, "lastThawTicksAny");
    }
}
