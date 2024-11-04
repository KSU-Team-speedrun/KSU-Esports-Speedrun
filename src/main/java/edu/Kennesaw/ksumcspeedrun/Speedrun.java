package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Events.PlayerMove;
import edu.Kennesaw.ksumcspeedrun.Objects.CountdownTimer;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.*;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import edu.Kennesaw.ksumcspeedrun.Utilities.WorldGenerator;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    // True if the Speedrun has started
    private boolean isStarted;

    private int totalWeight = 0;

    // ObjectiveManager contains the list of all Objectives & all incomplete objectives, can be modified
    private final ObjectiveManager objectives;
    private final TeamManager tm;

    private CountdownTimer ct;

    // GameRules set by admins will be located in this HashMap
    private Map<GameRule<?>, Boolean> gameRules;

    public Map<UUID, Player> combatLog;
    public Map<UUID, ScheduledTask> combatTasks;

    public Map<Location, Player> bedLog;

    public Set<Player> teamCooldown;

    // Admins are left unincluded from games and team calculations unless they specify
    private final List<Player> onlinePlayers;
    private boolean teamsEnabled;

    final File worldFolder = new File(Bukkit.getWorldContainer(), "speedrunworld");

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

        combatLog = new ConcurrentHashMap<>();
        combatTasks = new ConcurrentHashMap<>();

        bedLog = new ConcurrentHashMap<>();

        teamCooldown = ConcurrentHashMap.newKeySet();

        if (plugin.getConfig().getBoolean("world.deleteOnStart")) {
            plugin.getLogger().info("Deleting old speedrun world...");
            deleteWorldFolder(worldFolder);
        }

        speedrunWorld = null;

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
    }

    public int getBorder() {
        return border;
    }

    public void setTimeLimit(int time) {
        this.timeLimit = time;
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
        objectives.removeObjective(objectiveNum);
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
                if (sender != null) {
                    Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                        sender.sendMessage(plugin.getMessages().getWorldGenerating());
                    });
                }
                WorldGenerator wg = new WorldGenerator();
                try {
                    speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(Long.parseLong(seed)));
                } catch (NumberFormatException e) {
                    speedrunWorld = Bukkit.createWorld(new WorldCreator("speedrunworld").seed(seed.hashCode()));
                }
                return false;
            }
            isStarted = true;
            new PlayerMove(plugin);
            ct = new CountdownTimer(plugin, timeLimit);
            if (teamsEnabled) assignPlayers();
            for (Player p : tm.getAssignedPlayers()) {
                p.getInventory().clear();
            }
            TeamSpawner.spawnTeamsInCircle(speedrunWorld, tm, spawnRadius, teamsEnabled);
            Bukkit.broadcast(plugin.getMessages().getStart(timeLimit));
            return true;
        }
        return null;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void endGame() {
        if (this.isStarted) {
            isStarted = false;
            ct.stop();
            Bukkit.broadcast(plugin.getMessages().getForceStop());
        }
    }

    public void endGame(Team winner) {
        if (this.isStarted) {
            this.isStarted = false;
            ct.stop();
            Bukkit.broadcast(plugin.getMessages().getWinner(winner.getName()));
        }
    }

    public void endGameTimeExpired() {
        if (this.isStarted) {
            this.isStarted = false;
            if (this.teamsEnabled) {
                Team winner = null;
                int points = 0;
                for (Team team : tm.getTeams()) {
                    if (team.getPoints() > points) {
                        points = team.getPoints();
                        winner = team;
                    }
                }
                if (winner != null) {
                    Bukkit.broadcast(plugin.getMessages().getTimeUp(winner.getName()));
                }
            }
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

            int maxTeams = Math.min(numberOfTeams, teamKeys.size());

            if (tm.getTeams().size() > maxTeams) {
                List<TrueTeam> teamsToRemove = new ArrayList<>(trueTeams.subList(maxTeams, tm.getTeams().size()));

                for (TrueTeam trueTeam : teamsToRemove) {
                    redistributePlayers(trueTeam);
                    remTeam(trueTeam);
                }
            }

            int count = 0;
            for (String teamKey : teamKeys) {

                if (teamKey.equals("inventory")) {
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

    public void resetAttributes() {
        if (!isStarted) {
            Random rand = new Random();
            this.seed = rand.nextInt() + "";
            this.border = 5000;
            this.timeLimit = 60;
            this.timeUnit = TimeUnit.MINUTES;
            this.spawnRadius = 300;
            this.teamsEnabled = true;

            objectives.clearObjectives();
            tm.reset();
            createTeams(null);

            gameRules = new ConcurrentHashMap<>();

            combatLog = new ConcurrentHashMap<>();
            combatTasks = new ConcurrentHashMap<>();

            bedLog = new ConcurrentHashMap<>();

            teamCooldown = ConcurrentHashMap.newKeySet();

            if (speedrunWorld != null) {
                Bukkit.unloadWorld(speedrunWorld, false);
            }

            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                deleteWorldFolder(worldFolder);
            });

            speedrunWorld = null;

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

    public boolean isParticipating(Player p) {
        return onlinePlayers.contains(p);
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
                Player playerToMove =  trueTeams.getLast().getPlayers().remove(trueTeams.getLast().getSize() - 1);
                trueTeams.getFirst().addPlayer(playerToMove);
                trueTeams.sort(Comparator.comparingInt(TrueTeam::getSize));
            }
        }
    }

    private void deleteWorldFolder(File path) {
        if (path.exists()) {
            if (path.isDirectory()) {
                plugin.getLogger().info("Deleting directory: " + path.getName());
                for (File file : path.listFiles()) {
                    deleteWorldFolder(file);
                }
            }
            path.delete();
            plugin.getLogger().info("Deleting file: " + path.getName());
        }
    }
}
