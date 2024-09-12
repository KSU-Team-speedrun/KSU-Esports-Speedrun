package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Commands.CommandSpeedrun;
import edu.Kennesaw.ksumcspeedrun.Events.EntityDeath;
import edu.Kennesaw.ksumcspeedrun.Events.ItemObtain;
import edu.Kennesaw.ksumcspeedrun.Events.MineBlock;
import edu.Kennesaw.ksumcspeedrun.Events.PlayerMove;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private Config config;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        config = new Config(this);

        Bukkit.getPluginManager().registerEvents(new EntityDeath(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMove(this), this);
        Bukkit.getPluginManager().registerEvents(new MineBlock(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemObtain(this), this);

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("speedrun", "Main command for KSU-MC-Speedrun", new CommandSpeedrun(this));
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic.
    }

    public Config getSpeedrunConfig() {
        return config;
    }
}

