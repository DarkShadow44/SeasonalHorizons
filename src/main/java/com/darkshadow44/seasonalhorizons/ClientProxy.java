package com.darkshadow44.seasonalhorizons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.event.terraingen.BiomeEvent;

import com.darkshadow44.seasonalhorizons.color.ColorHandler;
import com.darkshadow44.seasonalhorizons.color.ResourceReloadListener;
import com.darkshadow44.seasonalhorizons.season.ClientSeasonHandler;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager();
        resourceManager.registerReloadListener(new ResourceReloadListener());
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ClientSeasonHandler.reset();
    }

    @SubscribeEvent
    public void onFoliageColor(BiomeEvent.GetFoliageColor event) {
        event.newColor = ColorHandler.updateColorFoliage(event.biome, event.originalColor);
    }

    @SubscribeEvent
    public void onGrassColor(BiomeEvent.GetGrassColor event) {
        event.newColor = ColorHandler.updateColorGrass(event.biome, event.originalColor);
    }
}
