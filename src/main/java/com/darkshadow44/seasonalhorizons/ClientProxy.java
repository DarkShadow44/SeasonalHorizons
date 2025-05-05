package com.darkshadow44.seasonalhorizons;

import com.darkshadow44.seasonalhorizons.color.ColorHandler;
import com.darkshadow44.seasonalhorizons.color.ResourceReloadListener;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void initialize(FMLPreInitializationEvent event) {
        super.initialize(event);
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        resourceManager.registerReloadListener(new ResourceReloadListener());
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
