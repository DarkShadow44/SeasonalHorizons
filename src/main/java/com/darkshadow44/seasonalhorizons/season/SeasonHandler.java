package com.darkshadow44.seasonalhorizons.season;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

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

    private static Season currentSeasonClient = Season.SPRING_EARLY;

    public static void updateClientSeason(Season season) {
        if (season == currentSeasonClient) {
            return;
        }
        currentSeasonClient = season;
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    public static Season getCurrentClientSeason() {
        return currentSeasonClient;
    }

    public static void setSeasonForWorld(World world, Season season) {
        if (!(world instanceof WorldServer)) {
            throw new RuntimeException("Failed to set season for world type: " + world.getClass());
        }

        SeasonWorldData seasonWorldData = ((IMixinWorldServer) world).seasonalHorizons$getSeasonWorldData();

        if (seasonWorldData == null || seasonWorldData.season == season) {
            return;
        }
        seasonWorldData.season = season;
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

        if (world instanceof WorldClient) {
            return currentSeasonClient;
        }

        throw new RuntimeException("Failed to get season for world type: " + world.getClass());
    }
}
