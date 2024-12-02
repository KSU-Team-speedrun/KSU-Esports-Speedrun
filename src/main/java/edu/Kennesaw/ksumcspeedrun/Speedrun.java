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
            ct = new Scoreboard(plugin, timeLimit);
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
            ct = new Scoreboard(plugin, timeLimit);

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

    public World getSpeedrunWorld() {
        return speedrunWorld;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setTeamSpawnLocations(List<Location> locations) {
        this.teamSpawnLocations = locations;
    }

    public void endGame() {
        if (this.isStarted) {
            isStarted = false;
            ct.stop();
            Bukkit.broadcast(plugin.getMessages().getForceStop());
            Title title = Title.title(plugin.getMessages().getGameOverTitle(null), Component.empty());
            displayTitleAndSound(title);
        }
    }

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

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public int getTotalWeight() {
        if (this.totalWeight != 0) {
            return totalWeight;
        }
        return objectives.getTotalWeight();
    }

    public boolean isWeighted() {
        return this.totalWeight != 0;
    }

    /**
     *  @param onlinePlayers This function should be called with null as the argument in
     *                           standard situations, it is currently included for testing purposes
     *                           and will later be removed.
     */
    public void createTeams(Integer onlinePlayers) {

        if (teamsEnabled && !this.onlinePlayers.isEmpty()) {

            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());

            if (onlinePlayers == null) {
                onlinePlayers = this.onlinePlayers.size();
            }
            int numberOfTeams = (int) Math.ceil((double) onlinePlayers / getTeamSizeLimit());

            ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");

            if (teamsSection == null) {
                plugin.getLogger().severe("No teams found in the configuration!");
                return;
            }

            Set<String> teamKeys = teamsSection.getKeys(false);

            int maxTeams = Math.min(numberOfTeams, this.maxTeams);

            if (tm.getTeams().size() > maxTeams) {
                List<TrueTeam> teamsToRemove = new ArrayList<>(trueTeams.subList(maxTeams, tm.getTeams().size()));

                for (TrueTeam trueTeam : teamsToRemove) {
                    redistributePlayers(trueTeam);
                    remTeam(trueTeam);
                }
            }

            int count = 0;
            for (String teamKey : teamKeys) {

                if (teamKey.equals("inventory") || teamKey.equals("objectiveIncrement") || teamKey.equals("teamPvP")
                        || teamKey.equals("PvP")) {
                    continue;
                }

                if (count >= maxTeams) {
                    break;
                }

                Component teamName = plugin.getSpeedrunConfig().getComponent("teams." + teamKey + ".name");

                TrueTeam trueTeam = (TrueTeam) tm.getTeam(teamName);

                if (trueTeam != null) {
                    ItemStack item = trueTeam.getItem();
                    ItemMeta itemim = item.getItemMeta();
                    List<Component> lore = itemim.lore();
                    if (lore != null) {
                        if (trueTeam.isFull()) {
                            lore.set(1, Component.text("This team is FULL!")
                                    .color(TextColor.fromHexString("#ff0000")).decorate(TextDecoration.BOLD));
                        } else {
                            lore.set(1,Component.text( trueTeam.getSize() + "/" + tm.getSizeLimit() + " players on this team.")
                                    .color(TextColor.fromHexString("#c4c4c4")));
                        }
                        for (Player p : trueTeam.getPlayers()) {
                            lore.set(trueTeam.getPlayers().indexOf(p) + 2, ComponentHelper.mmStringToComponent("<gray> - " + p.getName() + "</gray>").decoration(TextDecoration.ITALIC, false));
                        }
                    }
                    itemim.lore(lore);
                    item.setItemMeta(itemim);
                    trueTeam.setItem(item);
                    count++;
                    continue;
                }

                Component teamLore = plugin.getSpeedrunConfig().getComponent("teams." + teamKey + ".lore");
                String teamItem = plugin.getSpeedrunConfig().getString("teams." + teamKey + ".item");

                if (teamName == null || teamItem == null) {
                    plugin.getLogger().warning("Team '" + teamKey + "' is missing a name or item in the configuration.");
                    continue;
                }

                ItemStack teamItemStack = new ItemStack(Material.valueOf(teamItem));
                ItemMeta tim = teamItemStack.getItemMeta();
                tim.displayName(teamName);
                List<Component> lore = new ArrayList<>();
                lore.add(teamLore);
                lore.add(Component.text( "0/" + tm.getSizeLimit() + " players on this team.")
                        .color(TextColor.fromHexString("#c4c4c4")));
                tim.lore(lore);
                teamItemStack.setItemMeta(tim);

                trueTeam = new TrueTeam(plugin, teamName, teamItemStack);
                tm.addTeam(trueTeam);

                count++;

            }

            tm.getTeamInventory().createTeamInventory();

            plugin.getLogger().info("Total teams created: " + tm.getTeams().size());

        }

    }

    private void redistributePlayers(TrueTeam trueTeam) {
        if (teamsEnabled) {
            List<Player> playersToRedistribute = new ArrayList<>(trueTeam.getPlayers());
            trueTeam.getPlayers().clear();

            List<TrueTeam> remainingTrueTeams = tm.convertAbstractToTeam(tm.getTeams());

            int teamIndex = 0;
            for (Player player : playersToRedistribute) {
                TrueTeam targetTrueTeam = remainingTrueTeams.get(teamIndex);
                targetTrueTeam.addPlayer(player);
                teamIndex = (teamIndex + 1) % remainingTrueTeams.size();
            }
        }
    }

    public void assignPlayers() {

        List<Player> assignedPlayers = tm.getAssignedPlayers();
        List<Player> noTeamPlayers = new ArrayList<>();

        for (Player p : onlinePlayers) {
            if (!assignedPlayers.contains(p)) {
                noTeamPlayers.add(p);
            }
        }

        int teamIndex = 0;

        noTeamLoop(noTeamPlayers, teamIndex);

    }

    public int getMaxTeams() {
        return maxTeams;
    }

    public void resetAttributes() {

        if (!isStarted) {
            Random rand = new Random();
            this.seed = rand.nextInt() + "";
            this.border = 5000;
            this.timeLimit = 60;
            this.timeUnit = TimeUnit.MINUTES;
            this.spawnRadius = 300;
            this.teamsEnabled = true;
            this.totalWeight = 0;

            objectives.clearObjectives();
            tm.reset();
            createTeams(null);

            combatLog = new ConcurrentTwoWayMap<>();
            combatTasks = new ConcurrentHashMap<>();

            bedLog = new ConcurrentHashMap<>();

            teamCooldown = ConcurrentHashMap.newKeySet();

            deleteSpeedrunWorld();

            ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");
            if (teamsSection != null) {
                this.maxTeams = teamsSection.getKeys(false).size() - 4;
            } else {
                this.maxTeams = 0;
            }

            teamSpawnLocations = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }

            ct = null;

        }
    }

    public boolean getTeamsEnabled() {
        return teamsEnabled;
    }

    public void setTeamsEnabled(boolean teamsEnabled) {
        if (!isStarted) {
            this.teamsEnabled = teamsEnabled;
            tm.reset();
            if (!this.teamsEnabled) {
                for (Player p : onlinePlayers) {
                    p.getInventory().setItem(4, new ItemStack(Material.AIR));
                    SoloTeam soloTeam = new SoloTeam(plugin, p);
                    tm.addTeam(soloTeam);
                }
            } else {
                createTeams(null);
                for (Player p : onlinePlayers) {
                    p.getInventory().setItem(4, Items.getTeamSelector());
                }
            }
        }
    }

    public Boolean participate(Player player) {
        if (!isStarted) {
            boolean isParticipating = true;
            if (teamsEnabled) {
                TrueTeam trueTeam = (TrueTeam) getTeams().getTeam(player);
                if (!onlinePlayers.contains(player)) {
                    onlinePlayers.add(player);
                    player.getInventory().setItem(4, Items.getTeamSelector());
                } else {
                    player.getInventory().setItem(4, new ItemStack(Material.AIR));
                    if (trueTeam != null) {
                        trueTeam.removePlayer(player);
                    }
                    onlinePlayers.remove(player);
                    isParticipating = false;
                }
                if (onlinePlayers.size() % getTeamSizeLimit() == 0 || getTeams()
                        .getTeamInventory().getInventory() == null) {
                    createTeams(null);
                } else if (!isParticipating) {
                    getTeams().getTeamInventory().updateTeamInventory(trueTeam);
                }
            } else {
                if (!onlinePlayers.contains(player)) {
                    onlinePlayers.add(player);
                    SoloTeam st = new SoloTeam(plugin, player);
                    addTeam(st);
                } else {
                    SoloTeam st = (SoloTeam) getTeams().getTeam(player);
                    if (st != null) {
                        remTeam(st);
                        onlinePlayers.remove(player);
                        isParticipating = false;
                    }
                }
            }
            return isParticipating;
        }
        return null;
    }

    public List<Location> getTeamSpawnLocations() {
        return teamSpawnLocations;
    }

    public boolean isParticipating(Player p) {
        return onlinePlayers.contains(p);
    }

    public void setTeamSpawnLocation(int index, Location loc) throws IndexOutOfBoundsException {
        teamSpawnLocations.set(index, loc);

    }

    public void addTeamCooldown(Player p) {
        teamCooldown.add(p);
        plugin.runAsyncDelayed(() -> teamCooldown.remove(p),
                tm.getInventoryCooldown(), TimeUnit.SECONDS);
    }

    public Set<Player> getTeamCooldown() {
        return teamCooldown;
    }

    public Set<Player> getScoreboardDisabled() { return scoreboardDisabled; }

    public boolean toggleScoreboard(Player p) {
        if (scoreboardDisabled.contains(p)) {
            scoreboardDisabled.remove(p);
            return true;
        }
        scoreboardDisabled.add(p);
        return false;
    }

    public void setSpawnLocationIndexToTeam(int index, Team team) {
        spawnLocationIndexToTeam.put(index, team);
    }

    public Team getTeamFromSpawnLocationIndex(int index) {
        if (isStarted) {
            return spawnLocationIndexToTeam.get(index);
        } return null;
    }

    public void deleteSpeedrunWorld() {
        teamSpawnLocations = new ArrayList<>();
        spawnLocationIndexToTeam = new ConcurrentHashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(speedrunWorld)) {
                if (plugin.getSpawnPoint() != null) {
                    p.teleport(plugin.getSpawnPoint());
                } else {
                    p.teleport(new Location(Bukkit.getWorld("world"), 0, 150, 0));
                }
            }
        }
        if (speedrunWorld != null) {
            Bukkit.unloadWorld(speedrunWorld, false);
        }
        plugin.runAsyncTask(() -> deleteWorldFolder(worldFolder));
        speedrunWorld = null;
    }

    private void noTeamLoop(List<Player> noTeamPlayers, int teamIndex) {
        if (teamsEnabled) {
            Iterator<Player> iterator = noTeamPlayers.iterator();
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());
            while (iterator.hasNext()) {
                Player player = iterator.next();
                TrueTeam trueTeam = trueTeams.get(teamIndex);
                if (trueTeam.getSize() > tm.getSizeLimit()) {
                    assignPlayers(noTeamPlayers, teamIndex + 1);
                    break;
                }
                trueTeam.addPlayer(player);
                teamIndex = (teamIndex + 1) % tm.getTeams().size();
                iterator.remove();
            }
            balanceTeams();
        }
    }


    private void assignPlayers(List<Player> noTeamPlayers, int teamIndex) {

        noTeamLoop(noTeamPlayers, teamIndex);

    }

    private void balanceTeams() {
        if (teamsEnabled) {
            List<TrueTeam> trueTeams = tm.convertAbstractToTeam(tm.getTeams());
            trueTeams.sort(Comparator.comparingInt(TrueTeam::getSize));
            while (trueTeams.getLast().getSize() - trueTeams.getFirst().getSize() > 1) {
                Player playerToMove = trueTeams.getLast().getPlayers().remove(trueTeams.getLast().getSize() - 1);
                trueTeams.getFirst().addPlayer(playerToMove);
                trueTeams.sort(Comparator.comparingInt(TrueTeam::getSize));
            }
        }
    }

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

    private void createWorldBorder() {
        WorldBorder worldBorder = getSpeedrunWorld().getWorldBorder();
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(border);
        worldBorder.setWarningDistance(5);
        worldBorder.setWarningTime(15);
    }

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
