package com.darkshadow44.seasonalhorizons.api;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

/** Temperature queries for integrations with Seasonal Horizons. */
public final class SeasonsAPI {

    private SeasonsAPI() {}

    /**
     * Returns the biome's base temperature adjusted for the world's current season.
     * This overload does not include altitude; use the position overload for weather and snow.
     * In a dimension without seasons, returns {@code biome.temperature}.
     */
    public static float getTemperature(World world, BiomeGenBase biome) {
        Season season = SeasonHandler.getSeasonForWorld(world);
        return season == null ? biome.temperature : season.getAdjustedTemperature(biome.temperature);
    }

    /**
     * Returns the biome temperature at a block position, including altitude and the world's current season.
     * In a dimension without seasons, returns {@link BiomeGenBase#getFloatTemperature(int, int, int)}.
     * For a precipitation column, pass its surface or precipitation height as {@code y}.
     */
    public static float getTemperature(World world, BiomeGenBase biome, int x, int y, int z) {
        return SeasonHandler.getAdjustedTemperature(world, biome, x, y, z);
    }
}
