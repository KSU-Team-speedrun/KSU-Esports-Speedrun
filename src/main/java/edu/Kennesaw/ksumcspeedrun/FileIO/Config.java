package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.Kennesaw.ksumcspeedrun.ComponentHelper;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

    Main plugin;

    private File file;
    private final YamlConfiguration config;

    private Component pluginPrefix;

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

    public List<String> getList(String line) {
        if (config.isList(line)) {
            return config.getStringList(line);
        }
        return null;
    }

    public String getString(String line) {
        return config.getString(line);
    }

    public Object getObject(String line) {
        return config.get(line);
    }

    public int getInt(String line) {
        return config.getInt(line);
    }

    public void setComponent(String line, Component tc) {
        config.set(line, ComponentHelper.componentToMM(tc));
    }

    public void set(String line, Object o) {
        config.set(line, o);
    }

    public void load() {
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
        addDefaults();
    }

    public Component getPrefix() {
        return pluginPrefix;
    }

    private void save() {
        plugin.getLogger().info("Saving config.yml...");
        try {
            config.save(file);
            plugin.getLogger().info("Success saving config.yml!");
        } catch (IOException e) {
            plugin.getLogger().warning("An IOException has occurred when attempting to save config.yml!");
            plugin.getLogger().warning(e.getMessage());
        }
    }

    private void addDefaults() {
        if (!config.contains("prefix")) {
            setComponent("prefix", Component.text("[KSU-MC-Speedrun]").color(TextColor.color(0xFFFF55)).append(Component.text(" ").color(TextColor.color(0xFFFFFF))));
        }
        if (!config.contains("structureLocations")) {
            for (String s : SRStructure.getStructureNames()) {
                if (s.equalsIgnoreCase("ANCIENT_CITY")) {
                    set("structureLocations." + s + ".averageYCoordinate", -51);
                } else if (s.equalsIgnoreCase("STRONGHOLD")) {
                    set("structureLocations." + s + ".averageYCoordinate", 0);
                } else if (s.equalsIgnoreCase("TRIAL_CHAMBERS")) {
                    set("structureLocations." + s + ".averageYCoordinate", -30);
                } else {
                    set("structureLocations." + s + ".averageYCoordinate", "ground");
                }
            }
            for (String s : SRStructure.getStructureNames()) {
                if (s.equalsIgnoreCase("ANCIENT_CITY")) {
                    set("structureLocations." + s + ".radius", 100);
                } else if (s.equalsIgnoreCase("STRONGHOLD")) {
                    set("structureLocations." + s + ".radius", 75);
                } else if (s.equalsIgnoreCase("TRIAL_CHAMBERS")) {
                    set("structureLocations." + s + ".radius", 100);
                } else {
                    set("structureLocations." + s + ".radius", 30);
                }
            }
        }
        save();
        pluginPrefix = getComponent("prefix");
    }

    private void generateConfig() {
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
        } else {
            load();
            return;
        }
        addDefaults();
    }
}
