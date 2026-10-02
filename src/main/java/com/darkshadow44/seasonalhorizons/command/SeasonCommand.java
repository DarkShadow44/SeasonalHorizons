package com.darkshadow44.seasonalhorizons.command;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.darkshadow44.seasonalhorizons.Config;
import com.darkshadow44.seasonalhorizons.save.IMixinWorldServer;
import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;
import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

public class SeasonCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "season";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.seasonalhorizons.season.usage";
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "get", "set");
        }
        if (args.length == 2 && args[0].equals("set")) {
            return getListOfStringsMatchingLastWord(args, SeasonHandler.getSeasonIds());
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
        } else if (args[0].equals("get")) {
            sendCurrentSeason(sender);
        } else if (args[0].equals("set")) {
            Optional<Season> season = Optional.empty();
            if (args.length > 1) {
                season = SeasonHandler.getSeasonById(args[1]);
            }

            if (!season.isPresent()) {
                sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.available"));
                sender.addChatMessage(new ChatComponentText(String.join(" ", SeasonHandler.getSeasonIds())));
                return;
            }

            if (SeasonHandler.getSeasonForWorld(sender.getEntityWorld()) == null) {
                sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.noSeasons"));
                return;
            }

            if (Config.getLockedSeason() != null) {
                sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.locked"));
                return;
            }

            if (Config.isRealTimeSeasons()) {
                sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.realTime"));
                return;
            }

            SeasonHandler.setSeasonForWorld(sender.getEntityWorld(), season.get());
        } else {
            sendHelp(sender);
        }
    }

    private void sendHelp(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.subcommands"));
        sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.subcommand.get"));
        sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.subcommand.set"));
    }

    private void sendCurrentSeason(ICommandSender sender) {
        World world = sender.getEntityWorld();
        SeasonWorldData seasonWorldData = null;
        if (world instanceof WorldServer) {
            seasonWorldData = ((IMixinWorldServer) world).seasonalHorizons$getSeasonWorldData();
        }

        if (seasonWorldData == null) {
            sender.addChatMessage(new ChatComponentTranslation("commands.seasonalhorizons.season.noSeasons"));
            return;
        }

        if (Config.getLockedSeason() != null) {
            sender.addChatMessage(
                new ChatComponentTranslation(
                    "commands.seasonalhorizons.season.current.locked",
                    seasonWorldData.season.getId()));
            return;
        }

        if (Config.isRealTimeSeasons()) {
            sender.addChatMessage(
                new ChatComponentTranslation(
                    "commands.seasonalhorizons.season.current.realTime",
                    seasonWorldData.season.getId()));
            return;
        }

        // The subseason changes on the tick after seasonTicks reaches the subseason length
        int ticksLeft = Math.max(0, Config.getSubseasonLength() - seasonWorldData.seasonTicks);
        String daysLeft = String.format(Locale.ROOT, "%.1f", ticksLeft / 24000.0);
        sender.addChatMessage(
            new ChatComponentTranslation(
                "commands.seasonalhorizons.season.current",
                seasonWorldData.season.getId(),
                daysLeft));
    }
}
