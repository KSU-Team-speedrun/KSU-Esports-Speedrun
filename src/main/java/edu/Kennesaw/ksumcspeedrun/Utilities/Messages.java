package edu.Kennesaw.ksumcspeedrun.Utilities;

import edu.Kennesaw.ksumcspeedrun.FileIO.Config;
import edu.Kennesaw.ksumcspeedrun.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

public class Messages {

    Main plugin;

    // Messages
    private Component prefix;
    private TagResolver.Single prefixPlaceholder;
    private String teamJoinMessage;
    private String alreadyOnTeam;
    private String teamIsFull;
    private String start;
    private String forceStop;
    private String winner;
    private String timeUp;
    private String objectiveComplete;
    private String objectiveCompleteNumber;
    private String objectiveAdded;
    private String objectiveAddedPoints;
    private String objectiveAddedNumber;
    private String objectiveAddedPointsNumber;
    private String timeLimitSet;
    private String teamSizeLimitSet;

    // Timer
    private String timerTitle;
    private String timeRemaining;
    private String gameOverMessage;

    public Messages(Main plugin) {

        this.plugin = plugin;

        Config config = plugin.getSpeedrunConfig();

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

            prefix = config.getComponent("messages.prefix");
            prefixPlaceholder = Placeholder.component("prefix", prefix);
            teamJoinMessage = config.getString("messages.teamJoinMessage");
            alreadyOnTeam = config.getString("messages.alreadyOnTeam");
            teamIsFull = config.getString("messages.teamIsFull");
            start = config.getString("messages.start");
            forceStop = config.getString("messages.forceStop");
            winner = config.getString("messages.winner");
            timeUp = config.getString("messages.timeUp");
            objectiveComplete = config.getString("messages.objectiveComplete");
            objectiveCompleteNumber = config.getString("messages.objectiveCompleteNumber");
            objectiveAdded = config.getString("messages.admin.objectiveAdded");
            objectiveAddedNumber = config.getString("messages.admin.objectiveAddedNumber");
            objectiveAddedPoints = config.getString("messages.admin.objectiveAddedPoints");
            objectiveAddedPointsNumber = config.getString("messages.admin.objectiveAddedPointsNumber");
            timeLimitSet = config.getString("messages.admin.timeLimitSet");
            teamSizeLimitSet = config.getString("messages.admin.teamSizeLimitSet");

            timerTitle = config.getString("timer.title");
            timeRemaining = config.getString("timer.timeLeft");
            gameOverMessage = config.getString("timer.gameOverMessage");

        });

    }

    public Component getPrefix() {
        return prefix;
    }

    public Component getTeamJoinMessage(Component teamName) {
        return ComponentHelper.mmStringToComponent(teamJoinMessage, prefixPlaceholder,
                Placeholder.component("team_name", teamName));
    }

    public Component getAlreadyOnTeam() {
        return ComponentHelper.mmStringToComponent(alreadyOnTeam, prefixPlaceholder);
    }

    public Component getTeamIsFull() {
        return ComponentHelper.mmStringToComponent(teamIsFull, prefixPlaceholder);
    }

    public Component getStart(int gameTimeInMinutes) {
        return ComponentHelper.mmStringToComponent(start, prefixPlaceholder,
                Placeholder.parsed("time", gameTimeInMinutes + ""));
    }

    public Component getForceStop() {
        return ComponentHelper.mmStringToComponent(forceStop, prefixPlaceholder);
    }

    public Component getWinner(Component winner) {
        return ComponentHelper.mmStringToComponent(this.winner, prefixPlaceholder,
                Placeholder.component("winner", winner));
    }

    public Component getTimeUp(Component winner) {
        return ComponentHelper.mmStringToComponent(this.timeUp, prefixPlaceholder,
                Placeholder.component("winner", winner));
    }

    public Component getObjectiveComplete(String objectiveType, String target, int points) {
        return ComponentHelper.mmStringToComponent(this.objectiveComplete, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType), Placeholder.parsed("target", target),
                Placeholder.parsed("points", points + ""));
    }

    public Component getObjectiveCompleteNumber(String objectiveType, String target, int number, int points) {
        return ComponentHelper.mmStringToComponent(objectiveCompleteNumber, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target),
                Placeholder.parsed("number", number + ""),
                Placeholder.parsed("points", points + ""));
    }

    public Component getObjectiveAdded(String objectiveType, String target) {
        return ComponentHelper.mmStringToComponent(objectiveAdded, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target));
    }

    public Component getObjectiveAddedPoints(String objectiveType, String target, int points) {
        return ComponentHelper.mmStringToComponent(objectiveAddedPoints, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target),
                Placeholder.parsed("points", points + ""));
    }

    public Component getObjectiveAddedNumber(String objectiveType, String target, int number) {
        return ComponentHelper.mmStringToComponent(objectiveAddedNumber, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target),
                Placeholder.parsed("number", number + ""));
    }

    public Component getObjectiveAddedPointsNumber(String objectiveType, String target, int number, int points) {
        return ComponentHelper.mmStringToComponent(objectiveAddedPointsNumber, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target),
                Placeholder.parsed("number", number + ""),
                Placeholder.parsed("points", points + ""));
    }

    public Component getTimeLimitSet(String timeInMinutes) {
        return ComponentHelper.mmStringToComponent(timeLimitSet, prefixPlaceholder, Placeholder.parsed("time_limit",
                timeInMinutes));
    }

    public Component getTeamSizeLimitSet(String sizeLimit) {
        return ComponentHelper.mmStringToComponent(teamSizeLimitSet, prefixPlaceholder, Placeholder.parsed("size_limit",
                sizeLimit));
    }

    public Component getTimerTitle() {
        return ComponentHelper.mmStringToComponent(timerTitle);
    }

    public String getTimeLeft(String mmSSTimeFormat) {
        return LegacyComponentSerializer.legacySection().serialize(ComponentHelper.mmStringToComponent(timeRemaining,
                Placeholder.parsed("time_remaining", mmSSTimeFormat)));
    }

    public String getGameOverMessage() {
        return LegacyComponentSerializer.legacySection().serialize(ComponentHelper
                .mmStringToComponent(gameOverMessage));
    }

}
