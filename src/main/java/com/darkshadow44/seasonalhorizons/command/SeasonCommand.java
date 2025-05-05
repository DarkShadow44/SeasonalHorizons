package com.darkshadow44.seasonalhorizons.command;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Optional;

public class SeasonCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "season";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("Available subcommands:"));
            sender.addChatMessage(new ChatComponentText(" set <season name>"));
        } else if (args[0].equals("set")) {
            Optional<Season> season = Optional.empty();
            if (args.length > 1) {
                season = SeasonHandlerServer.getSeasonById(args[1]);
            }

            if (!season.isPresent()) {
                sender.addChatMessage(new ChatComponentText("Available seasons:"));
                sender.addChatMessage(new ChatComponentText(String.join(" ", SeasonHandlerServer.getSeasonIds())));
                return;
            }

            SeasonHandlerServer.setSeasonForWorld(sender.getEntityWorld(), season.get());
        } else {
            sender.addChatMessage(new ChatComponentText("Available subcommands:"));
            sender.addChatMessage(new ChatComponentText(" set <season name>"));
        }
    }
}
