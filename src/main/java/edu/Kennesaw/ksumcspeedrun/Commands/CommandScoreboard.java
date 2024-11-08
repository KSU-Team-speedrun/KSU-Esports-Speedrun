package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CommandScoreboard implements BasicCommand {

    Main plugin;

    public CommandScoreboard(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        if (commandSourceStack.getSender() instanceof Player p) {

            boolean isEnabled = plugin.getSpeedrun().toggleScoreboard(p);
            if (!isEnabled) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }

            p.sendMessage(plugin.getMessages().getToggleScoreboard(isEnabled));

        }

    }

    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
