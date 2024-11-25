package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TeamSpawner class is responsible for managing the spawning of teams or individual players
 * in a circular formation around a central point within a game world.
 */
public class TeamSpawner {

    public static void spawnTeamsInCircle(Speedrun speedrun, List<Location> locations) {

        int i = 0;
        int maxTeamAmount = locations.size();
        int currentTeamsAmount = speedrun.getTeams().getTeams().size();
        int multiple = (int) Math.round((double) maxTeamAmount/currentTeamsAmount);

        for (Location teamSpawnLocation : locations) {
            if (i % multiple == 0) {
                int teamIndex = (int) Math.round((double) i / multiple);

                // Edge case check to prevent out-of-bounds access
                if (teamIndex >= currentTeamsAmount) { break; }
                if (speedrun.getTeamsEnabled()) {
                    try {
                        TrueTeam team = speedrun.getTeams().convertAbstractToTeam(speedrun.getTeams().getTeams()).get(teamIndex);
                        team.setRespawnLocation(teamSpawnLocation);
                        speedrun.setSpawnLocationIndexToTeam(teamIndex, team);

                        // Teleport each player to the team's spawn location and set their respawn point
                        for (Player player : team.getPlayers()) {
                            player.teleport(teamSpawnLocation);

                        }
                    } catch (IndexOutOfBoundsException e) { return; }
                } else {
                    try {
                        if (speedrun.getTeams().getTeams().get(teamIndex) instanceof SoloTeam p) {

                            // Teleport each player to the team's spawn location and set their respawn point
                            p.teleport(teamSpawnLocation);

                            // Set the player's respawn location
                            p.setRespawnLocation(teamSpawnLocation);
                            speedrun.setSpawnLocationIndexToTeam(teamIndex, p);

                        }
                    } catch (IndexOutOfBoundsException e) { return; }
                }
            } i++;
        }
    }

    public static CompletableFuture<List<Location>> getTeamSpawnLocations(Main plugin) {

        final Map<Integer, Location> incrementedLocations = new ConcurrentHashMap<>();
        Speedrun speedrun = plugin.getSpeedrun();

        // Calculate angle step based on the number of teams
        double angleStep = 2 * Math.PI / speedrun.getMaxTeams();

        // Create a list of futures for each location calculation
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < speedrun.getMaxTeams(); i++) {
            double angle = i * angleStep;
            final int increment = i;
            // Run each location calculation asynchronously
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Location location = findSafeLocation(plugin, angle, increment);
                incrementedLocations.put(increment, location);
            }); futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<Location> safeLocations = new ArrayList<>();
            for (int i = 0; i < speedrun.getMaxTeams(); i++) {
                safeLocations.add(incrementedLocations.get(i));
            } return safeLocations;
        });
    }

    private static Location findSafeLocation(Main plugin, double initialAngle, int increment) {

        int radius = plugin.getSpeedrun().getSpawnRadius();
        World world = plugin.getSpeedrun().getSpeedrunWorld();
        double angle = initialAngle;
        Location potentialLocation;
        int attempts = 0;
        int maxAttempts = 100;  // Limit to avoid infinite loop
        double angleStep = ((2 * Math.PI)/plugin.getSpeedrun().getMaxTeams())/maxAttempts;

        while (true) {
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));
            int highestY = world.getHighestBlockYAt(x, z);
            potentialLocation = new Location(world, x, highestY + 1, z); // +1 to spawn above the block
            Block block = world.getBlockAt(x, highestY, z);

            Material type = block.getType();

            // Check if the block below is solid &  not water/lava
            if (block.isSolid() && type != Material.LAVA && type != Material.WATER) {
                break;
            }

            // Allow players to spawn on leaves
            if (block.getBlockData() instanceof Leaves) {
                plugin.asyncBroadcast(plugin.getMessages().getLeavesWarning(increment + 1, type.toString()), "ksu.speedrun.admin");
                break;
            }

            // Allow players to spawn on plants, decrease the y coordinate by 1
            if (Tag.SMALL_FLOWERS.isTagged(type) || type.equals(Material.TALL_GRASS) || type.equals(Material.SHORT_GRASS)
                    || type.equals(Material.LARGE_FERN) || Tag.TALL_FLOWERS.isTagged(type)) {
                potentialLocation = block.getLocation();
                break;
            }

            // Allow players to spawn on snow or carpet
            if (type.equals(Material.SNOW) || Tag.WOOL_CARPETS.isTagged(type)) {
                break;
            }

            // Increment angle to try another location around the circle
            angle += angleStep; // Move slightly around the circle in radians
            attempts++;

            if (attempts >= maxAttempts) {
                plugin.getLogger().warning("Couldn't find a safe location, returning last tried location.");
                plugin.asyncBroadcast(plugin.getMessages().getUnsafeSpawnAlert(increment + 1), "ksu.speedrun.admin");
                break;
            }
        } return potentialLocation;
    }
}