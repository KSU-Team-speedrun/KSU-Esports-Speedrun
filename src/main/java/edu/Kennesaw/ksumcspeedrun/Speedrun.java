package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Events.PlayerMove;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

/* Speedrun Object Class, centerpoint of logic for speedrun events
   Contains various setters & getters for all speedrun attributes */
public class Speedrun {

    Main plugin;

    // Speedrun Attributes
    private long seed;
    private int border;
    private int timeLimit;
    private TimeUnit timeUnit;
    private int spawnRadius;
    private int playerLimit;

    // "isVerified" meaning objectives can be completed on world seed within world border constraints
    private boolean isVerified;

    // True if the Speedrun has started
    private boolean isStarted;

    private int totalWeight = 0;

    // ObjectiveManager contains the list of all Objectives & all incomplete objectives, can be modified
    private final ObjectiveManager objectives;
    private final TeamManager tm;

    // GameRules set by admins will be located in this HashMap
    private final HashMap<GameRule<?>, Boolean> gameRules;

    public Map<UUID, Player> combatLog;
    public Map<UUID, ScheduledTask> combatTasks;

    public Map<Location, Player> bedLog;

    // Main Constructor with default attributes assigned
    public Speedrun(Main plugin) {

        this.plugin = plugin;

        Random rand = new Random();
        this.seed = rand.nextInt();
        this.border = 5000;
        this.timeLimit = 60;
        this.timeUnit = TimeUnit.MINUTES;
        this.spawnRadius = 300;

        objectives = new ObjectiveManager();
        tm = new TeamManager(plugin);
        gameRules = new HashMap<>();

        combatLog = new HashMap<UUID, Player>();
        combatTasks = new HashMap<UUID, ScheduledTask>();

        bedLog = new HashMap<Location, Player>();
    }

    // Setters & Getters below should be quite self-explanatory

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
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

    public void verifyMap() {
        // verify map
        isVerified = true;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setStarted() {
        new PlayerMove(plugin);
        isStarted = true;
        assignPlayers();
        Bukkit.broadcast(plugin.getSpeedrunConfig().getPrefix().append(Component.text("The speedrun has started!")));
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void endGame() {
        isStarted = false;
    }

    public void endGame(Team winner) {
        this.isStarted = false;
        Bukkit.broadcast(plugin.getSpeedrunConfig().getPrefix().append(Component.text(winner.getName() + " has won the speedrun!")));
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

    public void createTeams(Optional<Integer> onlinePlayersTest) {
        int numberOfTeams = onlinePlayersTest.map(integer -> (int) Math.ceil((double) integer / getTeamSizeLimit()))
                .orElseGet(() -> (int) Math.ceil((double) Bukkit.getServer().getOnlinePlayers().size()
                        / getTeamSizeLimit()));
        ConfigurationSection teamsSection = plugin.getConfig().getConfigurationSection("teams");

        if (teamsSection == null) {
            Bukkit.getLogger().severe("No teams found in the configuration!");
            return;
        }

        Set<String> teamKeys = teamsSection.getKeys(false);

        int maxTeams = Math.min(numberOfTeams, teamKeys.size());

        if (tm.getTeams().size() > maxTeams) {
            List<Team> teamsToRemove = new ArrayList<>(tm.getTeams().subList(maxTeams, tm.getTeams().size()));

            for (Team team : teamsToRemove) {
                redistributePlayers(team);
                tm.removeTeam(team);
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

            Team team = tm.getTeam(teamName);

            if (team != null) {
                ItemStack item = team.getItem();
                ItemMeta itemim = item.getItemMeta();
                List<Component> lore = itemim.lore();
                if (lore != null) {
                    lore.set(1,Component.text( team.getSize() + "/" + tm.getSizeLimit() + " players on this team.")
                            .color(TextColor.fromHexString("#c4c4c4")));
                }
                itemim.lore(lore);
                item.setItemMeta(itemim);
                team.setItem(item);
                count++;
                continue;
            }

            Component teamLore = plugin.getSpeedrunConfig().getComponent("teams." + teamKey + ".lore");
            String teamItem = plugin.getSpeedrunConfig().getString("teams." + teamKey + ".item");

            if (teamName == null || teamItem == null) {
                Bukkit.getLogger().warning("Team '" + teamKey + "' is missing a name or item in the configuration.");
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

            team = new Team(plugin, teamName, teamItemStack);
            tm.addTeam(team);

            count++;

        }

        tm.getTeamInventory().createTeamInventory();

        Bukkit.getLogger().info("Total teams created: " + tm.getTeams().size());

    }

    private void redistributePlayers(Team team) {
        List<Player> playersToRedistribute = new ArrayList<>(team.getPlayers());
        team.getPlayers().clear();

        List<Team> remainingTeams = tm.getTeams();

        int teamIndex = 0;
        for (Player player : playersToRedistribute) {
            Team targetTeam = remainingTeams.get(teamIndex);
            targetTeam.addPlayer(player);
            teamIndex = (teamIndex + 1) % remainingTeams.size();
        }
    }

    public void assignPlayers() {

        List<Player> assignedPlayers = tm.getAssignedPlayers();
        List<Player> noTeamPlayers = new ArrayList<>();
        Collection<?> onlinePlayers = Bukkit.getServer().getOnlinePlayers();

        for (Object o : onlinePlayers) {
            if (o instanceof Player p) {
                if (!assignedPlayers.contains(p)) {
                    noTeamPlayers.add(p);
                }
            }
        }

        int teamIndex = 0;

        for (Player player : noTeamPlayers) {
            Team team = tm.getTeams().get(teamIndex);
            if (team.getSize() > tm.getSizeLimit()) {
                assignPlayers(noTeamPlayers, teamIndex + 1);
                break;
            }
            team.addPlayer(player);
            teamIndex = (teamIndex + 1) % tm.getTeams().size();
            noTeamPlayers.remove(player);
        }

        balanceTeams();

    }

    private void assignPlayers(List<Player> noTeamPlayers, int teamIndex) {

        for (Player player : noTeamPlayers) {
            Team team = tm.getTeams().get(teamIndex);
            if (team.getSize() > tm.getSizeLimit()) {
                assignPlayers(noTeamPlayers, teamIndex + 1);
                break;
            }
            team.addPlayer(player);
            teamIndex = (teamIndex + 1) % tm.getTeams().size();
            noTeamPlayers.remove(player);
        }

        balanceTeams();

    }

    private void balanceTeams() {
        tm.getTeams().sort(Comparator.comparingInt(Team::getSize));

        while (tm.getTeams().getLast().getSize() - tm.getTeams().getFirst().getSize() > 1) {
            Player playerToMove =  tm.getTeams().getLast().getPlayers().remove(tm.getTeams().getLast().getSize() - 1);
            tm.getTeams().getFirst().addPlayer(playerToMove);

            tm.getTeams().sort(Comparator.comparingInt(Team::getSize));
        }
    }
}
