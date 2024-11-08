package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class CommandSpawn implements BasicCommand {

    Main plugin;
    Speedrun sr;

    public CommandSpawn(Main plugin) {
        this.plugin = plugin;
        this.sr = plugin.getSpeedrun();
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        if (commandSourceStack.getSender() instanceof Player p) {

            if (sr.isStarted()) {

                Team team = sr.getTeams().getTeam(p);

                if (team != null) {

                    plugin.runAsyncTask(() -> {
                        if (sr.combatLog.containsValue(p) || sr.combatLog.containsKey(p.getUniqueId())) {
                            commandSourceStack.getSender().sendMessage(plugin.getMessages().getNoTeleportInCombat());
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () -> p.teleport(team.getRespawnLocation()));
                        }

                    });

                }

            }

        }

    }

    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
