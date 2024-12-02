package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Commands.*;
import edu.Kennesaw.ksumcspeedrun.Events.*;
import edu.Kennesaw.ksumcspeedrun.FileIO.Config;
import edu.Kennesaw.ksumcspeedrun.FileIO.Logger;
import edu.Kennesaw.ksumcspeedrun.Utilities.Messages;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/* The Main class will be passed to most instances of any other class, so that the Config and Speedrun instances
   can be accessed from any class */
public class Main extends JavaPlugin {

    /* The main class holds the main instance of the config. During runtime, the config is always
       accessed through the main class. */
    private Config config;

    // ^^ Likewise for "Speedrun" and "Messages"
    private Speedrun speedrun;
    private Messages messages;

    /* If a spawnpoint and specified and enabled in the config, it will be stored
       here on startup - not fetched during runtime */
    private Location spawnPoint = null;

    // Game Rules specified in the config.yml are stored here. Values are either Integers or Booleans.
    private Map<GameRule<?>, Object> gameRules;

    // Initial Startup Functionalities: This is what the plugin does first.
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {

        // All uncaught errors thrown on the main thread should be logged
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logError(throwable, thread);
        });

        // The Main & Only Config Instance is Initialized
        config = new Config(this);

        // The Main & Only Speedrun Instance is Initialized
        speedrun = new Speedrun(this);

        // Events are Registered
        Bukkit.getPluginManager().registerEvents(new EntityDeath(this), this);
        Bukkit.getPluginManager().registerEvents(new MineBlock(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemObtain(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerClick(this), this);
        Bukkit.getPluginManager().registerEvents(new BedInteract(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new PortalEvent(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeave(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemDrop(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemPickup(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawn(this), this);


        //getLogger().info("Playtime tracker enabled");
        //Bukkit.getServer().getPluginManager().registerEvents(new PlayTimeTracker(this), this);

        // The Main & Only Messages Instance is Initialized - Config must be FULLY loaded before this line is reached
        messages = new Messages(this);

        // All Commands are Registered
        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("speedrun", "Main command for KSU-MC-Speedrun", new CommandSpeedrun(this));
            commands.register("objectives", "Display a list of objectives to players", new CommandObjectives(this));
            commands.register("team", "Alternative to using Team GUI to join a team.", new CommandTeam(this));
            commands.register("help", "Default help message for KSU-MC-Speedrun", new CommandHelp(this));
            commands.register("scoreboard", "Toggle scoreboard enabled or disabled", new CommandScoreboard(this));
            commands.register("spawn", "Teleport to your team's spawnpoint", new CommandSpawn(this));
        });

        // If the spawn point is set & enabled in the config, we parse the data and save it to access when players join
        if (config.getBoolean("world.spawnPoint.enabled")) {
            double y = 100.0;
            spawnPoint = new Location(Bukkit.getWorld(config.getString("world.spawnPoint.world")),
                    config.getDouble("world.spawnPoint.x"), y,
                    config.getDouble("world.spawnPoint.z"), config.getDouble("world.spawnPoint.pitch").floatValue(),
                    config.getDouble("world.spawnPoint.yaw").floatValue());
            if (config.get("world.spawnPoint.y") instanceof String) {
                spawnPoint.setY(spawnPoint.getWorld().getHighestBlockYAt(spawnPoint));
            } else if (config.get("world.spawnPoint.y") instanceof Double) {
                spawnPoint.setY(config.getDouble("world.spawnPoint.y"));
            }
        }

        // Load game rules that are set in the config, if applicable.
        loadGameRules();

    }
    // Runs when plugin is disabled
    @Override
    public void onDisable() {
        // Plugin shutdown logic.
        getLogger().info("PlayTimeTracker has been disabled.");
    }

    // Getter for access to plugin Config file, can be accessed by any class that passes Main instance in constructor
    public Config getSpeedrunConfig() {
        return config;
    }

    // Getter for Speedrun instance, can be accessed by any class that passes Main instance in constructor
    public Speedrun getSpeedrun() {
        return speedrun;
    }

    // Getter for Messages instance, can be accessed by any class that passes Main instance in constructor
    public Messages getMessages() { return messages; }

    // Getter for spawn point Location, if set & enabled in the Config
    public Location getSpawnPoint() {
        return spawnPoint;
    }

    /* All Async tasks are run through this function - the purpose is so that uncaught errors thrown on an asynchronous
       thread can still be logged - it doesn't always work, however. Feel free to update & pull request */
    public void runAsyncTask(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(this, scheduledTask -> {
            try {
                task.run();
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(this, () -> logError(e, Thread.currentThread()));
            }
        });
    }

    /* Runs a task asynchronously and returns a CompletableFuture that is completed when the task finishes execution.
       This can be useful for asynchronous operations that need to signal their completion or handle errors.
       The implementations of this were removed so this function is unused. Could be handy
       in the future - or can be removed. */
    public CompletableFuture<Void> runCompletableFutureAsyncTask(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getAsyncScheduler().runNow(this, scheduledTask -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(this, () -> logError(e, Thread.currentThread()));
                future.completeExceptionally(null);
            }
        });
        return future;
    }

    // Run an Async Delayed Task & log any errors that are thrown
    public ScheduledTask runAsyncDelayed(Runnable task, long delay, TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runDelayed(this, scheduledTask -> {
            try {
                task.run();
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(this, () -> logError(e, Thread.currentThread()));
            }
        }, delay, unit);
    }

    // Getter method for GameRules map
    public Map<GameRule<?>, Object> getGameRules() {
        return gameRules;
    }

    // Broadcast messages asynchronously to all players
    ///  @param permission - Only players w/ this permission can see the message, put "" for all players
    public void asyncBroadcast(Component message, String permission) {
        runAsyncTask(() -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission(permission) || permission.isEmpty()) p.sendMessage(message);
            }
        });
    }

    // Log error using edu.Kennesaw.ksumcspeedrun.FileIO.Logger - saves error log to plugin file
    private void logError(Throwable throwable, Thread thread) {
        Logger.logError("Unhandled exception in thread " + thread.getName() + ": " +
                throwable.getMessage(), throwable, this);
        getLogger().warning("Saving error to errorlogs...");
    }

    // If gamerules are enabled in the config.yml, gamerules will be updated in-game
    // according to their setting in the config:
    // For boolean Game Rules:
    // - Can be set to their boolean value (supports both string format & boolean format) or "default"
    // - Any other setting will not change the game rule; it will be left as is
    // For integer Game Rules:
    // - Must be assigned integer value or "default"
    // - Any other setting will not change the game rule; it will be left as is
    // If game rule setting is unexpected, a warning will print in the console on plugin initialization
    private void loadGameRules() {
        gameRules = new LinkedHashMap<>();
        for (String gameRule : config.getConfigurationSection("gameRules").getKeys(false)) {
            if (gameRule.equalsIgnoreCase("enabled")) continue;
            GameRule<?> rule = GameRule.getByName(gameRule);
            if (rule != null) {
                Object value = config.get("gameRules." + gameRule);
                if (rule.getType() == Boolean.class) {
                    if (value instanceof Boolean val) {
                        gameRules.put(rule, val);
                    } else if (value instanceof String val) {
                        if (val.equalsIgnoreCase("default") ||
                                (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false"))) {
                            continue;
                        }
                        gameRules.put(rule, Boolean.parseBoolean(val));
                    } else {
                        getLogger().warning("Invalid Configuration Setting: '" + gameRule + "' set to '" + value + "'");
                        getLogger().warning("Expected Type: Boolean");
                    }
                } else if (rule.getType() == Integer.class) {
                    if (value instanceof Integer val) {
                        gameRules.put(rule, val);
                    } else {
                        if ((value instanceof String val) && val.equalsIgnoreCase("default")) {
                            continue;
                        }
                        getLogger().warning("Invalid Configuration Setting: '" + gameRule + "' set to '" + value + "'");
                        getLogger().warning("Expected Type: Integer");
                    }
                }
            }
        }
    }
}

