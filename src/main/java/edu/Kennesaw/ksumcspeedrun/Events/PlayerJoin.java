package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Event listener class that handles player join events.
 */
public class PlayerJoin implements Listener {

    Main plugin;

    public PlayerJoin(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        Speedrun sr = plugin.getSpeedrun();

        // If the speedrun is not started...
        if (!sr.isStarted()) {

            // Teleport the player to the plugin spawn point (if not null) & clear their inventory
            Location spawnPoint = plugin.getSpawnPoint();
            p.getInventory().clear();

            if (spawnPoint != null) p.teleport(spawnPoint);

            /* If the player is not an admin, also set their participation as true and ensure they're in survival
               Also set health & hunger to maximum */
            if (!p.hasPermission("ksu.speedrun.admin")) {
                p.setGameMode(GameMode.SURVIVAL);
                sr.participate(p);
                p.setHealth(20.0);
                p.setFoodLevel(20);
            }

        } else {

            // Ensure player gets added back to team if they were removed
            Team team = plugin.getOfflineParticipantTeam(p.getUniqueId());

            if (team != null) {

                if (!sr.isParticipating(p)) {
                    sr.participate(p);
                }

                if (team instanceof TrueTeam trueTeam) {

                    if (!trueTeam.containsPlayer(p)) {
                        trueTeam.addPlayer(p);
                    }

                } else {

                    /* Player's shouldn't lose their soloteam, but this hasn't been tested so this is a fallback
                       safety implementation */
                    SoloTeam st = (SoloTeam) team;
                    if (!st.getPlayer().equals(p) && st.getPlayerBackup().equals(p)) {
                        SoloTeam newSt = new SoloTeam(plugin, p);
                        for (Objective co : st.getCompleteObjectives()) {
                            newSt.addCompleteObjective(co);
                            co.setComplete(newSt);
                        }
                        for (Objective io : st.getIncompleteObjectives()) {
                            io.addTeam(newSt);
                        }
                        sr.getTeams().removePlayerTeam(p);
                        sr.remTeam(st);
                        sr.addTeam(newSt);
                        sr.getTeams().setPlayerTeam(p, newSt);
                    }
                }

                org.bukkit.scoreboard.Scoreboard teamScoreboard = plugin.getSpeedrun().getScoreboard().getTeamScoreboard(team);
                if (teamScoreboard != null) {
                    p.setScoreboard(teamScoreboard);
                }

            }
        }
    }
}

