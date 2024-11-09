package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CommandHelp implements BasicCommand {

    Main plugin;

    public CommandHelp(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        if (commandSourceStack.getSender() instanceof Player p) {

            if (p.hasPermission("ksu.speedrun.admin") || p.isOp()) {

                if (args.length == 1) {

                    try {

                        int pageNum = Integer.parseInt(args[0]);

                        for (Component line : plugin.getMessages().getAdminHelpMessage(pageNum)) {
                            p.sendMessage(line);
                        }

                    } catch (NumberFormatException e) {

                        for (Component line : plugin.getMessages().getAdminHelpMessage(1)) {
                            p.sendMessage(line);
                        }

                    }

                } else {

                    for (Component line : plugin.getMessages().getAdminHelpMessage(1)) {
                        p.sendMessage(line);
                    }

                }

            } else {

                for (Component line : plugin.getMessages().getPlayerHelpMessage()) {
                    p.sendMessage(line);
                }

            }

        }

    }

    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
