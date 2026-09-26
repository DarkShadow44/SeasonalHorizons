package com.darkshadow44.seasonalhorizons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import com.darkshadow44.seasonalhorizons.season.ClientSeasonHandler;
import com.darkshadow44.seasonalhorizons.season.Season;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public class MessageSeasonChange implements IMessage, IMessageHandler<MessageSeasonChange, IMessage> {

    public int season;
    public int dimension;

    public MessageSeasonChange() {}

    public MessageSeasonChange(World world, Season season) {
        this.dimension = world.provider.dimensionId;
        this.season = season == null ? -1 : season.ordinal();
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
            if (Minecraft.getMinecraft().thePlayer.dimension == message.dimension) {
                Season[] seasons = Season.values();
                // Ignore out-of-range values, e.g. from a mismatched server; -1 means no season
                if (message.season < -1 || message.season >= seasons.length) {
                    return null;
                }
                Season season = message.season < 0 ? null : seasons[message.season];
                ClientSeasonHandler.updateSeason(season);
            }
        }

        return null;
    }
}
