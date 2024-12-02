package edu.Kennesaw.ksumcspeedrun.Objects;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Scoreboard {

    private final Main plugin;
    private final TeamManager teamManager;
    private final Map<Team, org.bukkit.scoreboard.Scoreboard> teamScoreboards;
    private final int interval;
    private int timeLeftInSeconds;
    private boolean replaced;

    public Scoreboard(Main plugin, int time, TimeUnit tu) {
        this.plugin = plugin;
        this.teamManager = plugin.getSpeedrun().getTeams();
        this.teamScoreboards = new HashMap<>();
        if (tu.equals(TimeUnit.MINUTES)) {
            this.timeLeftInSeconds = time * 60;
        } else if (tu.equals(TimeUnit.SECONDS)) {
            this.timeLeftInSeconds = time;
        }
        this.interval = plugin.getSpeedrunConfig().getInt("scoreboard.interval");
        replaced = false;

        if (!plugin.getSpeedrunConfig().getBoolean("scoreboard.disable")) {
            setupScoreboards();
            startCountdown();
        }
    }

    public void stop() {
        this.timeLeftInSeconds = 0;
    }

    public void replace() { this.replaced = true; }

    private void startCountdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeftInSeconds <= 0) {
                    updateScoreboards();
                    plugin.getSpeedrun().endGameTimeExpired();
                    cancel();
                    return;
                }
                if (replaced) {
                    cancel();
                    return;
                }
                String formattedTime = String.format("%02d:%02d", timeLeftInSeconds / 60, timeLeftInSeconds % 60);
                updateScoreboards(formattedTime);
                timeLeftInSeconds -= interval;
            }
        }.runTaskTimer(plugin, 0L, interval * 20L);
    }

    public org.bukkit.scoreboard.Scoreboard getTeamScoreboard(Team team) {
        return teamScoreboards.get(team);
    }

    private void setupScoreboards() {
        ScoreboardManager sbm = Bukkit.getScoreboardManager();

        for (Team team : teamManager.getTeams()) {
            // Create a new scoreboard and objective for each team
            org.bukkit.scoreboard.Scoreboard teamScoreboard = sbm.getNewScoreboard();
            Objective teamObjective = teamScoreboard.registerNewObjective("timer_" + team.getName(), Criteria.DUMMY, plugin.getMessages().getTimerTitle());
            teamObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

            // Store the team scoreboard in the map
            teamScoreboards.put(team, teamScoreboard);
        }
    }

    private void updateScoreboards(String time) {
        for (Map.Entry<Team, org.bukkit.scoreboard.Scoreboard> entry : teamScoreboards.entrySet()) {
            Team team = entry.getKey();
            org.bukkit.scoreboard.Scoreboard teamScoreboard = entry.getValue();
            Objective teamObjective = teamScoreboard.getObjective(DisplaySlot.SIDEBAR);

            if (teamObjective != null) {
                // Clear previous scores
                teamScoreboard.getEntries().forEach(teamScoreboard::resetScores);

                // Update the timer
                Score timerScore = teamObjective.getScore(plugin.getMessages().getTimeLeft(time));
                timerScore.setScore(2);

                // Update team points
                String pointsMessage = plugin.getMessages().getPointsMessage(team.getPoints(), plugin.getSpeedrun().getTotalWeight());
                Score pointsScore = teamObjective.getScore(pointsMessage);
                pointsScore.setScore(1);

                // Assign this team’s scoreboard to each player on the team
                if (team instanceof TrueTeam tt) {
                    for (Player player : tt.getPlayers()) {
                        if (!plugin.getSpeedrun().getScoreboardDisabled().contains(player)) {
                            player.setScoreboard(teamScoreboard);
                        }
                        player.sendActionBar(tt.getName());
                    }
                } else {
                    SoloTeam st = (SoloTeam) team;
                    if (!plugin.getSpeedrun().getScoreboardDisabled().contains(st.getPlayer())) {
                        st.setScoreboard(teamScoreboard);
                    }
                }
            }
        }
    }

    private void updateScoreboards() {
        for (Map.Entry<Team, org.bukkit.scoreboard.Scoreboard> entry : teamScoreboards.entrySet()) {
            Team team = entry.getKey();
            org.bukkit.scoreboard.Scoreboard teamScoreboard = entry.getValue();
            Objective teamObjective = teamScoreboard.getObjective(DisplaySlot.SIDEBAR);

            if (teamObjective != null) {
                // Clear previous scores
                teamScoreboard.getEntries().forEach(teamScoreboard::resetScores);

                // Display the "Game Over" message
                Score timerScore = teamObjective.getScore(plugin.getMessages().getGameOverMessage());
                timerScore.setScore(2);

                // Update team points
                String pointsMessage = plugin.getMessages().getPointsMessage(team.getPoints(), plugin.getSpeedrun().getTotalWeight());
                Score pointsScore = teamObjective.getScore(pointsMessage);
                pointsScore.setScore(1);

                // Assign this team’s scoreboard to each player on the team
                if (team instanceof TrueTeam tt) {
                    for (Player player : tt.getPlayers()) {
                        if (!plugin.getSpeedrun().getScoreboardDisabled().contains(player)) {
                            player.setScoreboard(teamScoreboard);
                        }
                    }
                } else {
                    SoloTeam st = (SoloTeam) team;
                    if (!plugin.getSpeedrun().getScoreboardDisabled().contains(st.getPlayer())) {
                        st.setScoreboard(teamScoreboard);
                    }
                }
            }
        }
    }
}
