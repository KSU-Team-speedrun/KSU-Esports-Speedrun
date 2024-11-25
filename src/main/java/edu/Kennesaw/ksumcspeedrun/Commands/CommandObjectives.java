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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * The CommandObjectives class implements the BasicCommand interface and is responsible
 * for handling the "objectives" command in the plugin.
 *
 * This class facilitates the following:
 * - Opens a book for the player that lists objectives.
 * - Determines the type of book to open (all objectives, incomplete, or complete objectives).
 * - Grants special permissions to admin users.
 *
 * Usage of the class involves the following:
 * - Checking if the command sender is a player.
 * - Fetching the team the player belongs to.
 * - Opening a book with relevant objectives for the player.
 * - Providing an admin book if the player has the required permissions for admin actions.
 */
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

            // Objectives are specific to teams, so the team must be determined
            Team team = tm.getTeam(p);

            if (team != null) {

                /* Unless specified as "incomplete" or "complete", the main objective book opens when users
                   can make their selection */
                Book book = Items.getObjectiveBookMain();;
                if (args.length == 1) {

                    // Open incomplete/complete book if specified
                    if (args[0].equalsIgnoreCase("incomplete")) {
                        book = Items.getObjectiveBook(team, plugin.getSpeedrun().isWeighted(), true);
                    } else if (args[0].equalsIgnoreCase("complete")) {
                        book = Items.getObjectiveBook(team, plugin.getSpeedrun().isWeighted(), false);
                    }

                }

                p.openBook(book);

            } else {

                // If the player is an admin, the admin book is opened which contains the objective number
                if ((!plugin.getSpeedrun().isParticipating(p) || !plugin.getSpeedrun().isStarted()) && p.hasPermission("ksu.speedrun.admin")) {
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
