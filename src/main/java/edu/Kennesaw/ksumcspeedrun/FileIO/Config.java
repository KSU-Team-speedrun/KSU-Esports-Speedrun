package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Utilities.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

@SuppressWarnings("SpellCheckingInspection")
public class Config {

    // Main plugin instance
    Main plugin;

    // File object that contains location of Config.yml
    private File file;

    // YamlConfiguration object allows is to modify, load, and save Config.yml
    private final YamlConfiguration config;

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

    public Boolean getBoolean(String line) {
        if (config.isBoolean(line)) {
            return config.getBoolean(line);
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
        if (!config.contains("message")) {
            set("messages.prefix", "<bold><gold>[SPEEDRUN]</gold></bold>");
            config.setComments("messages.teamJoinMessage", Arrays.asList(" ", "# Team-Related Messages"));
            set("messages.teamJoinMessage", "<prefix> You joined: <team_name>");
            set("messages.teamCooldownMessage", "<prefix> Please wait a few seconds before changing your team.");
            set("messages.alreadyOnTeam", "<prefix> You are already on this team!");
            set("messages.teamIsFull", "<prefix> This team is full!");
            config.setComments("messages.start", Arrays.asList(" ", "# Game Start and End Messages"));
            set("messages.start", "<prefix> The speedrun has started!<newline><prefix> You have <bold><gold><time> minutes" +
                    "</gold></bold> to complete the objectives!<newline><prefix> Please type <click:run_command:" +
                    "'/objectives'><hover:show_text:'<bold><gold>Click here to view your objectives</gold></bold>'>" +
                    "<bold><gold>/objectives</gold></bold></hover></click> to view your objectives.");
            set("messages.forceStop", "<prefix> The game has ended!<newline><prefix> The winner is inconclusive.");
            set("messages.winner", "<prefix> The game has ended!<newline><prefix> The winner is: <winner>");
            set("messages.timeUp", "<prefix> The game has ended! Time has run out.<newline><prefix> The team with the" +
                    " most points is: <winner>");
            config.setComments("messages.objectiveComplete", Arrays.asList(" ", "# Objective Completion Messages"));
            set("messages.objectiveComplete", "<prefix> Objective Complete: <bold><gold><objective_type> <target>" +
                    "</gold></bold><newline><prefix> Your team has earned <bold><gold><points> points</gold></bold>!");
            set("messages.objectiveCompleteNumber", "<prefix> Objective Complete: <bold><gold><objective_type> <number>" +
                    " <target></gold></bold><newline><prefix> Your team has earned <bold><gold><points> " +
                    "point(s)</gold></bold>!");
            config.setComments("messages.error.invalidArguments", Arrays.asList(" ", "# Error Messages"));
            set("messages.error.invalidArguments", "<prefix> Invalid Arguments! Usage: <usage>");
            set("messages.error.illegalArgument", "<prefix> Illegal Argument! <illegal_arg> is not a <expected_type>.");
            set("messages.error.outOfBounds", "<prefix> Illegal Argument! <illegal_arg> is out of bounds for <object>.");
            config.setComments("messages.admin.objectiveAdded", Arrays.asList(" ", "# Admin Messages"));
            set("messages.admin.objectiveAdded", "<prefix> Objective Added: <bold><gold><objective_type> <target>" +
                    "</gold></bold>");
            set("messages.admin.objectiveAddedPoints", "<prefix> Objective Added: <bold><gold><objective_type> " +
                    "<target></gold></bold> - <points> points");
            set("messages.admin.objectiveAddedNumber", "<prefix> Objective Added: <bold><gold><objective_type> " +
                    "<number> <target></gold></bold>");
            set("messages.admin.objectiveAddedPointsNumber", "<prefix> Objective Added: <bold><gold><objective_type> " +
                    "<number> <target></gold></bold> - <points> points");
            set("messages.admin.objectiveRemoved", "<prefix> Objective Removed: <bold><gold><objective_type> <target>" +
                    "</gold></bold>");
            set("messages.admin.timeLimitSet", "<prefix> Time limit set to: <bold><gold><time_limit> Minute(s)</gold></bold>");
            set("messages.admin.teamSizeLimitSet", "<prefix> Team size limit set to: <bold><gold><size_limit></gold></bold>");

        }

        if (!config.contains("timer")) {

            config.setComments("timer.interval", Arrays.asList(" ", "# Timer Configurations"));
            set("timer.interval", 1);
            set("timer.disable", false);

            config.setComments("timer.title", Arrays.asList(" ", "# Timer Messages"));
            set("timer.title", "<bold><gold>KSU SPEEDRUN</gold></bold>");
            set("timer.timeLeft", "<white>Time Remaining:</white> <bold><gold><time_remaining></gold></bold>");
            set("timer.gameOverMessage", "<white><bold>GAME OVER!</bold></white>");

        }

        if (!config.contains("teams")) {

            config.setComments("teams.inventory.title", Arrays.asList(" ", "# Team GUI Meta"));
            set("teams.inventory.title", "<bold><yellow>SELECT A TEAM:</yellow></bold>");
            set("teams.inventory.cooldown", 5);


            config.setComments("teams.white.name", Arrays.asList(" ", "# Team Definitions"));
            set("teams.white.name", "<!italic><white><bold>WHITE TEAM</bold></white>");
            set("teams.white.item", "WHITE_WOOL");
            set("teams.white.lore", "<!italic><white>Click here to join <bold>WHITE TEAM</bold>!</white>");

            set("teams.orange.name", "<!italic><gold><bold>ORANGE TEAM</bold></gold>");
            set("teams.orange.item", "ORANGE_WOOL");
            set("teams.orange.lore", "<!italic><white>Click here to join </white><bold><gold>ORANGE TEAM</gold></bold><white>!</white>");

            set("teams.magenta.name", "<!italic><color:#ff00ff><bold>MAGENTA TEAM</bold></color>");
            set("teams.magenta.item", "MAGENTA_WOOL");
            set("teams.magenta.lore", "<!italic><white>Click here to join </white><bold><color:#ff00ff>MAGENTA TEAM</color></bold><white>!</white>");

            set("teams.light_blue.name", "<!italic><color:#00ffff><bold>LIGHT BLUE TEAM</bold></color>");
            set("teams.light_blue.item", "LIGHT_BLUE_WOOL");
            set("teams.light_blue.lore", "<!italic><white>Click here to join </white><bold><color:#00ffff>LIGHT BLUE TEAM</color></bold><white>!</white>");

            set("teams.yellow.name", "<!italic><yellow><bold>YELLOW TEAM</bold></yellow>");
            set("teams.yellow.item", "YELLOW_WOOL");
            set("teams.yellow.lore", "<!italic><white>Click here to join </white><bold><yellow>YELLOW TEAM</yellow></bold><white>!</white>");

            set("teams.lime.name", "<!italic><green><bold>LIME TEAM</bold></green>");
            set("teams.lime.item", "LIME_WOOL");
            set("teams.lime.lore", "<!italic><white>Click here to join </white><bold><green>LIME TEAM</green></bold><white>!</white>");

            set("teams.pink.name", "<!italic><color:#ff85c8><bold>PINK TEAM</bold></color>");
            set("teams.pink.item", "PINK_WOOL");
            set("teams.pink.lore", "<!italic><white>Click here to join </white><bold><color:#ff85c8>PINK TEAM</color></bold><white>!</white>");

            set("teams.gray.name", "<!italic><dark_gray><bold>GRAY TEAM</bold></dark_gray>");
            set("teams.gray.item", "GRAY_WOOL");
            set("teams.gray.lore", "<!italic><white>Click here to join </white><bold><dark_gray>GRAY TEAM</dark_gray></bold><white>!</white>");

            set("teams.light_gray.name", "<!italic><gray><bold>LIGHT GRAY TEAM</bold></gray>");
            set("teams.light_gray.item", "LIGHT_GRAY_WOOL");
            set("teams.light_gray.lore", "<!italic><white>Click here to join </white><bold><gray>LIGHT GRAY TEAM</gray></bold><white>!</white>");

            set("teams.cyan.name", "<!italic><dark_aqua><bold>CYAN TEAM</bold></dark_aqua>");
            set("teams.cyan.item", "CYAN_WOOL");
            set("teams.cyan.lore", "<!italic><white>Click here to join </white><bold><dark_aqua>CYAN TEAM</dark_aqua></bold><white>!</white>");

            set("teams.purple.name", "<!italic><dark_purple><bold>PURPLE TEAM</bold></dark_purple>");
            set("teams.purple.item", "PURPLE_WOOL");
            set("teams.purple.lore", "<!italic><white>Click here to join </white><bold><dark_purple>PURPLE TEAM</dark_purple></bold><white>!</white>");

            set("teams.blue.name", "<!italic><dark_blue><bold>BLUE TEAM</bold></dark_blue>");
            set("teams.blue.item", "BLUE_WOOL");
            set("teams.blue.lore", "<!italic><white>Click here to join </white><bold><dark_blue>BLUE TEAM</dark_blue></bold><white>!</white>");

            set("teams.brown.name", "<!italic><color:#964b00><bold>BROWN TEAM</bold></color>");
            set("teams.brown.item", "BROWN_WOOL");
            set("teams.brown.lore", "<!italic><white>Click here to join </white><bold><color:#964b00>BROWN TEAM</color></bold><white>!</white>");

            set("teams.green.name", "<!italic><dark_green><bold>GREEN TEAM</bold></dark_green>");
            set("teams.green.item", "GREEN_WOOL");
            set("teams.green.lore", "<!italic><white>Click here to join </white><bold><dark_green>GREEN TEAM</dark_green></bold><white>!</white>");

            set("teams.red.name", "<!italic><dark_red><bold>RED TEAM</bold></dark_red>");
            set("teams.red.item", "RED_WOOL");
            set("teams.red.lore", "<!italic><white>Click here to join </white><bold><dark_red>RED TEAM</dark_red></bold><white>!</white>");

            set("teams.black.name", "<!italic><black><bold>BLACK TEAM</bold></black>");
            set("teams.black.item", "BLACK_WOOL");
            set("teams.black.lore", "<!italic><white>Click here to join </white><bold><black>BLACK TEAM</black></bold><white>!</white>");

        }

                /* Loops through every structure in the game and adds it to the config by default. Administrators can update
           these values with the average Y-coordinate of each structure */
        if (!config.contains("structureLocations")) {

            config.setComments("structureLocations", Arrays.asList(" ", "# Structure Detection Configurations"));

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

        ConfigurationSection cs = config.getConfigurationSection("messages");

        if (cs != null) {
            cs.setComments("", Arrays.asList(" ", "# Global Plugin Prefix"));
        }

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
