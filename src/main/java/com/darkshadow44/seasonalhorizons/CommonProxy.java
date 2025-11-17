package com.darkshadow44.seasonalhorizons;

import net.minecraftforge.common.MinecraftForge;

import com.darkshadow44.seasonalhorizons.network.NetworkHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CommonProxy {

    public void initialize(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        NetworkHandler.sendSeasonUpdate(event.player.worldObj);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.sendSeasonUpdate(event.player.worldObj);
    }
}
