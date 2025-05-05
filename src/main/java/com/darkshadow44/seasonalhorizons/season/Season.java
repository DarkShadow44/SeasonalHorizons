package com.darkshadow44.seasonalhorizons.season;

import com.darkshadow44.seasonalhorizons.color.SeasonColorMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

public enum Season {
    SPRING_EARLY(MainSeason.SPRING, SubSeason.EARLY, -0.4f, 0.1f),
    SPRING_MID(MainSeason.SPRING, SubSeason.MID, -0.2f, 0.3f),
    SPRING_LATE(MainSeason.SPRING, SubSeason.LATE, -0.1f, 0.2f),
    SUMMER_EARLY(MainSeason.SUMMER, SubSeason.EARLY, 0.3f, -0.2f),
    SUMMER_MID(MainSeason.SUMMER, SubSeason.MID, 0.8f, -0.5f),
    SUMMER_LATE(MainSeason.SUMMER, SubSeason.LATE, 0.4f, -0.2f),
    AUTUMN_EARLY(MainSeason.AUTUMN, SubSeason.EARLY, 0.1f, 0.1f),
    AUTUMN_MID(MainSeason.AUTUMN, SubSeason.MID, -0.2f, 0.2f),
    AUTUMN_LATE(MainSeason.AUTUMN, SubSeason.LATE, -0.4f, 0.1f),
    WINTER_EARLY(MainSeason.WINTER, SubSeason.EARLY, -0.7f, 0),
    WINTER_MID(MainSeason.WINTER, SubSeason.MID, -0.8f, 0.2f),
    WINTER_LATE(MainSeason.WINTER, SubSeason.LATE, -0.7f, 0);

    private final MainSeason mainSeason;
    private final SubSeason subSeason;
    private final float temperatureChange;
    private final float rainChange;

    private SeasonColorMap colorMapFoliage;
    private SeasonColorMap colorMapGrass;

    Season(MainSeason mainSeason, SubSeason subSeason, float temperatureChange, float rainChange) {
        this.mainSeason = mainSeason;
        this.subSeason = subSeason;
        this.temperatureChange = temperatureChange;
        this.rainChange = rainChange;
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

    public float getAdjustedTemperature(float temperature) {
        return MathHelper.clamp_float(temperature + temperatureChange, -0.5f, 2.0f);
    }

    public float getAdjustedRainfall(float rainfall) {
        return MathHelper.clamp_float(rainfall + rainChange, -0.5f, 2.0f);
    }

    public void setFoliageColorMap(int[] rawPixelData) {
        colorMapFoliage = new SeasonColorMap(rawPixelData);
    }

    public void setGrassColorMap(int[] rawPixelData) {
        colorMapGrass = new SeasonColorMap(rawPixelData);
    }

    public int getFoliageColor(BiomeGenBase biome) {
        float temperature = getAdjustedTemperature(biome.temperature);
        float rainfall = getAdjustedRainfall(biome.rainfall);
        return colorMapFoliage.getColor(temperature, rainfall);
    }

    public int getGrassColor(BiomeGenBase biome) {
        float temperature = getAdjustedTemperature(biome.temperature);
        float rainfall = getAdjustedRainfall(biome.rainfall);
        return colorMapGrass.getColor(temperature, rainfall);
    }
}
