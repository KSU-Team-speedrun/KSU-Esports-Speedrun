package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Utilities.ComponentHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Subst;

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
        generateDefaultConfig();

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

    public Double getDouble(String line) {
        if (config.isDouble(line)) {
            return config.getDouble(line);
        }
        return null;
    }

    public Object get(String line) {
        return config.get(line);
    }

    public ConfigurationSection getConfigurationSection(String line) {
        return config.getConfigurationSection(line);
    }

    /* Gets a string from the Config.yml given a specified path and returns the value as the actual String (rather
       than a Component) */
    @Subst("")
    public String getString(String line) {

        return config.getString(line);

    }

    public List<String> getStringList(String line) {
        return config.getStringList(line);
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

        /* Ensure that defaults are always loaded even if missing in config. */
        addDefaults();

    }

    // Saves the Config.yml, to be used if the config is updated
    @Deprecated
    @SuppressWarnings("unused")
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

        if (!config.contains("message")) {

            set("messages.prefix", "<bold><gold>[SPEEDRUN]</gold></bold>");

            set("messages.helpMessage", Arrays.asList(
                    "<gold><st>                            </st></gold><white><bold> SPEEDRUN HELP </bold></white><gold><st>                           </st></gold>",
                    "<gold><click:suggest_command:/objectives>/objectives</click></gold> <black>-</black> View all speedrun objectives",
                    "<gold><click:suggest_command:/team>/team [teamName]</click></gold> <black>-</black> Join a specific team.",
                    "<gold><click:suggest_command:/scoreboard>/scoreboard</click></gold> <black>-</black> Enable or disable scoreboard.",
                    "<gold><click:suggest_command:/spawn>/spawn</click></gold> <black>-</black> Teleport to your team's spawn.",
                    "<gold><st>                                                                                </st></gold>"
            ));

            set("messages.toggleScoreboard", "<prefix> Scoreboard is: <is_enabled>");

            set("messages.teamJoinMessage", "<prefix> You joined: <team_name>");
            set("messages.teamCooldownMessage", "<prefix> Please wait a few seconds before changing your team.");
            set("messages.teamHelp", "<prefix> Please use /team [teamName] to join a team.");
            set("messages.alreadyOnTeam", "<prefix> You are already on this team!");
            set("messages.teamIsFull", "<prefix> This team is full!");
            set("messages.teamNotFound", "<prefix> Cannot find team: <team_name>");

            set("messages.start", "<prefix> The speedrun has started!<newline><prefix> You have <bold><gold><time> minutes" +
                    "</gold></bold> to complete the objectives!<newline><prefix> Please type <click:run_command:" +
                    "'/objectives'><hover:show_text:'<bold><gold>Click here to view your objectives</gold></bold>'>" +
                    "<bold><gold>/objectives</gold></bold></hover></click> to view your objectives.");
            set("messages.forceStop", "<prefix> The game has ended!<newline><prefix> The winner is inconclusive.");
            set("messages.winner", "<prefix> The game has ended!<newline><prefix> The winner is: <winner>");
            set("messages.timeUp", "<prefix> The game has ended! Time has run out.<newline><prefix> The team with the" +
                    " most points is: <winner>");

            set("messages.objectiveComplete", "<prefix> Objective Complete: <bold><gold><objective_type> <target>" +
                    "</gold></bold><newline><prefix> Your team has earned <bold><gold><points> points</gold></bold>!");
            set("messages.objectiveCompleteNumber", "<prefix> Objective Complete: <bold><gold><objective_type> <number>" +
                    " <target></gold></bold><newline><prefix> Your team has earned <bold><gold><points> " +
                    "point(s)</gold></bold>!");
            set("messages.objectiveIncrement", "<prefix> Objective Progress: <bold><gold><objective_type> <target></gold></bold>: <current_amount>/<total_amount>");

            set("messages.noTeleportInCombat", "<prefix> You cannot teleport while in combat. Please try again in 15 seconds.");

            set("messages.error.unknownCommand", "<prefix> Unknown Subcommand: /speedrun <unknown_command>");
            set("messages.error.invalidArguments", "<prefix> Invalid Arguments! Usage: <usage>");
            set("messages.error.illegalArgument", "<prefix> Illegal Argument! <illegal_arg> is not a <expected_type>.");
            set("messages.error.outOfBounds", "<prefix> Illegal Argument! <illegal_arg> is out of bounds for <object>.");
            set("messages.error.cannotStartGame", "<prefix> The game cannot be started at the moment. Please check console logs.");
            set("messages.error.worldNotGenerated", "<prefix> This command cannot be executed until a world is generated. Please type " +
                    "<click:run_command:'/speedrun generateWorld'><hover:show_text:'<bold><gold>Click here to generate " +
                    "a world.</gold></bold>'><bold><gold>/speedrun generateWorld</gold></bold></hover></click> to generate a world.");
            set("messages.error.teamsNotEnabled", "<prefix> Teams are not enabled in this speedrun.");
            set("messages.error.noObjectives", "<prefix> A speedrun cannot be started without any objectives.");


            set("messages.admin.helpMessage.p1", Arrays.asList("", "", "", "", "", "", "", "", "", "",
                    "<gold><st>                             </st></gold><white><bold> ADMIN HELP 1/5 </bold></white><gold><st>                        </st></gold><bold><click:run_command:/help 2><hover:show_text:'<bold>NEXT PAGE</bold>'>></hover></click></bold>",
                    "<gold><click:suggest_command:/speedrun addObjective>/speedrun addObjective [objectiveType] [target] <-flag(s)></click></gold> <black>-</black> Add an objective.",
                    "<gold><click:suggest_command:/speedrun objectives>/speedrun objectives</click></gold> <black>-</black> List all objectives.",
                    "<gold><click:suggest_command:/speedrun remObjective>/speedrun remObjective [number]</click></gold> <black>-</black> Remove an objective according to number on objective list.",
                    "<gold><click:suggest_command:/speedrun setSeed>/speedrun setSeed [seed]</click></gold> <black>-</black> Set the map seed.",
                    "<gold><click:suggest_command:/speedrun getSeed>/speedrun getSeed</click></gold> <black>-</black> Get the map seed.",
                    "<gold><click:suggest_command:/speedrun setBorder>/speedrun setBorder [radius]</click></gold> <black>-</black> Set the map border.",
                    "<gold><st>                                                                                </st></gold>"
            ));

            set("messages.admin.helpMessage.p2", Arrays.asList("", "", "", "", "", "", "", "", "", "",
                    "<bold><click:run_command:/help 1><hover:show_text:'<bold>PREVIOUS PAGE</bold>'><</hover></click></bold><gold><st>                           </st></gold><white><bold> ADMIN HELP 2/5 </bold></white><gold><st>                        </st></gold><bold><click:run_command:/help 3><hover:show_text:'<bold>NEXT PAGE</bold>'>></hover></click></bold>",
                    "<gold><click:suggest_command:/speedrun getBorder>/speedrun getBorder</click></gold> <black>-</black> Get the map border.",
                    "<gold><click:suggest_command:/speedrun setTimeLimit>/speedrun setTimeLimit [minutes]</click></gold> <black>-</black> Set the game time limit.",
                    "<gold><click:suggest_command:/speedrun getTimeLimit>/speedrun getTimeLimit</click></gold> <black>-</black> Get the game time limit.",
                    "<gold><click:suggest_command:/speedrun setSpawnRadius>/speedrun setSpawnRadius [number]</click></gold> <black>-</black> Set team spawn radius from (0, 0).",
                    "<gold><click:suggest_command:/speedrun getSpawnRadius>/speedrun getSpawnRadius</click></gold> <black>-</black> Get team spawn radius from (0, 0).",
                    "<gold><st>                                                                                </st></gold>",
                    ""
            ));

            set("messages.admin.helpMessage.p3", Arrays.asList("", "", "", "", "", "", "", "", "", "",
                    "<bold><click:run_command:/help 2><hover:show_text:'<bold>PREVIOUS PAGE</bold>'><</hover></click></bold><gold><st>                           </st></gold><white><bold> ADMIN HELP 3/5 </bold></white><gold><st>                        </st></gold><bold><click:run_command:/help 4><hover:show_text:'<bold>NEXT PAGE</bold>'>></hover></click></bold>",
                    "<gold><click:suggest_command:/speedrun setTeamSize>/speedrun setTeamSize [number]</click></gold> <black>-</black> Set the player limit for each team.",
                    "<gold><click:suggest_command:/speedrun getTeamSize>/speedrun getTeamSize</click></gold> <black>-</black> Get the player limit for each team.",
                    "<gold><click:suggest_command:/speedrun setPointLimit>/speedrun setPointLimit [number]</click></gold> <black>-</black> Set the points required for a team to win.",
                    "<gold><click:suggest_command:/speedrun getPointLimit>/speedrun getPointLimit</click></gold> <black>-</black> Get the points required for a team to win.",
                    "<gold><st>                                                                                </st></gold>",
                    ""
            ));

            set("messages.admin.helpMessage.p4", Arrays.asList("", "", "", "", "", "", "", "", "",
                    "<bold><click:run_command:/help 3><hover:show_text:'<bold>PREVIOUS PAGE</bold>'><</hover></click></bold><gold><st>                           </st></gold><white><bold> ADMIN HELP 4/5 </bold></white><gold><st>                        </st></gold><bold><click:run_command:/help 5><hover:show_text:'<bold>NEXT PAGE</bold>'>></hover></click></bold>",
                    "<gold><click:suggest_command:/speedrun toggleTeams>/speedrun toggleTeams</click></gold> <black>-</black> Toggle teams enabled or disabled.",
                    "<gold><click:suggest_command:/speedrun participate>/speedrun participate</click></gold> <black>-</black> Add yourself as a game participant.",
                    "<gold><click:suggest_command:/speedrun team>/speedrun team [teamName]</click></gold> <black>-</black> Join a specific team.",
                    "<gold><click:suggest_command:/speedrun resetAttributes>/speedrun resetAttributes</click></gold> <black>-</black> Reset all settings back to default.",
                    "<gold><click:suggest_command:/speedrun start>/speedrun start</click></gold> <black>-</black> Generate world and start the game.",
                    "<gold><click:suggest_command:/speedrun stop>/speedrun stop</click></gold> <black>-</black> Force end the game.",
                    "<gold><st>                                                                                </st></gold>",
                    ""
            ));

            set("messages.admin.helpMessage.p5", Arrays.asList("", "", "", "", "", "", "", "", "",
                    "<bold><click:run_command:/help 4><hover:show_text:'<bold>PREVIOUS PAGE</bold>'><</hover></click></bold><gold><st>                           </st></gold><white><bold> ADMIN HELP 5/5 </bold></white><gold><st>                          </st></gold>",
                    "<gold><click:suggest_command:/speedrun setTeamSpawnLocation>/speedrun setTeamSpawnLocation [teamNumber]</click></gold> <black>-</black> Set team spawn location to current location.",
                    "<gold><click:suggest_command:/speedrun getTeamSpawnLocations>/speedrun getTeamSpawnLocations</click></gold> <black>-</black> Get all team spawn locations.",
                    "<gold><click:suggest_command:/speedrun generateWorld>/speedrun generateWorld</click></gold> <black>-</black> Generate the speedrun world.",
                    "<gold><click:suggest_command:/speedrun deleteWorld>/speedrun deleteWorld</click></gold> <black>-</black> Delete current speedrun world.",
                    "<gold><st>                                                                                </st></gold>",
                    "", ""
            ));

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
            set("messages.admin.timeLimit", "<prefix> Time Limit: <bold><gold><time_limit> Minute(s)</gold></bold>");
            set("messages.admin.teamSizeLimit", "<prefix> Team Size Limit: <bold><gold><size_limit></gold></bold>");
            set("messages.admin.seedSet", "<prefix> World seed has been set to: <bold><gold><seed></gold></bold>");
            set("messages.admin.seed", "<prefix> Seed: <bold><gold><seed></gold></bold>");
            set("messages.admin.worldBorderSet", "<prefix> World border has been set to: <bold><gold><world_border></gold></bold>");
            set("messages.admin.worldBorder", "<prefix> World Border: <bold><gold><world_border></gold></bold>");
            set("messages.admin.spawnRadiusSet", "<prefix> Spawn radius has been set to: <bold><gold><spawn_radius></gold></bold>");
            set("messages.admin.spawnRadius", "<prefix> Spawn Radius: <bold><gold><spawn_radius></gold></bold>");
            set("messages.admin.pointLimitSet", "<prefix> Point limit set to: <bold><gold><point_limit></gold></bold>");
            set("messages.admin.pointLimit", "<prefix> Point Limit: <bold><gold><point_limit></gold></bold>");
            set("messages.admin.gameAlreadyStarted", "<prefix> The game has already started!");
            set("messages.admin.gameNotStarted", "<prefix> There is no game currently in progress.");
            set("messages.admin.gameStarted", "<prefix> You have started the speedrun!");
            set("messages.admin.gameStartedCannotChange", "<prefix> This attribute cannot be changed after the game has started.");
            set("messages.admin.worldGenerating", "<prefix> Generating world... Please wait.");
            set("messages.admin.worldGenerated", "<prefix> World generated!");
            set("messages.admin.worldDeleted", "<prefix> The existing speedrun world has been deleted.");
            set("messages.admin.spawnsGenerating", "<prefix> Locating team spawn points... Please wait.");
            set("messages.admin.spawnsGenerated", "<prefix> Team spawn points located! Please enter <gold><b><click:run_command:/speedrun " +
                    "getTeamSpawnLocations><hover:show_text:'<b><gold>Click to Execute Command</gold></b>'>/speedrun getTeamSpawnLocations</hover>" +
                    "</click></b></gold> to inspect spawn locations, or enter <gold><b><click:run_command:/speedrun start>" +
                    "<hover:show_text:'<b><gold>Click to Execute Command</gold></b>'>/speedrun start</hover></click></b></gold> to start the game.");
            set("messages.admin.resetAttributes", "<prefix> All attributes have been reset.");
            set("messages.admin.toggleTeams", "<prefix> Teams have been: <toggle_option>");
            set("messages.admin.participationSet", "<prefix> Participation set: <is_participating>");
            set("messages.admin.teamSpawnSet", "<prefix> Team #<team_number> spawn point has been updated.");
            set("messages.admin.teamSpawnLocations", "<prefix> Team Spawn Locations:");
            set("messages.admin.teamSpawnLocation", "<gold><b><number></b></gold>: <click:run_command:/execute in speedrunworld run teleport <player> " +
                    "<location_x> <location_y> <location_z>><hover:show_text:'<b><gold>Click to Teleport</gold></b>'>(<location_x>, " +
                    "<location_y>, <location_z>)</hover></click>");
            set("messages.admin.unsafeSpawnAlert", "<bold><red>[ALERT]</red></bold> The spawn point for team #<team_number> is unsafe. Consider manually setting this spawn point or choosing a different seed.");
            set("messages.admin.leavesWarning", "<b><yellow>[NOTICE]</yellow></b> The spawn point for team #<team_number> is on <type>. Consider reviewing this spawn point to ensure it is safe.");
        }

        if (!config.contains("scoreboard")) {

            set("scoreboard.interval", 1);
            set("scoreboard.disable", false);

            set("scoreboard.title", "<bold><gold>KSU SPEEDRUN</gold></bold>");
            set("scoreboard.timeLeft", "<white>Time Remaining:</white> <bold><gold><time_remaining></gold></bold>");
            set("scoreboard.gameOverMessage", "<white><bold>GAME OVER!</bold></white>");
            set("scoreboard.pointsMessage", "<white>Team Points:</white> <bold><gold><points></gold></bold><white>/</white><bold><gold><total_points></gold></bold>");

        }

        if (!config.contains("title")) {

            set("title.enabled", true);

            set("title.gameOverTitle", "<b><gold>GAME OVER!</gold></b>");
            set("title.gameOverSubtitle", "<b><winner> has won the game!</b>");

            set("title.sound", "item.goat_horn.sound.1");

        }

        if (!config.contains("teams")) {

            set("teams.inventory.title", "<bold><yellow>SELECT A TEAM:</yellow></bold>");
            set("teams.inventory.cooldown", 5);

            set("teams.objectiveIncrement", true);

            set("teams.teamPvP", false);
            set("teams.PvP", true);

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

        if (!config.contains("world")) {
            set("world.deleteOnStart", true);
            set("world.spawnPoint.enabled", false);
            set("world.spawnPoint.world", "world");
            set("world.spawnPoint.x", 0);
            set("world.spawnPoint.y", "ground");
            set("world.spawnPoint.z", 0);
            set("world.spawnPoint.pitch", 0.0);
            set("world.spawnPoint.yaw", -0.0);
        }

        if (!config.contains("gameRules")) {

            set("gameRules.enabled", false);

            for (GameRule<?> rule : GameRule.values()) {
                set("gameRules." + rule.getName(), "default");
            }

        }

        /* Loops through every structure in the game and adds it to the config by default. Administrators can update
           these values with the average Y-coordinate of each structure */
        if (!config.contains("structureLocations")) {

            set("structureLocations.PILLAGER_OUTPOST.averageYCoordinate", "ground");
            set("structureLocations.PILLAGER_OUTPOST.radius", 30);
            set("structureLocations.PILLAGER_OUTPOST.height", 25);

            set("structureLocations.MINESHAFT.averageYCoordinate", 0);
            set("structureLocations.MINESHAFT.radius", 100);
            set("structureLocations.MINESHAFT.height", 20);

            set("structureLocations.MINESHAFT_MESA.averageYCoordinate", "ground");
            set("structureLocations.MINESHAFT_MESA.radius", 30);
            set("structureLocations.MINESHAFT_MESA.height", 10);

            set("structureLocations.MANSION.averageYCoordinate", "ground");
            set("structureLocations.MANSION.radius", 30);
            set("structureLocations.MANSION.height", 25);

            set("structureLocations.JUNGLE_PYRAMID.averageYCoordinate", "ground");
            set("structureLocations.JUNGLE_PYRAMID.radius", 10);
            set("structureLocations.JUNGLE_PYRAMID.height", 15);

            set("structureLocations.DESERT_PYRAMID.averageYCoordinate", "ground");
            set("structureLocations.DESERT_PYRAMID.radius", 15);
            set("structureLocations.DESERT_PYRAMID.height", 20);

            set("structureLocations.IGLOO.averageYCoordinate", "ground");
            set("structureLocations.IGLOO.radius", 4);
            set("structureLocations.IGLOO.height", 5);

            set("structureLocations.SHIPWRECK.averageYCoordinate", 50);
            set("structureLocations.SHIPWRECK.radius", 15);
            set("structureLocations.SHIPWRECK.height", 10);

            set("structureLocations.SHIPWRECK_BEACHED.averageYCoordinate", "ground");
            set("structureLocations.SHIPWRECK_BEACHED.radius", 15);
            set("structureLocations.SHIPWRECK_BEACHED.height", 10);

            set("structureLocations.SWAMP_HUT.averageYCoordinate", "ground");
            set("structureLocations.SWAMP_HUT.radius", 4);
            set("structureLocations.SWAMP_HUT.height", 5);

            set("structureLocations.STRONGHOLD.averageYCoordinate", 0);
            set("structureLocations.STRONGHOLD.radius", 75);
            set("structureLocations.STRONGHOLD.height", 30);

            set("structureLocations.MONUMENT.averageYCoordinate", 45);
            set("structureLocations.MONUMENT.radius", 30);
            set("structureLocations.MONUMENT.height", 16);

            set("structureLocations.OCEAN_RUIN_COLD.averageYCoordinate", 45);
            set("structureLocations.OCEAN_RUIN_COLD.radius", 10);
            set("structureLocations.OCEAN_RUIN_COLD.height", 5);

            set("structureLocations.OCEAN_RUIN_WARM.averageYCoordinate", 45);
            set("structureLocations.OCEAN_RUIN_WARM.radius", 10);
            set("structureLocations.OCEAN_RUIN_WARM.height", 5);

            set("structureLocations.FORTRESS.averageYCoordinate", 15);
            set("structureLocations.FORTRESS.radius", 75);
            set("structureLocations.FORTRESS.height", 50);

            set("structureLocations.NETHER_FOSSIL.averageYCoordinate", 35);
            set("structureLocations.NETHER_FOSSIL.radius", 5);
            set("structureLocations.NETHER_FOSSIL.height", 4);

            set("structureLocations.END_CITY.averageYCoordinate", "ground");
            set("structureLocations.END_CITY.radius", 30);
            set("structureLocations.END_CITY.height", 100);

            set("structureLocations.BURIED_TREASURE.averageYCoordinate", "ground - 4");
            set("structureLocations.BURIED_TREASURE.radius", 2);
            set("structureLocations.BURIED_TREASURE.height", 1);

            set("structureLocations.BASTION_REMNANT.averageYCoordinate", 35);
            set("structureLocations.BASTION_REMNANT.radius", 40);
            set("structureLocations.BASTION_REMNANT.height", 45);

            set("structureLocations.VILLAGE_PLAINS.averageYCoordinate", "ground");
            set("structureLocations.VILLAGE_PLAINS.radius", 30);
            set("structureLocations.VILLAGE_PLAINS.height", 8);

            set("structureLocations.VILLAGE_DESERT.averageYCoordinate", "ground");
            set("structureLocations.VILLAGE_DESERT.radius", 30);
            set("structureLocations.VILLAGE_DESERT.height", 8);

            set("structureLocations.VILLAGE_SAVANNA.averageYCoordinate", "ground");
            set("structureLocations.VILLAGE_SAVANNA.radius", 30);
            set("structureLocations.VILLAGE_SAVANNA.height", 8);

            set("structureLocations.VILLAGE_SNOWY.averageYCoordinate", "ground");
            set("structureLocations.VILLAGE_SNOWY.radius", 30);

            set("structureLocations.VILLAGE_TAIGA.averageYCoordinate", "ground");
            set("structureLocations.VILLAGE_TAIGA.radius", 30);
            set("structureLocations.VILLAGE_TAIGA.height", 8);

            set("structureLocations.RUINED_PORTAL.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL.radius", 30);
            set("structureLocations.RUINED_PORTAL.height", 8);

            set("structureLocations.RUINED_PORTAL_DESERT.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL_DESERT.radius", 30);
            set("structureLocations.RUINED_PORTAL_DESERT.height", 8);

            set("structureLocations.RUINED_PORTAL_JUNGLE.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL_JUNGLE.radius", 5);
            set("structureLocations.RUINED_PORTAL_JUNGLE.height", 6);

            set("structureLocations.RUINED_PORTAL_SWAMP.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL_SWAMP.radius", 5);
            set("structureLocations.RUINED_PORTAL_SWAMP.height", 6);

            set("structureLocations.RUINED_PORTAL_MOUNTAIN.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL_MOUNTAIN.radius", 5);
            set("structureLocations.RUINED_PORTAL_MOUNTAIN.height", 6);

            set("structureLocations.RUINED_PORTAL_OCEAN.averageYCoordinate", "ground");
            set("structureLocations.RUINED_PORTAL_OCEAN.radius", 5);
            set("structureLocations.RUINED_PORTAL_OCEAN.height", 6);

            set("structureLocations.RUINED_PORTAL_NETHER.averageYCoordinate", 35);
            set("structureLocations.RUINED_PORTAL_NETHER.radius", 5);
            set("structureLocations.RUINED_PORTAL_NETHER.height", 6);

            set("structureLocations.ANCIENT_CITY.averageYCoordinate", -51);
            set("structureLocations.ANCIENT_CITY.radius", 100);
            set("structureLocations.ANCIENT_CITY.height", 50);

            set("structureLocations.TRAIL_RUINS.averageYCoordinate", "ground - 8");
            set("structureLocations.TRAIL_RUINS.radius", 5);
            set("structureLocations.TRAIL_RUINS.height", 10);

            set("structureLocations.TRIAL_CHAMBERS.averageYCoordinate", -30);
            set("structureLocations.TRIAL_CHAMBERS.radius", 100);
            set("structureLocations.TRIAL_CHAMBERS.height", 30);

            // Dynamic handling for any unspecified structures
            for (String s : SRStructure.getStructureNames()) {
                if (!config.contains("structureLocations." + s)) {
                    set("structureLocations." + s + ".averageYCoordinate", "ground");
                    set("structureLocations." + s + ".radius", 30);
                    set("structureLocations." + s + ".height", 10); // Default height for unspecified structures
                }
            }
        }
    }

    // Generates a Config.yml file with defaults if one does not already exist.. Loads the Config.yml file if one exists
    @Deprecated
    @SuppressWarnings("unused")
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

    private void generateDefaultConfig() {

        plugin.getLogger().info("Loading config.yml...");
        file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.getLogger().info("Config.yml does not exist");
            if (file.getParentFile().mkdirs()) {
                plugin.getLogger().info("Creating parent directory \"" + plugin.getName() + "\"");
            }

            plugin.saveResource("default-config.yml", false);

            File defaultConfig = new File(plugin.getDataFolder(), "default-config.yml");
            if (defaultConfig.exists() && defaultConfig.renameTo(file)) {
                plugin.getLogger().info("Generated new config.yml from default-config.yml.");
            } else {
                plugin.getLogger().warning("Failed to rename default-config.yml to config.yml.");
            }
        }
        load();
    }
}
