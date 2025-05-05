package com.darkshadow44.seasonalhorizons;

import com.darkshadow44.seasonalhorizons.command.SeasonCommand;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.event.terraingen.BiomeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;

@Mod(modid = SeasonalHorizons.MODID, version = Tags.VERSION, name = "SeasonalHorizons", acceptedMinecraftVersions = "[1.7.10]")
public class SeasonalHorizons {

    public static final String MODID = "seasonalhorizons";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "com.darkshadow44.seasonalhorizons.ClientProxy", serverSide = "com.darkshadow44.seasonalhorizons.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.initialize(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new SeasonCommand());
    }
}
