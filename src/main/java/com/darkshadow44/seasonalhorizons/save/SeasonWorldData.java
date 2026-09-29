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
    // Season of the corresponding lastThawTicksSummer event, see encodeSeason
    public final byte[] lastThawSummerSeason = new byte[256 * 256];
    public final long[] lastSnowTicksAny = new long[256 * 256];
    public final long[] lastThawTicksAny = new long[256 * 256];

    public SeasonWorldData(String name) {
        super(name);
    }

    // Stored as ordinal + 1, so the default 0 means unknown
    public static byte encodeSeason(Season season) {
        return (byte) (season.ordinal() + 1);
    }

    // null when unknown
    public static Season decodeSeason(byte season) {
        return season == 0 ? null : Season.byOrdinal(season - 1);
    }

    private static byte[] readSeasonByteList(NBTTagCompound tag, String key) {
        byte[] list = tag.getByteArray(key);
        if (list.length != 256 * 256) {
            throw new IllegalStateException(
                "Invalid season data for " + key + ": expected " + (256 * 256) + " entries, got " + list.length);
        }
        return list;
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
        if (tag.hasKey("lastThawSummerSeason")) {
            byte[] list = readSeasonByteList(tag, "lastThawSummerSeason");
            System.arraycopy(list, 0, lastThawSummerSeason, 0, list.length);
        } else if (tag.hasKey("lastThawSummerWasAutumn")) {
            // Older saves only recorded autumn, without the subseason, so mid autumn is a best guess; the rest stays
            // unknown
            byte[] wasAutumn = readSeasonByteList(tag, "lastThawSummerWasAutumn");
            for (int i = 0; i < wasAutumn.length; i++) {
                if (wasAutumn[i] != 0) {
                    lastThawSummerSeason[i] = encodeSeason(Season.AUTUMN_MID);
                }
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
        tag.setByteArray("lastThawSummerSeason", lastThawSummerSeason);
    }
}
