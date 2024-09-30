package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Utilities.ComponentHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

    // Main plugin instance
    Main plugin;

    // File object that contains location of Config.yml
    private File file;

    // YamlConfiguration object allows is to modify, load, and save Config.yml
    private final YamlConfiguration config;

    /* The pluginPrefix Component is used frequently in messages sent to senders in this plugin, so this value is loaded
       when the plugin is enabled and stored so that it does not need to be retrieved from the Config each time. */
    private Component pluginPrefix;

    // An instance of Config can be created by passing the Main plugin instance through the constructor
    // This instance is created when the plugin is enabled and used until the plugin is disabled.
    public Config(Main plugin) {

        config = new YamlConfiguration();
        this.plugin = plugin;

        // Called when a Config instance is created
        generateConfig();

    }

    // Returns a Component value from the Config.yml given the String path
    public Component getComponent(String line) {

        if (config.contains(line)) {
            if (config.isString(line)) {

                /* Messages are saved in MiniMessage format in the Config.yml, this will deserialize the MiniMessage
                   into regular Component format before returning the message */
                return ComponentHelper.mmStringToComponent(config.getString(line));

            } else {

                /* The specified path in the Config.yml does not lead to a MiniMessage String that can be deserialized
                into a component */
                plugin.getLogger().warning("Data on line \"" + line + "\" is not readable.");
                plugin.getLogger().warning("Data cannot be extracted from config.yml. Returning NULL..");
                return null;

            }
        } else {

            // The specified path does not exist in the Config.yml
            plugin.getLogger().warning("Config does not contain line \"" + line + "\"");
            plugin.getLogger().warning("Data cannot be extracted from config.yml. Returning NULL..");
            return null;

        }
    }

    // Gets a string list from the Config.yml given a specified path
    public List<String> getStringList(String line) {

        if (config.isList(line)) {
            return config.getStringList(line);
        }
        return null;

    }

    /* Gets a string from the Config.yml given a specified path and returns the value as the actual String (rather
       than a Component) */
    public String getString(String line) {

        return config.getString(line);

    }

    /* Gets an ambiguous object from the Config.yml given a specified path; useful if something can be, e.g., either a
       string or integer. */
    public Object getObject(String line) {

        return config.get(line);

    }

    // Get an integer from the Config.yml given a specified path
    public int getInt(String line) {

        return config.getInt(line);

    }

    // Set a Component at the specified path in the Config.yml
    public void setComponent(String line, Component tc) {

        // Component is serialized into MiniMessage format before being saved in the Config.yml
        config.set(line, ComponentHelper.componentToMM(tc));

    }

    // Set an ambiguous object in the Config.yml given a specified path
    public void set(String line, Object o) {

        config.set(line, o);

    }

    // Load (or reload) the Config.yml file using YamlConfiguration
    public void load() {

        // Attempt to load the Config.yml file
        try {

            config.load(file);

        // Errors will be thrown and caught if an IOException occurs or if the Config.yml has an invalid format
        } catch (IOException e) {

            plugin.getLogger().warning("An IOException has occurred when attempting to load config.yml!");
            plugin.getLogger().warning(e.getMessage());
        } catch (InvalidConfigurationException e) {
            Logger.logError("Your config.yml is configured in an invalid format! Please fix this problem or delete the config.yml for a default file.", e, plugin);
            plugin.getServer().shutdown();
            return;

        }

        /* All values that are assigned as default (required) must always be in the Config.yml: this method adds any
           values, if they are missing. */
        addDefaults();

    }

    /* Returns the pluginPrefix that was stored when the plugin was enabled, can be retrieved from any class that has a
       main plugin instance (plugin.getSpeedrunConfig().getPrefix) */
    public Component getPrefix() {
        return pluginPrefix;
    }

    // Saves the Config.yml, to be used if the config is updated
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

    /* Called when the config is loaded/reloaded or generated, adds default values to the config that must exist
       for the plugin to function correctly */
    private void addDefaults() {

        // Default layout of plugin prefix, can be updated in the Config.yml
        if (!config.contains("prefix")) {

            setComponent("prefix", Component.text("[KSU-MC-Speedrun]").color(TextColor.color(0xFFFF55)).append(Component.text(" ").color(TextColor.color(0xFFFFFF))));

        }

        /* Loops through every structure in the game and adds it to the config by default. Administrators can update
           these values with the average Y-coordinate of each structure */
        if (!config.contains("structureLocations")) {
            for (String s : SRStructure.getStructureNames()) {

                /* The average Y-coordinate for an Ancient City is y=-51, the radius is set to 100
                   This is an example of how structures can be configured, as well as the next two
                   default examples (Stronghold & Trial Chambers) */

                if (s.equalsIgnoreCase("ANCIENT_CITY")) {
                    set("structureLocations." + s + ".averageYCoordinate", -51);
                    set("structureLocations." + s + ".radius", 100);
                } else if (s.equalsIgnoreCase("STRONGHOLD")) {
                    set("structureLocations." + s + ".averageYCoordinate", 0);
                    set("structureLocations." + s + ".radius", 75);
                } else if (s.equalsIgnoreCase("TRIAL_CHAMBERS")) {
                    set("structureLocations." + s + ".averageYCoordinate", -30);
                    set("structureLocations." + s + ".radius", 100);

                } else {

                    /* Most structures are set to where their Y-coordinate is ground level by default and the structure
                       can be detected within a 30 block radius */

                    set("structureLocations." + s + ".averageYCoordinate", "ground");
                    set("structureLocations." + s + ".radius", 30);

                }
            }
        }
        save();
        pluginPrefix = getComponent("prefix");
    }

    // Generates a Config.yml file with defaults if one does not already exist.. Loads the Config.yml file if one exists
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
            addDefaults();
        } else {
            load();
        }

    }
}
