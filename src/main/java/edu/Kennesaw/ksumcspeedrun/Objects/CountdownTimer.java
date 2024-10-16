package edu.Kennesaw.ksumcspeedrun.Objects;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class CountdownTimer {

    Main plugin;

    private Scoreboard scoreboard;
    private org.bukkit.scoreboard.Objective objective;
    private int timeLeftInSeconds;
    private int interval;

    public CountdownTimer(Main plugin, int timeInMinutes) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            this.plugin = plugin;
            if (plugin.getSpeedrunConfig().getBoolean("timer.disable")) return;
            this.timeLeftInSeconds = timeInMinutes * 60;
            interval = plugin.getSpeedrunConfig().getInt("timer.interval");
            setupScoreboard();
            startCountdown();
        });
    }

    public void stop() {
        this.timeLeftInSeconds = 0;
    }

    private void startCountdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeftInSeconds <= 0) {
                    updateScoreboardTimeUp();
                    plugin.getSpeedrun().endGameTimeExpired();
                    cancel();
                    return;
                }
                String formattedTime = String.format("%02d:%02d", timeLeftInSeconds / 60, timeLeftInSeconds % 60);
                updateScoreboard(formattedTime);
                timeLeftInSeconds = timeLeftInSeconds - interval;
            }
        }.runTaskTimer(plugin, 0L, interval * 20L);
    }

    private void setupScoreboard() {
        ScoreboardManager sbm = Bukkit.getScoreboardManager();
        scoreboard = sbm.getNewScoreboard();
        objective = scoreboard.registerNewObjective("timer", Criteria.DUMMY, plugin.getMessages().getTimerTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void updateScoreboard(String time) {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        Score timerScore = objective.getScore(plugin.getMessages().getTimeLeft(time));
        timerScore.setScore(1);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.setScoreboard(scoreboard);
        }
    }

    private void updateScoreboardTimeUp() {
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        Score timerScore = objective.getScore(plugin.getMessages().getGameOverMessage());
        timerScore.setScore(1);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.setScoreboard(scoreboard);
        }
    }

}
