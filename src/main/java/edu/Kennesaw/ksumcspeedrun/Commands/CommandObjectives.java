package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandObjectives implements BasicCommand {

    Main plugin;

    public CommandObjectives(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        CommandSender sender = commandSourceStack.getSender();

        if (sender instanceof Player p) {

            Team team = null;

            for (Team teamLoop : plugin.getSpeedrun().getTeams().getTeams()) {
                if (teamLoop != null) {
                    if (teamLoop.getPlayers().contains(p)) {
                        team = teamLoop;
                    }
                }
            }

            if (team != null) {
                p.openBook(Items.getObjectiveBook(team));
            }
        }
    }
}
