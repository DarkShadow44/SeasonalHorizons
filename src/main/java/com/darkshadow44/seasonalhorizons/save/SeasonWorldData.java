package com.darkshadow44.seasonalhorizons.save;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

import com.darkshadow44.seasonalhorizons.season.Season;

public class SeasonWorldData extends WorldSavedData {

    public Season season = Season.SPRING_EARLY;

    public int seasonTicks;

    // Per-dimension clock for snow/thaw timestamps; only advances while the dimension is loaded
    public long seasonTime;

    // Current position in the active snow/thaw schedule
    public int schedulePos;

    public boolean scheduleInitialized;
    public boolean scheduleRaining;
    public long scheduleSeed;

    public final long[] lastSnowTicksWinter = new long[256 * 256];
    public final long[] lastThawTicksSummer = new long[256 * 256];
    // 1 when the corresponding lastThawTicksSummer event occurred in autumn, 0 otherwise
    public byte[] lastThawSummerWasAutumn = new byte[256 * 256];
    public final long[] lastSnowTicksAny = new long[256 * 256];
    public final long[] lastThawTicksAny = new long[256 * 256];

    public SeasonWorldData(String name) {
        super(name);
    }

    public void changeSeason(Season newSeason) {
        season = newSeason;
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
        seasonTime = tag.getLong("seasonTime");
        schedulePos = tag.getInteger("schedulePos");
        scheduleInitialized = tag.getBoolean("scheduleInitialized");
        scheduleRaining = tag.getBoolean("scheduleRaining");
        scheduleSeed = tag.getLong("scheduleSeed");
        readSeasonEventList(lastSnowTicksWinter, tag, "lastSnowTicksWinter");
        readSeasonEventList(lastSnowTicksAny, tag, "lastSnowTicksAny");
        readSeasonEventList(lastThawTicksSummer, tag, "lastThawTicksSummer");
        readSeasonEventList(lastThawTicksAny, tag, "lastThawTicksAny");
        if (tag.hasKey("lastThawSummerWasAutumn")) {
            lastThawSummerWasAutumn = tag.getByteArray("lastThawSummerWasAutumn");
            if (lastThawSummerWasAutumn.length != 256 * 256) {
                throw new IllegalStateException(
                    "Invalid season data for lastThawSummerWasAutumn: expected "
                        + (256 * 256)
                        + " entries, got "
                        + lastThawSummerWasAutumn.length);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("season", (byte) season.ordinal());
        tag.setInteger("seasonTicks", seasonTicks);
        tag.setLong("seasonTime", seasonTime);
        tag.setInteger("schedulePos", schedulePos);
        tag.setBoolean("scheduleInitialized", scheduleInitialized);
        tag.setBoolean("scheduleRaining", scheduleRaining);
        tag.setLong("scheduleSeed", scheduleSeed);
        writeSeasonEventList(lastSnowTicksWinter, tag, "lastSnowTicksWinter");
        writeSeasonEventList(lastSnowTicksAny, tag, "lastSnowTicksAny");
        writeSeasonEventList(lastThawTicksSummer, tag, "lastThawTicksSummer");
        writeSeasonEventList(lastThawTicksAny, tag, "lastThawTicksAny");
        tag.setByteArray("lastThawSummerWasAutumn", lastThawSummerWasAutumn);
    }
}
