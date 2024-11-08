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
    private String seed;
    private int border;
    private int timeLimit;
    private TimeUnit timeUnit;
    private int spawnRadius;
    private int playerLimit;
    private World speedrunWorld;
    private int maxTeams;

    // True if the Speedrun has started
    private boolean isStarted;

    private int totalWeight = 0;

    // ObjectiveManager contains the list of all Objectives & all incomplete objectives, can be modified
    private final ObjectiveManager objectives;
    private final TeamManager tm;

    private Scoreboard ct;

    // GameRules set by admins will be located in this HashMap
    private Map<GameRule<?>, Boolean> gameRules;

    public ConcurrentTwoWayMap<UUID, Player> combatLog;
    public Map<UUID, ScheduledTask> combatTasks;

    public Map<Location, Player> bedLog;

    private Set<Player> teamCooldown;

    // Admins are left unincluded from games and team calculations unless they specify
    private final List<Player> onlinePlayers;
    private boolean teamsEnabled;

    private final Set<Player> scoreboardDisabled;

    final File worldFolder = new File(Bukkit.getWorldContainer(), "speedrunworld");

    List<Location> teamSpawnLocations;
    Map<Integer, Team> spawnLocationIndexToTeam;

    // Main Constructor with default attributes assigned
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

        gameRules = new ConcurrentHashMap<>();

        combatLog = new ConcurrentTwoWayMap<>();
        combatTasks = new ConcurrentHashMap<>();

        bedLog = new ConcurrentHashMap<>();

        teamCooldown = ConcurrentHashMap.newKeySet();
        scoreboardDisabled = ConcurrentHashMap.newKeySet();

        spawnLocationIndexToTeam = new ConcurrentHashMap<>();

        if (plugin.getConfig().getBoolean("world.deleteOnStart")) {
            plugin.getLogger().info("Deleting old speedrun world...");
            deleteWorldFolder(worldFolder);
        }

        speedrunWorld = null;

        ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");
        if (teamsSection != null) {
            this.maxTeams = teamsSection.getKeys(false).size() - 4;
        } else {
            this.maxTeams = 0;
        }

        teamSpawnLocations = new ArrayList<>();

    }

    // Setters & Getters below should be quite self-explanatory

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getSeed() {
        return seed;
    }

    public void setBorder(int border) {
        this.border = border;
        if (isStarted) {
            createWorldBorder();
        }
    }

    public int getBorder() {
        return border;
    }

    public void setTimeLimit(int time) {
        this.timeLimit = time;
        if (isStarted) {
            ct.replace();
            ct = new Scoreboard(plugin, timeLimit);
        }
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setSpawnRadius(int spawnRadius) {
        this.spawnRadius = spawnRadius;
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public void addObjective(Objective objective) {
        if (objective != null) {
            objectives.addObjective(objective);
        }
    }

    public void remObjective(int objectiveNum) {
        Objective objective = objectives.getObjective(objectiveNum);
        objectives.removeObjective(objectiveNum);
        if (objectives.getLength() == 0) {
            endGame();
            return;
        }
        for (Team team : tm.getTeams()) {
            if (team.getCompleteObjectives().contains(objective)) {
                team.getCompleteObjectives().remove(objective);
                team.removePoints(objective.getWeight());
            } else {
                team.getIncompleteObjectives().remove(objective);
                if (team.getPoints() >= getTotalWeight()) {
                    endGame(team);
                }
            }
        }
    }

    public ObjectiveManager getObjectives() {
        return objectives;
    }

    public void addTeam(Team team) {
        if (team != null) {
            tm.addTeam(team);
        }
    }

    public void remTeam(Team team) {
        tm.removeTeam(team);
    }

    public void setTeamSizeLimit(int sizeLimit) {
        tm.setSizeLimit(sizeLimit);
    }

    public int getTeamSizeLimit() {
        return tm.getSizeLimit();
    }

    public TeamManager getTeams() {
        return tm;
    }

    public void setGameRule(GameRule<?> gameRule, boolean value) {
        gameRules.put(gameRule, value);
    }

    public boolean getGameRule(GameRule<?> gameRule) {
        return gameRules.getOrDefault(gameRule, false);
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public Boolean setStarted(CommandSender sender) {
        if (!this.isStarted) {
            if (speedrunWorld == null) {
                sender.sendMessage(plugin.getMessages().getWorldNotGenerated());
                return false;
            }
            if (objectives.getObjectives().isEmpty()) {
                sender.sendMessage(plugin.getMessages().getNoObjectives());
                return false;
            }
            if (plugin.getSpeedrunConfig().getBoolean("gameRules.enabled")) {
                for (Map.Entry<GameRule<?>, Object> entry : plugin.getGameRules().entrySet()) {
                    if (entry.getKey().getType() == Boolean.class) {
                        speedrunWorld.setGameRule((GameRule<Boolean>) entry.getKey(), (Boolean) entry.getValue());
                    } else {
                        speedrunWorld.setGameRule((GameRule<Integer>) entry.getKey(), (Integer) entry.getValue());
                    }
                    plugin.getLogger().info("Setting GameRule '" + entry.getKey() + "' to '" + entry.getValue()+ "'.");
                }
            }
            new PlayerMove(plugin);
            ct = new Scoreboard(plugin, timeLimit);
            if (teamsEnabled) assignPlayers();
            for (Player p : tm.getAssignedPlayers()) {
                p.getInventory().clear();
            }
            for (Team team : tm.getTeams()) {
                for (Objective o : objectives.getObjectives()) {
                    if (o.getHasCount()) o.addTeam(team);
                }
            }
            TeamSpawner.spawnTeamsInCircle(this, teamSpawnLocations);
            Bukkit.broadcast(plugin.getMessages().getStart(timeLimit));
            isStarted = true;
            return true;
        }
        return null;
    }

    public Boolean generateWorld(CommandSender sender) {
        if (isStarted) {
            sender.sendMessage(plugin.getMessages().getGameAlreadyStarted());
            return null;
        }
        if (speedrunWorld != null) {
            deleteSpeedrunWorld();
        }
        if (sender != null) {
            plugin.runAsyncTask(() -> sender.sendMessage(plugin.getMessages().getWorldGenerating()));
        }
        WorldGenerator wg = new WorldGenerator();
        File file = new File(plugin.getDataFolder() + "/speedrunworld");
        file.mkdirs();
        try {
            speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(Long.parseLong(seed)));
        } catch (NumberFormatException e) {
            speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(seed.hashCode()));
        }
        createWorldBorder();
        if (sender != null) {
            sender.sendMessage(plugin.getMessages().getWorldGenerated());
            sender.sendMessage(plugin.getMessages().getSpawnsGenerating());
        }
        if (!teamsEnabled) maxTeams = 32;
        // Call getTeamSpawnLocations and handle the future directly
        TeamSpawner.getTeamSpawnLocations(plugin).thenAccept(locations -> {
            teamSpawnLocations = locations;

            // Schedule the next steps to run on the main thread
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

            gameRules = new ConcurrentHashMap<>();

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
