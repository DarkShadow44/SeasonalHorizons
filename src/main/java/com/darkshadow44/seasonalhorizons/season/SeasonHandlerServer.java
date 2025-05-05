package com.darkshadow44.seasonalhorizons.season;

import com.darkshadow44.seasonalhorizons.network.NetworkHandler;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Optional;

public class SeasonHandlerServer {
    public static String[] getSeasonIds() {
        return Arrays.stream(Season.values()).map(Season::getId).toArray(String[]::new);
    }

    public static Optional<Season> getSeasonById(String id) {
        return Arrays.stream(Season.values()).filter(x -> x.getId().equals(id)).findAny();
    }

    private static Season currentSeason = Season.AUTUMN_EARLY;

    public static void setSeasonForWorld(World world, Season season) {
        if (currentSeason == season) {
            return;
        }
        currentSeason = season;
        NetworkHandler.sendSeasonUpdate(world);
    }

    public static Season getSeasonForWorld(World world) {
        return currentSeason;
    }
}
