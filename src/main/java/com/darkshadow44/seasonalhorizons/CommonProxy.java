package com.darkshadow44.seasonalhorizons;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

import com.darkshadow44.seasonalhorizons.network.NetworkHandler;
import com.darkshadow44.seasonalhorizons.save.AdditionalChunkData;
import com.darkshadow44.seasonalhorizons.save.IMixinChunk;
import com.darkshadow44.seasonalhorizons.save.IMixinWorldServer;
import com.darkshadow44.seasonalhorizons.season.SnowHandler;
import com.falsepattern.chunk.api.DataRegistry;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        NetworkHandler.register();
    }

    public void init(FMLInitializationEvent event) {
        DataRegistry.registerDataManager(new AdditionalChunkData(), 0);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        NetworkHandler.sendSeasonUpdate(event.player.worldObj);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.sendSeasonUpdate(event.player.worldObj);
    }

    @SubscribeEvent
    public void onWorldSave(ChunkEvent.Unload event) {
        if (event.world.isRemote) {
            return;
        }
        IMixinChunk mixinChunk = (IMixinChunk) event.getChunk();
        mixinChunk.seasonalHorizons$setLastSaveTime(event.world.getTotalWorldTime());
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.world.isRemote) {
            return;
        }

        IMixinWorldServer mixinWorldServer = (IMixinWorldServer) event.world;
        SnowHandler snowHandler = mixinWorldServer.seasonalHorizons$getSnowHandler();
        IMixinChunk mixinChunk = (IMixinChunk) event.getChunk();

        snowHandler.processChunk(event.getChunk(), mixinChunk.seasonalHorizons$getLastSaveTime());
    }
}
