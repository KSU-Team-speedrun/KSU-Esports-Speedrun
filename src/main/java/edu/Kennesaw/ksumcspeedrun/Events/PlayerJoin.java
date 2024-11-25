package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
                p.setFoodLevel(10);
            }

        }

    }

}

