package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

    Main plugin;

    public PlayerLeave(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Speedrun sr = plugin.getSpeedrun();
        Player p = e.getPlayer();
        if (!sr.getTeamsEnabled()) {
            if (sr.getSoloPlayers().contains((SoloTeam) p)) {
                sr.removeSoloPlayer((SoloTeam) p);
            }
        }
        Team team = sr.getTeams().getTeam(p);
        if (team != null) {
            team.removePlayer(p);
        }
        if (!sr.isStarted()) {
            if (Bukkit.getServer().getOnlinePlayers().size() % sr.getTeamSizeLimit() == 0 || sr.getTeams()
                    .getTeamInventory().getInventory() == null) {
                if (sr.getTeamsEnabled()) sr.createTeams(null);
            } else {
                sr.getTeams().getTeamInventory().updateTeamInventory(team);
            }
        }
    }
}
