package edu.Kennesaw.ksumcspeedrun.Events;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import edu.Kennesaw.ksumcspeedrun.Main;

import java.util.UUID;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayTimeTracker implements Listener {

    private final HashMap<UUID, Long> loginTimes = new HashMap<>();

    Main plugin;

    private final HashMap<UUID, Long> totalPlayTimes = new HashMap<>();

    public PlayTimeTracker(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Record the login time
        UUID playerId = event.getPlayer().getUniqueId();
        loginTimes.put(playerId, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Calculate the session time and update total playtime
        UUID playerId = event.getPlayer().getUniqueId();
        Long loginTime = loginTimes.remove(playerId);

        if (loginTime != null) {
            long logoutTime = System.currentTimeMillis();
            long sessionDuration = logoutTime - loginTime; // in milliseconds

            // Update the total playtime
            this.addPlayTime(playerId, sessionDuration);
        }
    }

    public void addPlayTime(UUID playerId, long playTime) {
        totalPlayTimes.put(playerId, totalPlayTimes.getOrDefault(playerId, 0L) + playTime);
        savePlayTime(playerId);
    }
    
    public long getPlayTime(UUID playerId) {
        return totalPlayTimes.getOrDefault(playerId, 0L);
    }

    public void savePlayTime(UUID playerId) {
        File file = new File(plugin.getDataFolder(), "playtimes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
    
        config.set(playerId.toString(), getPlayTime(playerId));
    
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public void loadPlayTime(UUID playerId) {
        File file = new File(plugin.getDataFolder(), "playtimes.yml");
        if (!file.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        long playTime = config.getLong(playerId.toString(), 0L);
        totalPlayTimes.put(playerId, playTime);
    }
}