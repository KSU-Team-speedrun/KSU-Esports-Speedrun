package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Events.PlayerMove;
import edu.Kennesaw.ksumcspeedrun.Objects.Scoreboard;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.*;
import edu.Kennesaw.ksumcspeedrun.Utilities.ComponentHelper;
import edu.Kennesaw.ksumcspeedrun.Utilities.ConcurrentTwoWayMap;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import edu.Kennesaw.ksumcspeedrun.Utilities.WorldGenerator;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.sound.Sound;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/* Speedrun Object Class, centerpoint of logic for speedrun events
   Contains various setters & getters for all speedrun attributes */
@SuppressWarnings({"unstableApiUsage", "unused", "SpellCheckingInspection"})
public class Speedrun {

    Main plugin;

    // Speedrun Attributes
    private String seed; // Seed -> Random if Never Specified
    private int border; // World Border -> 5000 by Default
    private int timeLimit; // Game Time Limit -> 1hr by Default
    private TimeUnit timeUnit; // Only supports minutes as of latest version
    private int spawnRadius; // Team Spawn Radius -> 300 by Default

    // The maximum amount of teams is equal to the number of teams defined in the config
    // If teams are disabled (SoloTeams are used), maximum teams are 32
    /*
     * TODO - Allow maximum number of soloteams to be defined in config:
     *  - Explanation: This must be a set number because the plugin finds this number of team spawn points when
     *  - a world is generated, which should theoretically be prior to the server filling with players.
     */
    private int maxTeams;

    // True if the Speedrun has started
    private boolean isStarted;

    /* The total points to win IF SET BY AN ADMIN - If an admin does not adjust this number, then the sum
       of the weight of all objectives will be used instead of this number (objectives.getTotalWeight()) */
    private int totalWeight = 0;

    /* ObjectiveManager manages all objectives and holds them in a single list which can be adjusted using the provided
       methods. It keeps track of the objectives and their total weight. */
    private final ObjectiveManager objectives;

    /* This class manages teams in the plugin, including their creation, removal, and associated data mappings for
       players, names, and inventory items. It also provides functionality for handling team size limits, UI interactions,
       and resetting team states during gameplay. */
    private final TeamManager tm;

    // This is edu.Kennesaw.Objects.Scoreboard - not org.Bukkit...
    private Scoreboard ct;

    /* Combat Logs stored here: any entity can initiate combat (players or NPC), but combat can only be initiated
       with a player */
    public ConcurrentTwoWayMap<UUID, Player> combatLog;

    /* Store the actual combat log deletion task so it can be canceled if the player is removed from the combat log
       for a reason other than time expiration */
    public Map<UUID, ScheduledTask> combatTasks;

    /* The location of a bed and the player that activated it are mapped together:
       This is used to attribute bed explosion kills to players who activated them */
    public Map<Location, Player> bedLog;

    // Players are temporarily added to this set when they pick/change their team. They are removed after a set time.
    // They cannot change their team while they are in this set.
    private Set<Player> teamCooldown;

    /* Admins are left unincluded from games and team calculations unless they specify with /speedrun participate:
       Thus, we use this list instead of Bukkit.getOnlinePlayers() */
    private final List<Player> onlinePlayers;

    // True by default - if teams are disabled then SoloTeams are used: each player is their own "team"
    private boolean teamsEnabled;

    // Specific players can disable their scoreboard during a game - they are stored here
    private final Set<Player> scoreboardDisabled;

    // This is the folder where the speedrun world is stored. Kept here for easy access & deletion if necessary
    final File worldFolder = new File(Bukkit.getWorldContainer(), "speedrunworld");

    // This is the actual speedrun world object
    private World speedrunWorld;

    /* When a world is generated, team spawn locations are calculated & then stored here. The spawn locations for the
       maximum possible number of teams is calculated: e.g., if there are 16 teams defined in the config but only
       4 teams in use, the spawn location for all 16 teams will calculate, but when the game starts only spawnpoints
       4, 8, 12, & 16 will be used */
    List<Location> teamSpawnLocations;

    /* We want to be able to get a team easily from the corresponding team number so that spawn locations
       can be updated if necessary */
    Map<Integer, Team> spawnLocationIndexToTeam;

    // Speedrun Constructor for Initialization
    // All objects explained above are initialized or set to their default value
    public Speedrun(Main plugin) {

        this.plugin = plugin;

        Random rand = new Random();
        this.seed = rand.nextInt() + "";
        this.border = 5000;
        this.timeLimit = 60;
        this.timeUnit = TimeUnit.MINUTES;
        this.spawnRadius = 300;
        this.teamsEnabled = true;

        objectives = new ObjectiveManager();
        tm = new TeamManager(plugin);

        onlinePlayers = new ArrayList<>();

        combatLog = new ConcurrentTwoWayMap<>();
        combatTasks = new ConcurrentHashMap<>();

        bedLog = new ConcurrentHashMap<>();

        teamCooldown = ConcurrentHashMap.newKeySet();
        scoreboardDisabled = ConcurrentHashMap.newKeySet();

        spawnLocationIndexToTeam = new ConcurrentHashMap<>();

        // If set in the config, the existing speedrun world (if applicable) will delete on startup
        if (plugin.getConfig().getBoolean("world.deleteOnStart")) {
            plugin.getLogger().info("Deleting old speedrun world...");
            deleteWorldFolder(worldFolder);
        }

        // Speedrun World is null until generated
        speedrunWorld = null;

        /* The max teams is equal to the number of teams defined in the config.
           We subtract 4 because the first 4 options under "teams" in the config
           are not actually teams, but configuration options that are handled differently
           (i.e., inventory, objectiveIncrement, teamPvP, & PvP) */
        ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");
        if (teamsSection != null) {
            this.maxTeams = teamsSection.getKeys(false).size() - 4;
        } else {
            this.maxTeams = 0;
        }

        teamSpawnLocations = new ArrayList<>();

    }

    // Default Seed Setter
    public void setSeed(String seed) {
        this.seed = seed;
    }

    // Default Seed Getter
    public String getSeed() {
        return seed;
    }

    // Default Border Setter - If Speedrun is already started, then border changes take effect immediately
    public void setBorder(int border) {
        this.border = border;
        if (isStarted) {
            createWorldBorder();
        }
    }

    // Default Border Getter
    public int getBorder() {
        return border;
    }

    // Default Time Limit Setter - If Speedrun is already started, then time limit changes take effect immediately
    public void setTimeLimit(int time) {
        this.timeLimit = time;
        if (isStarted) {
            ct.replace();
            ct = new Scoreboard(plugin, timeLimit, TimeUnit.MINUTES);
        }
    }

    // Default Time Limit Getter
    public int getTimeLimit() {
        return timeLimit;
    }

    // Default Time Unit Setter
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    // Default Time Unit Getter
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    // Default Spawn Radius Setter
    public void setSpawnRadius(int spawnRadius) {
        this.spawnRadius = spawnRadius;
    }

    // Default Time Radius Getter
    public int getSpawnRadius() {
        return spawnRadius;
    }

    // Add an objective to the objectives list in ObjectiveManager
    public void addObjective(Objective objective) {
        if (objective != null) {
            objectives.addObjective(objective);
        }
    }

    /* Remove an objective from the objective list in ObjectiveManager
       If total weight was explicitly set, it will remain unchanged
       If total weight is default, then total weight will decrease by the weight of the removed objective
       If a team has completed the objective & it is removed, the team's points will decrease by the weight of the objective
       If a team has not completed the objective, the game will check if they have enough points to win since conditions
       have changed */
    /// @param objectiveNum - Corresponds to the number displayed next to the objective in the admin objective book
    public void remObjective(int objectiveNum) {
        // Get objective corresponding to number
        Objective objective = objectives.getObjective(objectiveNum);

        // Remove that objective (reduce default calculated total weight by weight of specific objective)
        objectives.removeObjective(objectiveNum);

        // A game cannot continue if there are no objectives
        if (objectives.getLength() == 0) {
            endGame();
            return;
        }

        // For each team...
        for (Team team : tm.getTeams()) {

            // If the team has completed the removed objective
            if (team.getCompleteObjectives().contains(objective)) {

                // Remove that objective from the team's completed objectives
                team.getCompleteObjectives().remove(objective);

                // Remove the objective's weight from the team's points
                team.removePoints(objective.getWeight());

                /* Note: There is no condition where a team could win when the objective is removed if the team has
                   yet to win after they have completed the objective. */
            } else {

                /* If the team hasn't completed the objective, remove it from their incomplete objectives
                   objectives.getTotalWeight() automatically updates to account for this change. */
                team.getIncompleteObjectives().remove(objective);

                /* If the team's total points is now greater or equal to the total weight after the objective is
                   removed, then the team wins. */
                if (team.getPoints() >= getTotalWeight()) {
                    endGame(team);
                }
            }
        }
    }

    // Default ObjectiveManager Getter
    // TODO - getObjectives is misleading - this should maybe be changed to getObjectiveManager
    public ObjectiveManager getObjectives() {
        return objectives;
    }

    // Use the team manager to add a new team
    public void addTeam(Team team) {
        if (team != null) {
            tm.addTeam(team);
        }
    }

    // Use the team manager to remove a current team
    public void remTeam(Team team) {
        tm.removeTeam(team);
    }

    // Use the team manager to adjust team size limit
    public void setTeamSizeLimit(int sizeLimit) {
        tm.setSizeLimit(sizeLimit);
    }

    // Get the team size limit from the team manager
    public int getTeamSizeLimit() {
        return tm.getSizeLimit();
    }

    // Default Getter for TeamManager
    // TODO - Change this to getTeamManager
    public TeamManager getTeams() {
        return tm;
    }

    // Start the Game
    /// @param sender - This method is called in the CommandSpeedrun class and returns update messages to command sender
    /// based on certain conditions - it can be left null, then it just will not return any messages
    public Boolean setStarted(CommandSender sender) {

        // The game can only be started if it is not already started
        if (!this.isStarted) {

            // If the speedrun world is not yet generated, the sender will be instructed to do that first
            if (speedrunWorld == null) {
                if (sender != null) sender.sendMessage(plugin.getMessages().getWorldNotGenerated());
                return false;
            }

            // If no objectives are set, the sender will be instructed to set objectives first
            if (objectives.getObjectives().isEmpty()) {
                if (sender != null) sender.sendMessage(plugin.getMessages().getNoObjectives());
                return false;
            }

            /* If config game rules are enabled, they are implemented into the speedrun world once the game starts.
               Game rules are deleted with speedrun worlds. If you change these configurations, the changes will take
               effect the next time a speedrun is started using a new map (& plugin is reloaded). */
            if (plugin.getSpeedrunConfig().getBoolean("gameRules.enabled")) {

                // Loop through all game rules (pre-loaded when plugin first started)
                for (Map.Entry<GameRule<?>, Object> entry : plugin.getGameRules().entrySet()) {

                    // If the gamerule type is not boolean, then it must be integer
                    if (entry.getKey().getType() == Boolean.class) {
                        speedrunWorld.setGameRule((GameRule<Boolean>) entry.getKey(), (Boolean) entry.getValue());
                    } else {
                        speedrunWorld.setGameRule((GameRule<Integer>) entry.getKey(), (Integer) entry.getValue());
                    }

                    // Gamerules that are changed by the plugin are logged in the console
                    plugin.getLogger().info("Setting GameRule '" + entry.getKey() + "' to '" + entry.getValue()+ "'.");
                }
            }

            // Initialize the PlayerMoveEvent alternative listener
            new PlayerMove(plugin);

            // Initialize the Scoreboard
            ct = new Scoreboard(plugin, timeLimit, TimeUnit.MINUTES);

            // Assign players that have not picked a team to a team
            if (teamsEnabled) assignPlayers();

            // Clear all participating players' inventories
            for (Player p : tm.getAssignedPlayers()) {
                p.getInventory().clear();
            }

            /* For objectives that specifically have a "count" (i.e., must be completed x number of times before it
               counts as a completion. e.g., KILL 10 ZOMBIES), the objective actually holds the counts for all teams,
               rather than the team holding the count for all objectives. It doesn't make much sense to do it this way
               but that's how it was implemented. */
            // TODO - Look into mapping objective count in teams or justifying why the objective should hold this info
            for (Team team : tm.getTeams()) {
                for (Objective o : objectives.getObjectives()) {
                    if (o.getHasCount()) o.addTeam(team);
                }
            }

            // Spawn the teams in a circle around (0, 0) according to the team spawn radius
            // Teams spawn together in groups - players will always spawn seperately in SoloTeams
            TeamSpawner.spawnTeamsInCircle(this, teamSpawnLocations);

            // Broadcast that the game has started
            Bukkit.broadcast(plugin.getMessages().getStart(timeLimit));

            // Officially set isStarted to true; the game has officially started
            isStarted = true;

            // If this method returns true you know the game has successfully started
            return true;
        }

        // Return null if the game has already started
        return null;
    }


    // Generate the Speedrun Map
    /// @param sender - This method is called in the CommandSpeedrun class and returns update messages to command sender
    /// based on certain conditions - it can be left null, then it just will not return any messages
    public Boolean generateWorld(CommandSender sender) {

        // Cannot Generate a World if the game has already started
        if (isStarted) {
            sender.sendMessage(plugin.getMessages().getGameAlreadyStarted());
            return null;
        }

        /* If a world is already generated (but game is not started), the current world must
           be deleted to generate a new one. */
        if (speedrunWorld != null) {
            deleteSpeedrunWorld();
        }

        // The sender will be told when world generation initiates
        if (sender != null) {
            plugin.runAsyncTask(() -> sender.sendMessage(plugin.getMessages().getWorldGenerating()));
        }

        // Initialize edu.Kennesaw.Objects.Utilities.WorldGenerator
        WorldGenerator wg = new WorldGenerator();

        // Ensure that a data folder for the world exists
        File file = new File(plugin.getDataFolder() + "/speedrunworld");
        file.mkdirs();

        try {

            // Assume the seed is an integer (long) value first, generate world with that seed
            speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(Long.parseLong(seed)));

        } catch (NumberFormatException e) {

            /* If NumberFormatException is caught, then convert the string to a hash code (this is what default
               Minecraft does w/ string seeds according to my research) */
            speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(seed.hashCode()));

        }

        // Create the world border after the world finishes generating
        createWorldBorder();

        // Let the sender know world generation is finish - inform them spawn points are now being located
        if (sender != null) {
            sender.sendMessage(plugin.getMessages().getWorldGenerated());
            sender.sendMessage(plugin.getMessages().getSpawnsGenerating());
        }

        // maxTeams = number of teams defined in config, if teams are disabled # is 32
        // TODO - Make this number customizable via config.yml or otherwise
        if (!teamsEnabled) maxTeams = 32;

        /* TeamSpawnLocations can be located asynchronously - this is an intensive tasks, so it saves many resources
           for the main thread. We call getTeamSpawnLocations & Handle the Future Directly */
        TeamSpawner.getTeamSpawnLocations(plugin).thenAccept(locations -> {

            // Set teamSpawnLocations equal to locations returned
            teamSpawnLocations = locations;

            // We want to message the sender only after the asynchronous task is completed
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (sender != null) {
                    sender.sendMessage(plugin.getMessages().getSpawnsGenerated());
                }
            });
        }); // Ensures `thenAcceptAsync` runs on the main thread
        return false;
    }

    // Default Getter for Speedrun World
    public World getSpeedrunWorld() {
        return speedrunWorld;
    }

    // Default Getter for isStarted
    public boolean isStarted() {
        return isStarted;
    }

    // Default Setter for TeamSpawnLocations
    public void setTeamSpawnLocations(List<Location> locations) {
        this.teamSpawnLocations = locations;
    }

    // Force end the Game (no winner)
    public void endGame() {
        if (this.isStarted) {

            // set started to false
            isStarted = false;

            // stop countdown
            ct.stop();

            // broadcast game is over
            Bukkit.broadcast(plugin.getMessages().getForceStop());

            // display game over title (no winner)
            Title title = Title.title(plugin.getMessages().getGameOverTitle(null), Component.empty());
            displayTitleAndSound(title);

        }
    }

    // End game w/ winner - same as above but team is declared winner
    public void endGame(Team winner) {
        if (this.isStarted) {
            this.isStarted = false;
            ct.stop();
            Bukkit.broadcast(plugin.getMessages().getWinner(winner.getName()));
            Title title = Title.title(plugin.getMessages().getGameOverTitle(winner),
                    plugin.getMessages().getGameOverSubtitle(winner));
            displayTitleAndSound(title);
        }
    }

    // Same as above - winner is determined by team w/ most points
    // TODO - Handle tie cases
    public void endGameTimeExpired() {
        if (this.isStarted) {
            this.isStarted = false;
            Team winner = null;
            int points = 0;
            for (Team team : tm.getTeams()) {
                if (team.getPoints() > points) {
                    points = team.getPoints();
                    winner = team;
                }
            }
            Bukkit.broadcast(plugin.getMessages().getWinner(winner == null ? Component.text("UNDETERMINED") : winner.getName()));
            Title title = Title.title(plugin.getMessages().getGameOverTitle(winner), plugin.getMessages().getGameOverSubtitle(winner));
            displayTitleAndSound(title);
        }
    }

    // Default Setter for Total Weight
    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    /* If Total Weight has been specifically set, return that value. If total weight has not been set, return default
       value (= sum of the weight of all objectives) */
    public int getTotalWeight() {
        if (this.totalWeight != 0) {
            return totalWeight;
        }
        return objectives.getTotalWeight();
    }

    // Return true if totalWeight has been modified (thus, we have a weighted speedrun)
    public boolean isWeighted() {
        return this.totalWeight != 0;
    }

    // Create Teams
    /**
     *  @param onlinePlayers This function should be called with null as the argument in
     *                           standard situations, it is currently included for testing purposes
     *                           and will later be removed.
     */
    public void createTeams(Integer onlinePlayers) {

        // Teams must be enabled & at least 1 participating player online
        if (teamsEnabled && !this.onlinePlayers.isEmpty()) {

            // If teams are enabled, all abstract teams are indeed true teams
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());

            // This is why this function should always be called w/ "null" as the argument
            if (onlinePlayers == null) {
                onlinePlayers = this.onlinePlayers.size();
            }

            /* # of teams = ceil(participating players / team size limit):
               e.g., 10 participating players, size limit is 4: # teams = ceil(10/4) = ceil(2.5) = 3 */
            int numberOfTeams = (int) Math.ceil((double) onlinePlayers / getTeamSizeLimit());

            // Pull team configurations from the config.yml
            ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");

            // There must be teams defined in the config
            if (teamsSection == null) {
                plugin.getLogger().severe("No teams found in the configuration!");
                return;
            }

            // Get all top level keys under the "teams" configuration section
            Set<String> teamKeys = teamsSection.getKeys(false);

            // # of teams created = numberOfTeams, but can't be larger than this.maxTeams
            int maxTeams = Math.min(numberOfTeams, this.maxTeams);

            // If there are currently more teams than the maximum number of teams..
            if (tm.getTeams().size() > maxTeams) {

                // Get the team (or teams) that are marked for removal
                List<TrueTeam> teamsToRemove = new ArrayList<>(trueTeams.subList(maxTeams, tm.getTeams().size()));

                // Redistribute players on the teams marked for removal to current teams & then remove the team
                for (TrueTeam trueTeam : teamsToRemove) {
                    redistributePlayers(trueTeam, teamsToRemove);
                    remTeam(trueTeam);
                }
            }

            // Keep count, ensure we don't go over maxTeams
            int count = 0;

            // Loop through all the team keys
            for (String teamKey : teamKeys) {

                // Ignore the following - they are not teams
                if (teamKey.equals("inventory") || teamKey.equals("objectiveIncrement") || teamKey.equals("teamPvP")
                        || teamKey.equals("PvP")) {
                    continue;
                }

                // If over maxTeams, break off of loop
                if (count >= maxTeams) {
                    break;
                }

                // Get the name of the team
                Component teamName = plugin.getSpeedrunConfig().getComponent("teams." + teamKey + ".name");

                // Check if team already exists
                TrueTeam trueTeam = (TrueTeam) tm.getTeam(teamName);

                // If team already exists...
                if (trueTeam != null) {

                    // Update team item ItemMeta to reflect changes in teams (size limit or player count)
                    ItemStack item = trueTeam.getItem();
                    ItemMeta itemim = item.getItemMeta();
                    List<Component> lore = itemim.lore();

                    if (lore != null) {
                        if (trueTeam.isFull()) {
                            // If team is full...
                            lore.set(1, Component.text("This team is FULL!")
                                    .color(TextColor.fromHexString("#ff0000")).decorate(TextDecoration.BOLD));
                        } else {
                            // If team is not full display size/sizelimit
                            lore.set(1,Component.text( trueTeam.getSize() + "/" + tm.getSizeLimit() + " players on this team.")
                                    .color(TextColor.fromHexString("#c4c4c4")));
                        }
                        // Add all players on the team to the lore
                        for (Player p : trueTeam.getPlayers()) {
                            // The 1st (index 0) player on the team should be placed at index 2 of the lore (index 0 & 1 already being used)
                            lore.set(trueTeam.getPlayers().indexOf(p) + 2, ComponentHelper.mmStringToComponent("<gray> - " + p.getName() + "</gray>").decoration(TextDecoration.ITALIC, false));
                        }
                    }

                    // Update item meta's lore
                    itemim.lore(lore);

                    // Update item's item meta
                    item.setItemMeta(itemim);

                    // Update team's item
                    trueTeam.setItem(item);

                    // Increment the count & move on to the next team
                    count++;
                    continue;

                }

                // If the team doesn't exist...

                // Get the team item & lore from the config
                Component teamLore = plugin.getSpeedrunConfig().getComponent("teams." + teamKey + ".lore");
                String teamItem = plugin.getSpeedrunConfig().getString("teams." + teamKey + ".item");

                // Ensure no values are missing/null
                if (teamName == null || teamItem == null) {
                    plugin.getLogger().warning("Team '" + teamKey + "' is missing a name or item in the configuration.");
                    continue;
                }

                // Create a new ItemStack using the team's name, item, and lore:
                ItemStack teamItemStack = new ItemStack(Material.valueOf(teamItem));
                ItemMeta tim = teamItemStack.getItemMeta();
                tim.displayName(teamName);
                List<Component> lore = new ArrayList<>();

                // Index 0 of lore is what config defines
                lore.add(teamLore);

                // Index 1 of config is the team count (if the team is being created, it's 0/(size_limit))
                lore.add(Component.text( "0/" + tm.getSizeLimit() + " players on this team.")
                        .color(TextColor.fromHexString("#c4c4c4")));

                /* Since the team was just created, there are no players on it - we do not need to add current
                   members to the lore. */
                tim.lore(lore);

                // Update the ItemStack with the ItemMeta containing the name & lore
                teamItemStack.setItemMeta(tim);

                // Create a new team w/ this data
                trueTeam = new TrueTeam(plugin, teamName, teamItemStack);

                // Add the team using the team manager
                tm.addTeam(trueTeam);

                // Increment the count
                count++;

            }

            /* After looping through all teams, we can create the team inventory, which will display the team
               item to represent the team: player's can select the item to join the team. */
            tm.getTeamInventory().createTeamInventory();

            plugin.getLogger().info("Total teams created: " + tm.getTeams().size());

        }

    }

    // Redistribute Players to all teams excluding ones marked for removal
    private void redistributePlayers(TrueTeam trueTeam, List<TrueTeam> teamsToRemove) {
        if (teamsEnabled) {

            // Get & store all players on specific team marked for removal
            List<Player> playersToRedistribute = new ArrayList<>(trueTeam.getPlayers());

            // Clear the players
            trueTeam.getPlayers().clear();

            // Get all remaining TrueTeams
            List<TrueTeam> remainingTrueTeams = tm.convertAbstractToTeam(tm.getTeams());
            remainingTrueTeams.removeAll(teamsToRemove);

            /* Overflow team created in case all teams fill up & players can't be redistributed
               Note: I think the logic prevents this from ever being necessary, but all cases have not been tested
               This is implemented as a fallback incase for some reason this happens. */
            TrueTeam overflowTeam = (TrueTeam) tm.getTeam(Component.text("Overflow Team"));

            // Create a new Overflow Team if it doesn't exist
            if (overflowTeam == null) {
                overflowTeam = new TrueTeam(plugin, Component.text("Overflow Team"), new ItemStack(Material.BARRIER));
            }

            int teamIndex = 0;

            // Use sequential assignment to assign all players to the remaining teams evenly
            for (Player player : playersToRedistribute) {

                // Player is unassigned until a team is found
                boolean assigned = false;

                // Try to find a non-full team
                for (int attempts = 0; attempts < remainingTrueTeams.size(); attempts++) {

                    // Get team corresponding to index
                    TrueTeam targetTrueTeam = remainingTrueTeams.get(teamIndex);

                    // Increment Team Index whether team is full or not
                    teamIndex = (teamIndex + 1) % remainingTrueTeams.size();

                    if (!targetTrueTeam.isFull()) {
                        targetTrueTeam.addPlayer(player);
                        assigned = true;
                        // Exit the inner loop if player is assigned
                        break;
                    }
                }

                // If player cannot be reassigned to any existing teams, they are added to overflow
                if (!assigned) {
                    overflowTeam.addPlayer(player);
                    plugin.getLogger().warning("All teams are full. Player " + player.getName() + " added to the Overflow Team.");
                }
            }
        }
    }

    // Evenly assign players that are not on a team to a current team
    public void assignPlayers() {

        List<Player> assignedPlayers = tm.getAssignedPlayers();
        List<Player> noTeamPlayers = new ArrayList<>();

        // Get all players that are not on a team
        for (Player p : onlinePlayers) {
            if (!assignedPlayers.contains(p)) {
                noTeamPlayers.add(p);
            }
        }

        int teamIndex = 0;

        // Assign no team players to teams
        noTeamLoop(noTeamPlayers, teamIndex);

    }

    // Similar to above function, but allows subsets to be specified
    private void assignPlayers(List<Player> noTeamPlayers, int teamIndex) {
        noTeamLoop(noTeamPlayers, teamIndex);
    }

    // Default MaxTeams Getter
    public int getMaxTeams() {
        return maxTeams;
    }

    // Reset all attributes - this function essentially reruns the constructor, creating a fresh speedrun instance
    public void resetAttributes() {

        if (!isStarted) {

            // Attributes reset to default
            Random rand = new Random();
            this.seed = rand.nextInt() + "";
            this.border = 5000;
            this.timeLimit = 60;
            this.timeUnit = TimeUnit.MINUTES;
            this.spawnRadius = 300;
            this.teamsEnabled = true;
            this.totalWeight = 0;

            // Objectives cleared
            objectives.setTotalWeight(0);
            objectives.clearObjectives();

            // Teams reset
            tm.reset();

            // Teams created as normal
            createTeams(null);

            // Refresh Combat Log
            combatLog = new ConcurrentTwoWayMap<>();
            combatTasks = new ConcurrentHashMap<>();

            // Refresh Bed Log
            bedLog = new ConcurrentHashMap<>();

            // Refresh Team Cooldown
            teamCooldown = ConcurrentHashMap.newKeySet();

            // Delete Current Speedrun World (if exists)
            deleteSpeedrunWorld();

            // Reset Max Teams
            ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");
            if (teamsSection != null) {
                this.maxTeams = teamsSection.getKeys(false).size() - 4;
            } else {
                this.maxTeams = 0;
            }

            // Reset Team Spawn Locations
            teamSpawnLocations = new ArrayList<>();

            // Reset Scoreboards
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }

            ct = null;

            // Clear offline participant list
            plugin.setOfflineParticipants(new HashMap<>());
        }
    }

    // Default teamsEnabled Getter
    public boolean getTeamsEnabled() {
        return teamsEnabled;
    }

    // Set teams as enabled / disabled
    public void setTeamsEnabled(boolean teamsEnabled) {

        // Only if speedrun has not yet started
        if (!isStarted) {

            // Set actual boolean as true or false
            this.teamsEnabled = teamsEnabled;

            // Reset the team manager
            tm.reset();

            // If teams were disabled...
            if (!this.teamsEnabled) {

                // For every participating player..
                for (Player p : onlinePlayers) {

                    // Remove team compass, add them to solo team
                    p.getInventory().setItem(4, new ItemStack(Material.AIR));
                    SoloTeam soloTeam = new SoloTeam(plugin, p);
                    tm.addTeam(soloTeam);
                }

            // If teams were enabled...
            } else {

                // Create teams using participating players
                createTeams(null);

                // Add team selector to all players' inventories
                for (Player p : onlinePlayers) {
                    p.getInventory().setItem(4, Items.getTeamSelector());
                }
            }
        }
    }

    // Admins are not considered participants in a speedrun by default, this function toggles their participation
    public Boolean participate(Player player) {

        // Can only toggle participation if speedrun isn't started
        if (!isStarted) {

            // Return value: true by default, will change to false below if participation is disabled
            boolean isParticipating = true;

            // If teams are enabled...
            if (teamsEnabled) {

                // Check if player is currently on a team
                TrueTeam trueTeam = (TrueTeam) getTeams().getTeam(player);

                // If player is not currently participating...
                if (!onlinePlayers.contains(player)) {

                    // Add player to online players (set participation as true)
                    onlinePlayers.add(player);

                    // Add team selector compass
                    player.getInventory().setItem(4, Items.getTeamSelector());

                } else {

                    // If player is participating already...
                    player.getInventory().setItem(4, new ItemStack(Material.AIR));

                    // Remove player from team if applicable
                    if (trueTeam != null) {
                        trueTeam.removePlayer(player);
                    }

                    // Remove player from participating players
                    onlinePlayers.remove(player);

                    // Participation is false now
                    isParticipating = false;
                }

                // If adjustment causes a team to need to be added or removed...
                if (onlinePlayers.size() % getTeamSizeLimit() == 0 || getTeams()
                        .getTeamInventory().getInventory() == null) {

                    // Refresh the teams based on participating players
                    createTeams(null);

                } else if (!isParticipating) {
                    // or simply refresh team inventories if # of teams doesn't change
                    getTeams().getTeamInventory().updateTeamInventory(trueTeam);
                }

            // If teams are disabled...
            } else {

                // If player is not participating currently..
                if (!onlinePlayers.contains(player)) {

                    // Add player as participant
                    onlinePlayers.add(player);

                    // Create & add new player soloteam
                    SoloTeam st = new SoloTeam(plugin, player);
                    addTeam(st);

                //If player is participating...
                } else {

                    // Remove player from online participants & remove soloteam
                    SoloTeam st = (SoloTeam) getTeams().getTeam(player);
                    if (st != null) {
                        remTeam(st);
                        onlinePlayers.remove(player);

                        // participation is now false
                        isParticipating = false;
                    }
                }
            }
            // Return true if participation enabled, false if disabled
            return isParticipating;
        }
        // Return null if game is started
        return null;
    }

    // Default Getter for Team Spawn Locations
    public List<Location> getTeamSpawnLocations() {
        return teamSpawnLocations;
    }

    // Return true if onlinePlayers (online participants) contains the player
    public boolean isParticipating(Player p) {
        return onlinePlayers.contains(p);
    }

    // Update teamSpawnLocations list at location index to provided location
    public void setTeamSpawnLocation(int index, Location loc) throws IndexOutOfBoundsException {
        teamSpawnLocations.set(index, loc);

    }

    // Add a player to the team cooldown set
    public void addTeamCooldown(Player p) {

        teamCooldown.add(p);

        // Remove player from cooldown after x (defined in config) # of seconds have passed
        plugin.runAsyncDelayed(() -> teamCooldown.remove(p),
                tm.getInventoryCooldown(), TimeUnit.SECONDS);
    }

    // Default Getter for Team Cooldown
    public Set<Player> getTeamCooldown() {
        return teamCooldown;
    }

    // Default Getter for scoreboardDisabled
    public Set<Player> getScoreboardDisabled() { return scoreboardDisabled; }

    // Enable scoreboard if disabled, vice versa
    public boolean toggleScoreboard(Player p) {
        if (scoreboardDisabled.contains(p)) {
            scoreboardDisabled.remove(p);
            return true;
        }
        scoreboardDisabled.add(p);
        return false;
    }

    // Map a Team Index Number to a Specific Team (used in TeamSpawner)
    public void setSpawnLocationIndexToTeam(int index, Team team) {
        spawnLocationIndexToTeam.put(index, team);
    }

    // Get the team that is mapped to an index number
    public Team getTeamFromSpawnLocationIndex(int index) {
        if (isStarted) {
            return spawnLocationIndexToTeam.get(index);
        } return null;
    }

    // Delete the current speedrun world
    public void deleteSpeedrunWorld() {

        // Clear team spawn locations & mappings
        teamSpawnLocations = new ArrayList<>();
        spawnLocationIndexToTeam = new ConcurrentHashMap<>();

        // For all players (including non-participants)
        for (Player p : Bukkit.getOnlinePlayers()) {

            // If the player is in the speedrun world..
            if (p.getWorld().equals(speedrunWorld)) {

                // Try to teleport them to the spawn point set in config
                if (plugin.getSpawnPoint() != null) {
                    p.teleport(plugin.getSpawnPoint());
                } else {

                    // If spawnpoint is disabled, just teleport them to 0, 0 in "world".
                    p.teleport(new Location(Bukkit.getWorld("world"), 0, 150, 0));
                }
            }
        }

        // If speedrun world exists...
        if (speedrunWorld != null) {

            // Unload the world
            Bukkit.unloadWorld(speedrunWorld, false);
        }

        // Delete the world folder
        plugin.runAsyncTask(() -> deleteWorldFolder(worldFolder));

        // Set speedrunWorld to null
        speedrunWorld = null;
    }

    public Scoreboard getScoreboard() {
        return ct;
    }

    public void updateScoreboard(Scoreboard ct) {
        this.ct = ct;
    }

    // Assigns a list of players w/ no team evenly to existing teams
    private void noTeamLoop(List<Player> noTeamPlayers, int teamIndex) {

        if (teamsEnabled) {

            // Iterate through all teams - do not include overflow team if it exists
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());
            TrueTeam overFlow = (TrueTeam) tm.getTeam(Component.text("Overflow Team"));
            trueTeams.remove(overFlow);

            // Iterate through all no team players
            Iterator<Player> iterator = noTeamPlayers.iterator();

            // For each player...
            while (iterator.hasNext()) {

                Player player = iterator.next();

                // Get team that corresponds to current index
                TrueTeam trueTeam = trueTeams.get(teamIndex);

                // Ensure players aren't assigned to full teams
                if (trueTeam.getSize() > tm.getSizeLimit()) {
                    assignPlayers(noTeamPlayers, teamIndex + 1);
                    break;
                }

                // Add player to teamIndex (increments using sequential assignment)
                trueTeam.addPlayer(player);
                teamIndex = (teamIndex + 1) % tm.getTeams().size();
                iterator.remove();

            }

            // After all players are assigned to teams, balance the teams
            balanceTeams();
        }
    }

    private void balanceTeams() {
        if (teamsEnabled) {

            // Get all teams (excluding overflow)
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());
            TrueTeam overFlow = (TrueTeam) tm.getTeam(Component.text("Overflow Team"));
            trueTeams.remove(overFlow);

            // Sort teams in ascending order from smallest to largest team
            trueTeams.sort(Comparator.comparingInt(TrueTeam::getSize));

            // While team counts differ by more than one player...
            while (trueTeams.getLast().getSize() - trueTeams.getFirst().getSize() > 1) {

                // Remove the last player in the largest team
                Player playerToMove = trueTeams.getLast().getPlayers().remove(trueTeams.getLast().getSize() - 1);

                // Add the player to the smallest team
                trueTeams.getFirst().addPlayer(playerToMove);

                // Resort teams in ascending order from smallest to largest team
                trueTeams.sort(Comparator.comparingInt(TrueTeam::getSize));
            }
        }
    }

    /* Recursively delete all files within a folder from the smallest elements to the largest
       until the folder itself is deleted. */
    @SuppressWarnings("all")
    private Boolean deleteWorldFolder(File path) {
        if (path.exists()) {
            if (path.isDirectory()) {
                plugin.getLogger().info("Deleting directory: " + path.getName());
                try {
                    for (File file : path.listFiles()) {
                        deleteWorldFolder(file);
                    }
                } catch (NullPointerException e) {
                    return null;
                }
            }
            plugin.getLogger().info("Deleting file: " + path.getName());
            return path.delete();
        }
        return null;
    }

    // World Border Setup
    // TODO - World Border Attributes (warning distance, time, etc.) should be Configurable
    private void createWorldBorder() {

        WorldBorder worldBorder = getSpeedrunWorld().getWorldBorder();

        // Center is (0, 0)
        worldBorder.setCenter(0, 0);

        // Specified size set
        worldBorder.setSize(border);

        // Warning distance is five blocks
        worldBorder.setWarningDistance(5);

        // Warning time is 15 seconds - only applies if world border is reduced
        worldBorder.setWarningTime(15);

    }

    // Display the specified title to all players, and play the sound that is specified in the config.yml
    private void displayTitleAndSound(Title title) {
        Bukkit.getServer().showTitle(title);
        String soundString = plugin.getSpeedrunConfig().getString("title.sound");
        if (!soundString.isEmpty()) {
            Sound sound = Sound.sound(configurer -> {
                configurer.type(Key.key("minecraft", plugin.getSpeedrunConfig().getString("title.sound")));
                configurer.volume(16.0F);
                configurer.pitch(1.0F);
            });
            Bukkit.getServer().playSound(sound);
        }
    }
}
