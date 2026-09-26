package com.darkshadow44.seasonalhorizons.season;

import net.minecraft.client.Minecraft;

import com.darkshadow44.seasonalhorizons.mixin.client.AccessorForgeHooksClient;

// Client-only; must not be referenced from code that runs on a dedicated server
public class ClientSeasonHandler {

    // Null when the client is in a dimension without seasons
    private static Season currentSeason;

    public static void updateSeason(Season season) {
        if (season == currentSeason) {
            return;
        }
        currentSeason = season;
        AccessorForgeHooksClient.setSkyInit(false);
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    public static Season getCurrentSeason() {
        return currentSeason;
    }
}
