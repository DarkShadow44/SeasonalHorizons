package com.darkshadow44.seasonalhorizons.season;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.network.NetworkHandler;
import com.darkshadow44.seasonalhorizons.save.IMixinWorldServer;
import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;

public class SeasonHandler {

    public static String[] getSeasonIds() {
        return Arrays.stream(Season.values())
            .map(Season::getId)
            .toArray(String[]::new);
    }

    public static Optional<Season> getSeasonById(String id) {
        return Arrays.stream(Season.values())
            .filter(
                x -> x.getId()
                    .equals(id))
            .findAny();
    }

    public static void setSeasonForWorld(World world, Season season) {
        if (!(world instanceof WorldServer)) {
            throw new RuntimeException("Failed to set season for world type: " + world.getClass());
        }

        SeasonWorldData seasonWorldData = ((IMixinWorldServer) world).seasonalHorizons$getSeasonWorldData();

        if (seasonWorldData == null) {
            return;
        }
        // Start the subseason from the beginning, also when setting the current one again
        seasonWorldData.seasonTicks = 0;
        if (seasonWorldData.season == season) {
            return;
        }
        // Takes effect with the next tick's season time
        seasonWorldData.changeSeason(season, seasonWorldData.seasonTime + 1);
        NetworkHandler.sendSeasonUpdate(world);
    }

    public static Season getSeasonForWorld(World world) {
        if (world instanceof WorldServer) {
            SeasonWorldData seasonWorldData = ((IMixinWorldServer) world).seasonalHorizons$getSeasonWorldData();
            if (seasonWorldData == null) {
                return null;
            }
            return seasonWorldData.season;
        }

        if (world.isRemote) {
            return ClientSeasonHandler.getCurrentSeason();
        }

        // Unknown world types (e.g. fake worlds from other mods) have no season
        return null;
    }

    public static float getAdjustedTemperature(World world, BiomeGenBase biome, int x, int y, int z) {
        Season season = getSeasonForWorld(world);
        if (season == null) {
            return biome.getFloatTemperature(x, y, z);
        }
        return season.getAdjustedTemperatureFloat(biome, x, y, z);
    }
}
