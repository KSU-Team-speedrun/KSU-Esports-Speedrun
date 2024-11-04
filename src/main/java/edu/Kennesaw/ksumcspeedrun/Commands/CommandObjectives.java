package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.inventory.Book;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CommandObjectives implements BasicCommand {

    Main plugin;
    TeamManager tm;

    public CommandObjectives(Main plugin) {
        this.plugin = plugin;
        this.tm = plugin.getSpeedrun().getTeams();
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        CommandSender sender = commandSourceStack.getSender();

        if (sender instanceof Player p) {

            Team team = tm.getTeam(p);

            if (team != null) {
                Book book = Items.getObjectiveBook(team, plugin.getSpeedrun().isWeighted());
                p.openBook(book);
            } else {
                if (p.isOp()) {
                    p.openBook(Items.getAdminBook(plugin.getSpeedrun()));
                }
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
