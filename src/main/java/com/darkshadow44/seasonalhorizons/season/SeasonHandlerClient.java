package com.darkshadow44.seasonalhorizons.season;

import net.minecraft.client.Minecraft;

public class SeasonHandlerClient {

    private static Season currentSeason = Season.SPRING_EARLY;

    public static void updateSeason(Season season) {
        if (season == currentSeason) {
            return;
        }
        currentSeason = season;
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    public static Season getCurrentSeason() {
        return currentSeason;
    }
}
