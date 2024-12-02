package edu.Kennesaw.ksumcspeedrun.Utilities;

import edu.Kennesaw.ksumcspeedrun.FileIO.Config;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is initialized in the 'Main' class during plugin startup.
 * It loads all plugin messages into memory to provide efficient access during the game.
 *
 * Design Trade-offs:
 * - Slightly increased memory usage and startup time.
 * - Significantly reduced IO operations during gameplay, improving runtime performance.
 *
 * Purpose:
 * - Stores and formats almost all messages (chat & otherwise) used by the plugin.
 * - Messages are retrieved from the plugin's configuration file via the 'Config' dependency.
 */
public class Messages {

    Main plugin;

    private Component prefix;
    private TagResolver.Single prefixPlaceholder;

    // Messages
    private List<String> playerHelpMessage;
    private String toggleScoreboard;
    private String teamJoinMessage;
    private String teamCooldownMessage;
    private String teamHelp;
    private String teamNotFound;
    private String alreadyOnTeam;
    private String teamIsFull;
    private String start;
    private String forceStop;
    private String winner;
    private String timeUp;
    private String objectiveComplete;
    private String objectiveCompleteNumber;
    private String objectiveIncrement;
    private String noTeleportInCombat;
    private String invalidArguments;
    private String illegalArguments;
    private String outOfBounds;
    private String unknownCommand;
    private String worldNotGenerated;
    private String teamsNotEnabled;
    private String noObjectives;
    private ConfigurationSection adminHelpMessage;
    private String objectiveAdded;
    private String objectiveAddedPoints;
    private String objectiveAddedNumber;
    private String objectiveAddedPointsNumber;
    private String objectiveRemoved;
    private String timeLimitSet;
    private String timeLimit;
    private String teamSizeLimitSet;
    private String teamSizeLimit;
    private String seedSet;
    private String seed;
    private String worldBorderSet;
    private String worldBorder;
    private String spawnRadiusSet;
    private String spawnRadius;
    private String pointLimitSet;
    private String pointLimit;
    private String participationSet;
    private String gameAlreadyStarted;
    private String gameNotStarted;
    private String gameStarted;
    private String worldGenerating;
    private String worldGenerated;
    private String worldDeleted;
    private String spawnsGenerating;
    private String spawnsGenerated;
    private String resetAttributes;
    private String toggleTeams;
    private String gameStartedCannotChange;
    private String cannotStartGame;
    private String teamSpawnSet;
    private String teamSpawnLocations;
    private String teamSpawnLocation;
    private String unsafeSpawnAlert;
    private String leavesWarning;

    // Scoreboard
    private String scoreboardTitle;
    private String timeRemaining;
    private String gameOverMessage;
    private String pointsMessage;

    // Title
    private String gameOverTitle;
    private String gameOverSubtitle;

    // Loads all message from config upon initialization, store them privately
    public Messages(Main plugin) {

        this.plugin = plugin;

        Config config = plugin.getSpeedrunConfig();

        plugin.runAsyncTask(() -> {

            prefix = config.getComponent("messages.prefix");
            prefixPlaceholder = Placeholder.component("prefix", prefix);

            playerHelpMessage = config.getStringList("messages.helpMessage");
            toggleScoreboard = config.getString("messages.toggleScoreboard");
            teamJoinMessage = config.getString("messages.teamJoinMessage");
            teamCooldownMessage = config.getString("messages.teamCooldownMessage");
            teamHelp = config.getString("messages.teamHelp");
            teamNotFound = config.getString("messages.teamNotFound");
            alreadyOnTeam = config.getString("messages.alreadyOnTeam");
            teamIsFull = config.getString("messages.teamIsFull");
            start = config.getString("messages.start");
            forceStop = config.getString("messages.forceStop");
            winner = config.getString("messages.winner");
            timeUp = config.getString("messages.timeUp");
            objectiveComplete = config.getString("messages.objectiveComplete");
            objectiveCompleteNumber = config.getString("messages.objectiveCompleteNumber");
            objectiveIncrement = config.getString("messages.objectiveIncrement");
            noTeleportInCombat = config.getString("messages.noTeleportInCombat");
            invalidArguments = config.getString("messages.error.invalidArguments");
            illegalArguments = config.getString("messages.error.illegalArgument");
            outOfBounds = config.getString("messages.error.outOfBounds");
            unknownCommand = config.getString("messages.error.unknownCommand");
            cannotStartGame = config.getString("messages.error.cannotStartGame");
            worldNotGenerated = config.getString("messages.error.worldNotGenerated");
            teamsNotEnabled = config.getString("messages.error.teamsNotEnabled");
            noObjectives = config.getString("messages.error.noObjectives");
            adminHelpMessage = config.getConfigurationSection("messages.admin.helpMessage");
            objectiveAdded = config.getString("messages.admin.objectiveAdded");
            objectiveAddedNumber = config.getString("messages.admin.objectiveAddedNumber");
            objectiveAddedPoints = config.getString("messages.admin.objectiveAddedPoints");
            objectiveAddedPointsNumber = config.getString("messages.admin.objectiveAddedPointsNumber");
            objectiveRemoved = config.getString("messages.admin.objectiveRemoved");
            timeLimitSet = config.getString("messages.admin.timeLimitSet");
            timeLimit = config.getString("messages.admin.timeLimit");
            teamSizeLimitSet = config.getString("messages.admin.teamSizeLimitSet");
            teamSizeLimit = config.getString("messages.admin.teamSizeLimit");
            seedSet = config.getString("messages.admin.seedSet");
            seed = config.getString("messages.admin.seed");
            worldBorderSet = config.getString("messages.admin.worldBorderSet");
            worldBorder = config.getString("messages.admin.worldBorder");
            spawnRadiusSet = config.getString("messages.admin.spawnRadiusSet");
            spawnRadius = config.getString("messages.admin.spawnRadius");
            pointLimitSet = config.getString("messages.admin.pointLimitSet");
            pointLimit = config.getString("messages.admin.pointLimit");
            participationSet = config.getString("messages.admin.participationSet");
            gameAlreadyStarted = config.getString("messages.admin.gameAlreadyStarted");
            gameNotStarted = config.getString("messages.admin.gameNotStarted");
            gameStarted = config.getString("messages.admin.gameStarted");
            worldGenerating = config.getString("messages.admin.worldGenerating");
            worldGenerated = config.getString("messages.admin.worldGenerated");
            worldDeleted = config.getString("messages.admin.worldDeleted");
            spawnsGenerating = config.getString("messages.admin.spawnsGenerating");
            spawnsGenerated = config.getString("messages.admin.spawnsGenerated");
            resetAttributes = config.getString("messages.admin.resetAttributes");
            toggleTeams = config.getString("messages.admin.toggleTeams");
            teamSpawnSet = config.getString("messages.admin.teamSpawnSet");
            gameStartedCannotChange = config.getString("messages.admin.gameStartedCannotChange");
            teamSpawnLocations = config.getString("messages.admin.teamSpawnLocations");
            teamSpawnLocation = config.getString("messages.admin.teamSpawnLocation");
            unsafeSpawnAlert = config.getString("messages.admin.unsafeSpawnAlert");
            leavesWarning = config.getString("messages.admin.leavesWarning");

            scoreboardTitle = config.getString("scoreboard.title");
            timeRemaining = config.getString("scoreboard.timeLeft");
            gameOverMessage = config.getString("scoreboard.gameOverMessage");
            pointsMessage = config.getString("scoreboard.pointsMessage");

            gameOverTitle = config.getString("title.gameOverTitle");
            gameOverSubtitle = config.getString("title.gameOverSubtitle");

        });

    }

    // *** GETTERS FOR ALL MESSAGES *** //

    public List<Component> getPlayerHelpMessage() {
        List<Component> helpMessage = new ArrayList<>();
        for (String line : playerHelpMessage) {
            helpMessage.add(ComponentHelper.mmStringToComponent(line));
        }
        return helpMessage;
    }

    public Component getToggleScoreboard(boolean isEnabled) {
        return ComponentHelper.mmStringToComponent(toggleScoreboard, prefixPlaceholder,
                Placeholder.parsed("is_enabled", isEnabled ? "ENABLED" : "DISABLED"));
    }

    public Component getTeamJoinMessage(Component teamName) {
            return ComponentHelper.mmStringToComponent(teamJoinMessage, prefixPlaceholder,
                    Placeholder.component("team_name", teamName));
    }

    public Component getTeamCooldownMessage() {
        return ComponentHelper.mmStringToComponent(teamCooldownMessage, prefixPlaceholder);
    }

    public Component getTeamHelp() {
        return ComponentHelper.mmStringToComponent(teamHelp, prefixPlaceholder);
    }

    public Component getAlreadyOnTeam() {
        return ComponentHelper.mmStringToComponent(alreadyOnTeam, prefixPlaceholder);
    }

    public Component getTeamIsFull() {
        return ComponentHelper.mmStringToComponent(teamIsFull, prefixPlaceholder);
    }

    public Component getTeamNotFound(String teamName) {
        return ComponentHelper.mmStringToComponent(teamNotFound, prefixPlaceholder, Placeholder.parsed("team_name",
                teamName));
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

    public Component getObjectiveIncrement(String objectiveType, String target, int currentAmount, int totalAmount, int points) {
        return ComponentHelper.mmStringToComponent(objectiveIncrement, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target),
                Placeholder.parsed("current_amount", currentAmount + ""),
                Placeholder.parsed("total_amount", totalAmount + ""),
                Placeholder.parsed("points", points + ""));
    }

    public Component getNoTeleportInCombat() {
        return ComponentHelper.mmStringToComponent(noTeleportInCombat, prefixPlaceholder);
    }

    public Component getInvalidArguments(String usage) {
        return ComponentHelper.mmStringToComponent(invalidArguments, prefixPlaceholder,
                Placeholder.parsed("usage", usage));
    }

    public Component getIllegalArguments(String illegalArg, String expectedType) {
        return ComponentHelper.mmStringToComponent(illegalArguments, prefixPlaceholder,
                Placeholder.parsed("illegal_arg", illegalArg),
                Placeholder.parsed("expected_type", expectedType));
    }

    public Component getOutOfBounds(String illegalArg, String object) {
        return ComponentHelper.mmStringToComponent(outOfBounds, prefixPlaceholder,
                Placeholder.parsed("illegal_arg", illegalArg),
                Placeholder.parsed("object", object));
    }

    public Component getUnknownCommand(String subcommand) {
        return ComponentHelper.mmStringToComponent(unknownCommand, prefixPlaceholder,
                Placeholder.parsed("unknown_command", subcommand));
    }

    public Component getCannotStartGame() {
        return ComponentHelper.mmStringToComponent(cannotStartGame, prefixPlaceholder);
    }

    public Component getWorldNotGenerated() {
        return ComponentHelper.mmStringToComponent(worldNotGenerated, prefixPlaceholder);
    }

    public Component getTeamsNotEnabled() {
        return ComponentHelper.mmStringToComponent(teamsNotEnabled, prefixPlaceholder);
    }

    public Component getNoObjectives() {
        return ComponentHelper.mmStringToComponent(noObjectives, prefixPlaceholder);
    }

    public List<Component> getAdminHelpMessage(int pageNumber) {
        List<Component> helpMessage = new ArrayList<>();
        List<String> adminHelpPage = adminHelpMessage.getStringList("p" + pageNumber);
        for (String line : adminHelpPage) {
            helpMessage.add(ComponentHelper.mmStringToComponent(line));
        }
        return helpMessage;
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

    public Component getObjectiveRemoved(String objectiveType, String target) {
        return ComponentHelper.mmStringToComponent(objectiveRemoved, prefixPlaceholder,
                Placeholder.parsed("objective_type", objectiveType),
                Placeholder.parsed("target", target));
    }

    public Component getTimeLimitSet(String timeInMinutes) {
        return ComponentHelper.mmStringToComponent(timeLimitSet, prefixPlaceholder, Placeholder.parsed("time_limit",
                timeInMinutes));
    }

    public Component getTimeLimit(String timeInMinutes) {
        return ComponentHelper.mmStringToComponent(timeLimit, prefixPlaceholder, Placeholder.parsed("time_limit",
                timeInMinutes));
    }

    public Component getTeamSizeLimitSet(String sizeLimit) {
        return ComponentHelper.mmStringToComponent(teamSizeLimitSet, prefixPlaceholder, Placeholder.parsed("size_limit",
                sizeLimit));
    }

    public Component getTeamSizeLimit(String sizeLimit) {
        return ComponentHelper.mmStringToComponent(teamSizeLimit, prefixPlaceholder, Placeholder.parsed("size_limit",
                sizeLimit));
    }

    public Component getSeedSet(String seed) {
        return ComponentHelper.mmStringToComponent(seedSet, prefixPlaceholder, Placeholder.parsed("seed",
                seed));
    }

    public Component getSeed(String seed) {
        return ComponentHelper.mmStringToComponent(this.seed, prefixPlaceholder, Placeholder.parsed("seed",
                seed));
    }

    public Component getWorldBorderSet(String worldBorder) {
        return ComponentHelper.mmStringToComponent(worldBorderSet, prefixPlaceholder, Placeholder.parsed("world_border",
                worldBorder));
    }

    public Component getWorldBorder(String worldBorder) {
        return ComponentHelper.mmStringToComponent(this.worldBorder, prefixPlaceholder, Placeholder.parsed("world_border",
                worldBorder));
    }

    public Component getSpawnRadiusSet(String spawnRadius) {
        return ComponentHelper.mmStringToComponent(spawnRadiusSet, prefixPlaceholder, Placeholder.parsed("spawn_radius",
                spawnRadius));
    }

    public Component getSpawnRadius(String spawnRadius) {
        return ComponentHelper.mmStringToComponent(this.spawnRadius, prefixPlaceholder, Placeholder.parsed("spawn_radius",
                spawnRadius));
    }

    public Component getPointLimitSet(String pointLimit) {
        return ComponentHelper.mmStringToComponent(pointLimitSet, prefixPlaceholder, Placeholder.parsed("point_limit",
                pointLimit));
    }

    public Component getPointLimit(String pointLimit) {
        return ComponentHelper.mmStringToComponent(this.pointLimit, prefixPlaceholder, Placeholder.parsed("point_limit",
                pointLimit));
    }

    public Component getGameAlreadyStarted() {
        return ComponentHelper.mmStringToComponent(this.gameAlreadyStarted, prefixPlaceholder);
    }

    public Component getGameNotStarted() {
        return ComponentHelper.mmStringToComponent(this.gameNotStarted, prefixPlaceholder);
    }

    public Component getGameStarted() {
        return ComponentHelper.mmStringToComponent(this.gameStarted, prefixPlaceholder);
    }

    public Component getParticipationSet(Boolean isParticipating) {
        return ComponentHelper.mmStringToComponent(this.participationSet, prefixPlaceholder,
                Placeholder.parsed("is_participating", isParticipating ? "TRUE" : "FALSE"));
    }

    public Component getGameStartedCannotChange() {
        return ComponentHelper.mmStringToComponent(this.gameStartedCannotChange, prefixPlaceholder);
    }

    public Component getWorldGenerating() {
        return ComponentHelper.mmStringToComponent(this.worldGenerating, prefixPlaceholder);
    }

    public Component getWorldGenerated() {
        return ComponentHelper.mmStringToComponent(this.worldGenerated, prefixPlaceholder);
    }

    public Component getWorldDeleted() {
        return ComponentHelper.mmStringToComponent(this.worldDeleted, prefixPlaceholder);
    }


    public Component getSpawnsGenerating() {
        return ComponentHelper.mmStringToComponent(this.spawnsGenerating, prefixPlaceholder);
    }

    public Component getSpawnsGenerated() {
        return ComponentHelper.mmStringToComponent(this.spawnsGenerated, prefixPlaceholder);
    }

    public Component getResetAttributes() {
        return ComponentHelper.mmStringToComponent(this.resetAttributes, prefixPlaceholder);
    }

    public Component getToggleTeams(boolean toggleOption) {
        return ComponentHelper.mmStringToComponent(this.toggleTeams, prefixPlaceholder,
                Placeholder.parsed("toggle_option", toggleOption ? "ENABLED" : "DISABLED"));
    }

    public Component getTeamSpawnSet(int teamNumber) {
        return ComponentHelper.mmStringToComponent(this.teamSpawnSet, prefixPlaceholder,
                Placeholder.parsed("team_number", teamNumber + ""));
    }

    public Component getTeamSpawnLocations() {
        return ComponentHelper.mmStringToComponent(this.teamSpawnLocations, prefixPlaceholder);
    }

    public Component getTeamSpawnLocation(String playerName, Location location, int number) {
        return ComponentHelper.mmStringToComponent(this.teamSpawnLocation, prefixPlaceholder,
                Placeholder.parsed("location_x", location.getBlockX() + ""),
                Placeholder.parsed("location_y", location.getBlockY() + ""),
                Placeholder.parsed("location_z", location.getBlockZ() + ""),
                Placeholder.parsed("player", playerName),
                Placeholder.parsed("number", number + ""));
    }

    public Component getUnsafeSpawnAlert(int increment) {
        return ComponentHelper.mmStringToComponent(this.unsafeSpawnAlert, prefixPlaceholder,
                Placeholder.parsed("team_number", increment + ""));
    }

    public Component getLeavesWarning(int increment, String type) {
        return ComponentHelper.mmStringToComponent(this.leavesWarning, prefixPlaceholder,
                Placeholder.parsed("team_number", increment + ""),
                Placeholder.parsed("type", type));
    }

    public Component getTimerTitle() {
        return ComponentHelper.mmStringToComponent(scoreboardTitle);
    }

    public String getTimeLeft(String mmSSTimeFormat) {
        return LegacyComponentSerializer.legacySection().serialize(ComponentHelper.mmStringToComponent(timeRemaining,
                Placeholder.parsed("time_remaining", mmSSTimeFormat)));
    }

    public String getGameOverMessage() {
        return LegacyComponentSerializer.legacySection().serialize(ComponentHelper
                .mmStringToComponent(gameOverMessage));
    }

    public String getPointsMessage(int points, int totalPoints) {
        return LegacyComponentSerializer.legacySection().serialize(ComponentHelper
                .mmStringToComponent(pointsMessage, Placeholder.parsed("points", points + ""),
                        Placeholder.parsed("total_points", totalPoints + "")));
    }

    public Component getGameOverTitle(Team winner) {
        return ComponentHelper.mmStringToComponent(gameOverTitle,
                Placeholder.parsed("winner", winner == null ? "" : winner.getStrippedName()));
    }

    public Component getGameOverSubtitle(Team winner) {
        return ComponentHelper.mmStringToComponent(gameOverSubtitle,
                Placeholder.parsed("winner", winner == null ? "NOBODY" : winner.getStrippedName()));
    }

}
