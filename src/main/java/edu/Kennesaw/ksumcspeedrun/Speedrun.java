package edu.Kennesaw.ksumcspeedrun;

import org.bukkit.GameRule;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Speedrun {

    Main plugin;

    private long seed;
    private int border;
    private int timeLimit;
    private TimeUnit timeUnit;
    private int spawnRadius;
    private List<String> objectives;
    private HashMap<GameRule, Boolean> gameRules;

    public Speedrun(Main plugin) {
        this.plugin = plugin;
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

}
