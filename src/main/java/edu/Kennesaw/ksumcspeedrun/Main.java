package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Commands.CommandHelp;
import edu.Kennesaw.ksumcspeedrun.Commands.CommandObjectives;
import edu.Kennesaw.ksumcspeedrun.Commands.CommandSpeedrun;
import edu.Kennesaw.ksumcspeedrun.Commands.CommandTeam;
import edu.Kennesaw.ksumcspeedrun.Events.*;
import edu.Kennesaw.ksumcspeedrun.FileIO.Config;
import edu.Kennesaw.ksumcspeedrun.Utilities.Messages;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/* The Main class will be passed to most instances of any other class, so that the Config and Speedrun instances
   can be accessed from any class */
public class Main extends JavaPlugin {

    private Config config;
    private Speedrun speedrun;
    private Messages messages;
    private Location spawnPoint;

    // The following method runs when the plugin is Enabled
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {

        // Config instance is initialized
        config = new Config(this);

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


        getLogger().info("Playtime tracker enabled");
        Bukkit.getServer().getPluginManager().registerEvents(new PlayTimeTracker(this), this);

        messages = new Messages(this);

        // Speedrun Command is Registered
        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("speedrun", "Main command for KSU-MC-Speedrun", new CommandSpeedrun(this));
            commands.register("objectives", "Display a list of objectives to players", new CommandObjectives(this));
            commands.register("team", "Alternative to using Team GUI to join a team.", new CommandTeam(this));
            commands.register("help", "Default help message for KSU-MC-Speedrun", new CommandHelp(this));
        });

        spawnPoint = new Location(Bukkit.getWorld(config.getString("world.spawnPoint.world")),
                config.getDouble("world.spawnPoint.x"), config.getDouble("world.spawnPoint.y"),
                config.getDouble("world.spawnPoint.z"), config.getDouble("world.spawnPoint.pitch").floatValue(),
                config.getDouble("world.spawnPoint.yaw").floatValue());

    }
    // Runs when plugin is disabled
    @Override
    public void onDisable() {
        // Plugin shutdown logic.
        getLogger().info("PlayTimeTracker has been disabled.");
    }

    // Getter for Plugin Config file, can be accessed by any class that passes Main instance in constructor
    public Config getSpeedrunConfig() {
        return config;
    }

    // Getter for Speedrun object, can be accessed by any class that passes Main instance in constructor
    public Speedrun getSpeedrun() {
        return speedrun;
    }

    public Messages getMessages() { return messages; }

    public Location getSpawnPoint() {
        return spawnPoint;
    }
}

