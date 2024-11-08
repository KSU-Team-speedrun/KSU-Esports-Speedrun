package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawn implements Listener {

    Main plugin;
    Speedrun speedrun;
    TeamManager tm;

    public PlayerRespawn(Main plugin) {
        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        this.tm = speedrun.getTeams();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        if (speedrun.isStarted()) {

            Player p = event.getPlayer();
            Team team = tm.getTeam(p);

            if (team != null) {

                Location respawnLocation = team.getRespawnLocation();

                if (respawnLocation != null) {

                    event.setRespawnLocation(respawnLocation);

                }

            }

        }

    }

}
