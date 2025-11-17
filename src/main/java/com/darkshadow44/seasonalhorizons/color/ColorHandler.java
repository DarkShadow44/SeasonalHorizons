package com.darkshadow44.seasonalhorizons.color;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerClient;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerServer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.awt.*;

public class ColorHandler {
    public static int updateColorFoliage(BiomeGenBase biome, int originalColor) {
        Season season = SeasonHandlerClient.getCurrentSeason();
        return season.getFoliageColor(biome);
    }

    public static int updateColorGrass(BiomeGenBase biome, int originalColor) {
        Season season = SeasonHandlerClient.getCurrentSeason();
        return season.getGrassColor(biome);
    }
}
