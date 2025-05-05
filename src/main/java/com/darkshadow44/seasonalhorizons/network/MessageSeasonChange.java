package com.darkshadow44.seasonalhorizons.network;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerClient;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class MessageSeasonChange implements IMessage, IMessageHandler<MessageSeasonChange, IMessage> {
    public int season;
    public int dimension;

    public MessageSeasonChange() {
    }

    public MessageSeasonChange(World world, Season season) {
        this.dimension = world.provider.dimensionId;
        this.season = season.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        season = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(season);
    }

    @Override
    public IMessage onMessage(MessageSeasonChange message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            if (Minecraft.getMinecraft().thePlayer.dimension == dimension) {
                Season season = Season.values()[message.season];
                SeasonHandlerClient.updateSeason(season);
            }
        }

        return null;
    }
}
