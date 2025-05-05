package com.darkshadow44.seasonalhorizons.season;

import com.darkshadow44.seasonalhorizons.color.SeasonColorMap;
import net.minecraft.world.biome.BiomeGenBase;

public enum Season {
    SPRING_EARLY(MainSeason.SPRING, SubSeason.EARLY),
    SPRING_MID(MainSeason.SPRING, SubSeason.MID),
    SPRING_LATE(MainSeason.SPRING, SubSeason.LATE),
    SUMMER_EARLY(MainSeason.SUMMER, SubSeason.EARLY),
    SUMMER_MID(MainSeason.SUMMER, SubSeason.MID),
    SUMMER_LATE(MainSeason.SUMMER, SubSeason.LATE),
    AUTUMN_EARLY(MainSeason.AUTUMN, SubSeason.EARLY),
    AUTUMN_MID(MainSeason.AUTUMN, SubSeason.MID),
    AUTUMN_LATE(MainSeason.AUTUMN, SubSeason.LATE),
    WINTER_EARLY(MainSeason.WINTER, SubSeason.EARLY),
    WINTER_MID(MainSeason.WINTER, SubSeason.MID),
    WINTER_LATE(MainSeason.WINTER, SubSeason.LATE);

    private final MainSeason mainSeason;
    private final SubSeason subSeason;

    private SeasonColorMap colorMapFoliage;
    private SeasonColorMap colorMapGrass;

    Season(MainSeason mainSeason, SubSeason subSeason) {
        this.mainSeason = mainSeason;
        this.subSeason = subSeason;
    }

    public String getId() {
        return subSeason.getId() + "_" + mainSeason.getId();
    }

    public MainSeason getMainSeason() {
        return mainSeason;
    }

    public SubSeason getSubSeason() {
        return subSeason;
    }

    public void setFoliageColorMap(int[] rawPixelData) {
        colorMapFoliage = new SeasonColorMap(rawPixelData);
    }

    public void setGrassColorMap(int[] rawPixelData) {
        colorMapGrass = new SeasonColorMap(rawPixelData);
    }

    public int getFoliageColor(BiomeGenBase biome) {
        return colorMapFoliage.getColor(biome.temperature, biome.rainfall);
    }

    public int getGrassColor(BiomeGenBase biome) {
        return colorMapGrass.getColor(biome.temperature, biome.rainfall);
    }
}
