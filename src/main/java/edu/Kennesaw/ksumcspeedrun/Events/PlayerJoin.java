package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    Main plugin;

    public PlayerJoin(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        Speedrun sr = plugin.getSpeedrun();

        if (!sr.isStarted()) {

            if (Bukkit.getServer().getOnlinePlayers().size() % sr.getTeamSizeLimit() == 0 || sr.getTeams()
                    .getTeamInventory().getInventory() == null) {
                sr.createTeams(null);
            }

            p.getInventory().setItem(4, Items.getTeamSelector());

        }

    }

}

