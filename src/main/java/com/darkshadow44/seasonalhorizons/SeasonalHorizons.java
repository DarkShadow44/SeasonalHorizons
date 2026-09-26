package com.darkshadow44.seasonalhorizons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.darkshadow44.seasonalhorizons.command.SeasonCommand;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = SeasonalHorizons.MODID,
    version = Tags.VERSION,
    name = "SeasonalHorizons",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:chunkapi")
public class SeasonalHorizons {

    public static final String MODID = "seasonalhorizons";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.darkshadow44.seasonalhorizons.ClientProxy",
        serverSide = "com.darkshadow44.seasonalhorizons.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void missingMappings(FMLMissingMappingsEvent event) {
        String leafPilePrefix = MODID + ":leaf_pile_";
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            // Leaf-pile blocks are generated from configured leaf blocks. If that leaf's mod or the config entry
            // is removed, the old pile is disposable and should become air instead of preventing the world from
            // loading.
            if (mapping.name.startsWith(leafPilePrefix)) {
                mapping.ignore();
            }
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new SeasonCommand());
    }
}
