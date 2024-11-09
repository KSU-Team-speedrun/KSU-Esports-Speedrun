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

        if (commandSourceStack.getSender() instanceof Player p) {

            if (speedrun.isStarted()) {
                p.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                return;
            }

            if (!speedrun.getTeamsEnabled()) {
                p.sendMessage(plugin.getMessages().getTeamsNotEnabled());
                return;
            }

            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());

            if (args.length != 1) {

                p.sendMessage(plugin.getMessages().getTeamHelp());

            } else {

                if (speedrun.getTeamCooldown().contains(p)) {
                    p.sendMessage(plugin.getMessages().getTeamCooldownMessage());
                    return;
                }

                for (TrueTeam trueTeam : trueTeams) {

                    if (args[0].equalsIgnoreCase(trueTeam.getStrippedName().replace(' ', '_'))) {

                        if (trueTeam.isFull()) {
                            p.sendMessage(plugin.getMessages().getTeamIsFull());
                            return;
                        }

                        TrueTeam oldTrueTeam = (TrueTeam) tm.getTeam(p);

                        if (oldTrueTeam != null) {

                            if (oldTrueTeam.equals(trueTeam)) {
                                p.sendMessage(plugin.getMessages().getAlreadyOnTeam());
                                return;
                            }

                            oldTrueTeam.removePlayer(p);
                            tm.getTeamInventory().updateTeamInventory(oldTrueTeam);
                        } else if (!speedrun.isParticipating(p)) {
                            speedrun.participate(p);
                        }

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
