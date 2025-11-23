package com.darkshadow44.seasonalhorizons.color;

import java.awt.*;

import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

public class ColorHandler {

    public static int updateColorFoliage(BiomeGenBase biome, int originalColor) {
        Season season = SeasonHandler.getCurrentClientSeason();
        return season.getFoliageColor(biome);
    }

    public static int updateColorGrass(BiomeGenBase biome, int originalColor) {
        Season season = SeasonHandler.getCurrentClientSeason();
        return season.getGrassColor(biome);
    }
}
