package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The CommandTeam class implements the BasicCommand interface and is used as an alternative to the GUI
 * for joining specific teams.
 */

public class CommandTeam implements BasicCommand {

    Main plugin;
    TeamManager tm;
    Speedrun speedrun;

    public CommandTeam(Main plugin) {
        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        this.tm = speedrun.getTeams();
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        // Only players can join teams
        if (commandSourceStack.getSender() instanceof Player p) {

            // Cannot change teams if the speedrun is started
            if (speedrun.isStarted()) {
                p.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                return;
            }

            // Cannot change teams if they're disabled
            if (!speedrun.getTeamsEnabled()) {
                p.sendMessage(plugin.getMessages().getTeamsNotEnabled());
                return;
            }

            // If teams are enabled, then tm.getTeams() are all instances of TrueTeams (more than one player)
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());

            // Team must be specified
            if (args.length != 1) {

                p.sendMessage(plugin.getMessages().getTeamHelp());

            } else {

                // Players cannot change teams if they have already changed team recently (if they are in cooldown)
                if (speedrun.getTeamCooldown().contains(p)) {
                    p.sendMessage(plugin.getMessages().getTeamCooldownMessage());
                    return;
                }

                for (TrueTeam trueTeam : trueTeams) {

                    // If the argument is equal to the team name (replacing space w/ _)... continue
                    if (args[0].equalsIgnoreCase(trueTeam.getStrippedName().replace(' ', '_'))) {

                        // If the team is full then the player cannot join
                        if (trueTeam.isFull()) {
                            p.sendMessage(plugin.getMessages().getTeamIsFull());
                            return;
                        }

                        // Get the player's current team (if applicable)
                        TrueTeam oldTrueTeam = (TrueTeam) tm.getTeam(p);

                        // A player cannot join a team again if they are already on that team
                        if (oldTrueTeam != null) {

                            if (oldTrueTeam.equals(trueTeam)) {
                                p.sendMessage(plugin.getMessages().getAlreadyOnTeam());
                                return;
                            }

                            oldTrueTeam.removePlayer(p);
                            tm.getTeamInventory().updateTeamInventory(oldTrueTeam);

                        } else if (!speedrun.isParticipating(p)) {

                            /* If the player is not participating (admin), also set them as participating so they
                               are included in all calculations */
                            speedrun.participate(p);

                        }

                        // Finally add the player to the new team and update the inventory UI to reflect this change
                        trueTeam.addPlayer(p);
                        tm.getTeamInventory().updateTeamInventory(trueTeam);

                        speedrun.addTeamCooldown(p);

                        return;

                    }

                }

                p.sendMessage(plugin.getMessages().getTeamNotFound(args[0]));

            }

        }

    }

    // Add all existing teams to command suggestions so players know what teams they can choose from
    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            for (Team team : tm.getTeams()) {
                if (team instanceof TrueTeam) {
                    suggestions.add(team.getStrippedName().replace(' ', '_'));
                }
            }
        } else if (args.length == 1) {
            addMatchingSuggestions(suggestions, args[0], tm.getStrippedTeamNames(true));
        }
        return suggestions;
    }

    // This method makes it easier to add multiple possible suggestions to a list suggestion list & match them
    private void addMatchingSuggestions(List<String> suggestions, String arg, List<String> teamNames) {
        for (String suggestion : teamNames) {
            if (suggestion.toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
