package com.darkshadow44.seasonalhorizons.save;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonEvent;

public class SeasonWorldData extends WorldSavedData {

    public Season season = Season.SPRING_EARLY;

    public int seasonTicks;

    // To detect changes
    public Season lastSeason;
    public boolean currentIsRaining;

    // List of all times it snowed in winter (at most MAX_EVENT_CACHE entries)
    public List<SeasonEvent> snowEvents = new ArrayList<>();

    // List of all times it thawed (at most MAX_EVENT_CACHE entries)
    public List<SeasonEvent> thawEvents = new ArrayList<>();

    public SeasonWorldData(String name) {
        super(name);
        season = Season.WINTER_MID;
        seasonTicks = 0;
        lastSeason = season;
        currentIsRaining = false;
        for (int i = 0; i < 100; i++) {
            SeasonEvent event = new SeasonEvent(0, true, 0);
            event.end = 19000;
            snowEvents.add(event);
        }
    }

    private List<SeasonEvent> readSeasonEventList(NBTTagList tagList) {
        List<SeasonEvent> ret = new ArrayList<>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            ret.add(SeasonEvent.readFromNBT(tagList.getCompoundTagAt(i)));
        }
        return ret;
    }

    private NBTTagList writeSeasonEventList(List<SeasonEvent> events) {
        NBTTagList ret = new NBTTagList();
        for (SeasonEvent event : events) {
            NBTTagCompound tag = new NBTTagCompound();
            event.writeToNBT(tag);
            ret.appendTag(tag);
        }
        return ret;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("season", (byte) season.ordinal());
        tag.setInteger("seasonTicks", seasonTicks);
        tag.setByte("lastSeason", (byte) lastSeason.ordinal());
        tag.setBoolean("currentIsRaining", currentIsRaining);
        tag.setTag("snowEvents", writeSeasonEventList(snowEvents));
        tag.setTag("thawEvents", writeSeasonEventList(thawEvents));
    }
}
