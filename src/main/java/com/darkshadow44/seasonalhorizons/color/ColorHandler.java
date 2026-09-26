package com.darkshadow44.seasonalhorizons.color;

import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.season.ClientSeasonHandler;
import com.darkshadow44.seasonalhorizons.season.Season;

public class ColorHandler {

    public static int updateColorFoliage(BiomeGenBase biome, int originalColor) {
        Season season = ClientSeasonHandler.getCurrentSeason();
        if (season == null) {
            return originalColor;
        }
        return season.getFoliageColor(biome);
    }

    public static int updateColorGrass(BiomeGenBase biome, int originalColor) {
        Season season = ClientSeasonHandler.getCurrentSeason();
        if (season == null) {
            return originalColor;
        }
        return season.getGrassColor(biome);
    }
}
