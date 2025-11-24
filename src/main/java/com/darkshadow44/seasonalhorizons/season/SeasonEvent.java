package com.darkshadow44.seasonalhorizons.season;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class SeasonEvent {

    public final int seed;
    public final boolean isWinter;
    public final long start;
    public long end;

    public SeasonEvent(int seed, boolean isWinter, long start) {
        this.seed = seed;
        this.isWinter = isWinter;
        this.start = start;
    }

    public SeasonEvent(World world, boolean isWinter) {
        this(world.rand.nextInt(), isWinter, world.getTotalWorldTime());
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("seed", seed);
        tag.setBoolean("isWinter", isWinter);
        tag.setLong("start", start);
        tag.setLong("end", end);
    }

    public static SeasonEvent readFromNBT(NBTTagCompound tag) {
        int seed = tag.getInteger("seed");
        boolean isWinter = tag.getBoolean("isWinter");
        long start = tag.getLong("start");
        SeasonEvent ret = new SeasonEvent(seed, isWinter, start);
        ret.end = tag.getLong("end");
        return ret;
    }
}
