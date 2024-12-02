package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
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

        // If game has not yet started,
        if (!sr.isStarted()) {

            // If player was participating, toggle participation off
            if (sr.isParticipating(p)) {
                sr.participate(p);
            }

        } else {

            // If game has started & player is participating,
            if (sr.isParticipating(p)) {
                // Keep track of player in offline participants mapping
                plugin.addOfflineParticipant(p.getUniqueId(), sr.getTeams().getTeam(p));

            }

        }

    }

}
