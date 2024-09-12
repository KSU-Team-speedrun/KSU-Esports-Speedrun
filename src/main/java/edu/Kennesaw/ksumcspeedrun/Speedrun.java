package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objective.ObjectiveManager;
import org.bukkit.GameRule;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Speedrun {

    Main plugin;

    private long seed;
    private int border;
    private int timeLimit;
    private TimeUnit timeUnit;
    private int spawnRadius;

    private final ObjectiveManager objectives;
    private final HashMap<GameRule<?>, Boolean> gameRules;

    public Speedrun(Main plugin) {

        this.plugin = plugin;

        Random rand = new Random();
        this.seed = rand.nextInt();
        this.border = 5000;
        this.timeLimit = 60;
        this.timeUnit = TimeUnit.MINUTES;
        this.spawnRadius = 300;

        objectives = new ObjectiveManager();
        gameRules = new HashMap<GameRule<?>, Boolean>();

    }

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
        objectives.addObjective(objective);
    }

    public void remObjective(int objectiveNum) {
        objectives.removeObjective(objectiveNum);
    }

    public void setGameRule(GameRule<?> gameRule, boolean value) {
        gameRules.put(gameRule, value);
    }

    public boolean getGameRule(GameRule<?> gameRule) {
        return gameRules.getOrDefault(gameRule, false);
    }

}
