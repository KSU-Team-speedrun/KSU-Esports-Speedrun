package edu.Kennesaw.ksumcspeedrun;

import java.io.File;
import java.io.IOException;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

    Main plugin;

    private File file;
    private final YamlConfiguration config;

    public Config(Main plugin) {
        config = new YamlConfiguration();
        this.plugin = plugin;
        generateConfig();
    }

    public Component getComponent(String line) {
        if (config.contains(line)) {
            if (config.isString(line)) {
                return ComponentHelper.mmStringToComponent(config.getString(line));
            } else {
                plugin.getLogger().warning("Data on line \"" + line + "\" is not readable.");
                plugin.getLogger().warning("Data cannot be extracted from config.yml. Returning NULL..");
                return null;
            }
        } else {
            plugin.getLogger().warning("Config does not contain line \"" + line + "\"");
            plugin.getLogger().warning("Data cannot be extracted from config.yml. Returning NULL..");
            return null;
        }
    }

    public void set(String line, Component tc) {
        config.set(line, ComponentHelper.componentToMM(tc));
        save();
    }

    public void load() {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            try {
                config.load(file);
            } catch (IOException e) {
                plugin.getLogger().warning("An IOException has occurred when attempting to load config.yml!");
                plugin.getLogger().warning(e.getMessage());
            } catch (InvalidConfigurationException e) {
                Logger.logError("Your config.yml is configured in an invalid format! Please fix this problem or delete the config.yml for a default file.", e, plugin);
                plugin.getServer().shutdown();
                return;
            }
            Bukkit.getScheduler().runTask(plugin, this::addDefaults);
        });
    }

    private void save() {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            plugin.getLogger().info("Saving config.yml...");
            try {
                config.save(file);
                plugin.getLogger().info("Success saving config.yml!");
            } catch (IOException e) {
                plugin.getLogger().warning("An IOException has occurred when attempting to save config.yml!");
                plugin.getLogger().warning(e.getMessage());
            }
        });
    }

    // No Defaults Yet.
    private void addDefaults() {
        /*if (!config.contains("line")) {
            set("line", component);
        }
        save();*/
    }

    private void generateConfig() {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            plugin.getLogger().info("Loading config.yml...");
            file = new File(plugin.getDataFolder(), "config.yml");
            if (!file.exists()) {
                plugin.getLogger().info("Config.yml does not exist");
                if (file.getParentFile().mkdirs()) {
                    plugin.getLogger().info("Creating parent directory \"" + plugin.getName() + "\"");
                }
                try {
                    if (file.createNewFile()) {
                        plugin.getLogger().info("Generating new config.yml...");
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("An IOException has occurred when attempting to generate config.yml!");
                    plugin.getLogger().warning(e.getMessage());
                }
            }
            Bukkit.getScheduler().runTask(plugin, this::addDefaults);
        });
    }
}
