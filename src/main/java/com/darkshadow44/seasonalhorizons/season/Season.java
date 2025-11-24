package com.darkshadow44.seasonalhorizons.season;

import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.color.SeasonColorMap;
import com.darkshadow44.seasonalhorizons.mixininterfaces.IMixinBiomeGenBase;

public enum Season {

    SPRING_EARLY(MainSeason.SPRING, SubSeason.EARLY, false),
    SPRING_MID(MainSeason.SPRING, SubSeason.MID, false),
    SPRING_LATE(MainSeason.SPRING, SubSeason.LATE, false),
    SUMMER_EARLY(MainSeason.SUMMER, SubSeason.EARLY, false),
    SUMMER_MID(MainSeason.SUMMER, SubSeason.MID, false),
    SUMMER_LATE(MainSeason.SUMMER, SubSeason.LATE, false),
    AUTUMN_EARLY(MainSeason.AUTUMN, SubSeason.EARLY, false),
    AUTUMN_MID(MainSeason.AUTUMN, SubSeason.MID, false),
    AUTUMN_LATE(MainSeason.AUTUMN, SubSeason.LATE, false),
    WINTER_EARLY(MainSeason.WINTER, SubSeason.EARLY, true),
    WINTER_MID(MainSeason.WINTER, SubSeason.MID, true),
    WINTER_LATE(MainSeason.WINTER, SubSeason.LATE, true);

    private final MainSeason mainSeason;
    private final SubSeason subSeason;
    private final boolean isWinter;
    private SeasonColorMap colorMapFoliage;
    private SeasonColorMap colorMapGrass;

    Season(MainSeason mainSeason, SubSeason subSeason, boolean isWinter) {
        this.mainSeason = mainSeason;
        this.subSeason = subSeason;
        this.isWinter = isWinter;
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
        float temperatureChange = isWinter ? -0.7f : 0;
        return MathHelper.clamp_float(temperature + temperatureChange, -0.5f, 2.0f);
    }

    public float getAdjustedTemperatureFloat(BiomeGenBase biome, int x, int y, int z) {
        IMixinBiomeGenBase biomeMixin = (IMixinBiomeGenBase) biome;
        float temperature = getAdjustedTemperature(biome.temperature);
        return biomeMixin.seasonalHorizons$getAdjustedFloatTemperature(temperature, x, y, z);
    }

    public float getAdjustedRainfall(float rainfall) {
        return rainfall;
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

    public boolean isWinter() {
        return isWinter;
    }

    public Season nextSeason() {
        int seasonId = ordinal() + 1;
        if (seasonId > Season.WINTER_LATE.ordinal()) {
            seasonId = 0;
        }
        return Season.values()[seasonId];
    }
}
