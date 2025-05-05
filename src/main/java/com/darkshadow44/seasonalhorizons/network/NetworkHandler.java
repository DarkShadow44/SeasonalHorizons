package com.darkshadow44.seasonalhorizons.network;

import com.darkshadow44.seasonalhorizons.SeasonalHorizons;
import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerServer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;

public class NetworkHandler {
    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(SeasonalHorizons.MODID);

    public static void register() {
        channel.registerMessage(MessageSeasonChange.class, MessageSeasonChange.class, 10, Side.CLIENT);
    }

    public static void sendSeasonUpdate(World world) {
        Season season = SeasonHandlerServer.getSeasonForWorld(world);
        channel.sendToAll(new MessageSeasonChange(world, season));
    }
}
