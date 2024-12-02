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

        // # of locations calculated = max team amount
        int maxTeamAmount = locations.size();

        // current team amount != max team amount
        int currentTeamsAmount = speedrun.getTeams().getTeams().size();

        // Since the number of teams is not always the maximum number, we need to ensure teams are still evenly spaced.
        // Thus, we calculate "multiple", which is essentially how many spawn points we skip before assigning a team
        // to one. e.g., current teams = 4; max teams = 16. multiple = 4; we have 16 spawn points, but only spawn teams
        // at locations where i (0 - 15) divided by 4 has no remainder. i.e., 0, 4, 8, & 12
        int multiple = (int) Math.round((double) maxTeamAmount/currentTeamsAmount);

        // For each location off all calculated locations
        for (Location teamSpawnLocation : locations) {

            // If i (starts at 0, increments after each loop) % multiple is 0, then we use this location
            if (i % multiple == 0) {

                /* If i % multiple == 0, then i / multiple is an integer. Going off example above:
                   multiple = 4 & i = 0: 0 % 4 = 0 -> 0 / 4 = 0 -> teamIndex = 0
                   multiple = 4 & i = 1: 1 % 4 != 0
                   ...
                   multiple = 4 & i = 4: 4 % 4 = 0 -> 4 / 4 = 1 -> teamIndex = 1
                   ...
                   multiple = 4 & i = 8: 8 % 4 = 0 -> 8 / 4 = 2 -> teamIndex = 2 */
                int teamIndex = (int) Math.round((double) i / multiple);

                // Edge case check to prevent out-of-bounds access:
                // The team index should never exceed the # of current teams
                if (teamIndex >= currentTeamsAmount) { break; }

                // If teams are enabled...
                if (speedrun.getTeamsEnabled()) {

                    try {

                        // Get the TrueTeam (since teams enabled) corresponding to teamIndex
                        TrueTeam team = speedrun.getTeams().convertAbstractToTeam(speedrun.getTeams().getTeams()).get(teamIndex);

                        // Set the team's respawn location to the current location in the loop
                        team.setRespawnLocation(teamSpawnLocation);

                        // Map the spawn location index to the team so we can keep track
                        speedrun.setSpawnLocationIndexToTeam(teamIndex, team);

                        // Teleport all players within the current specific team to the team spawn location
                        for (Player player : team.getPlayers()) {
                            player.teleport(teamSpawnLocation);
                        }

                    } catch (IndexOutOfBoundsException e) { return; }

                } else {

                    try {

                        // If teams are disabled, then:
                        if (speedrun.getTeams().getTeams().get(teamIndex) instanceof SoloTeam p) {

                            // Teleport each player to their specific spawn location
                            p.teleport(teamSpawnLocation);

                            // Set the player's respawn location
                            p.setRespawnLocation(teamSpawnLocation);

                            // Map the spawn location index to the soloteam (p) so we can keep track
                            speedrun.setSpawnLocationIndexToTeam(teamIndex, p);

                        }
                    } catch (IndexOutOfBoundsException e) { return; }
                }
                // Increment i after going through the loop
            } i++;
        }
    }

    public static CompletableFuture<List<Location>> getTeamSpawnLocations(Main plugin) {

        // Map that contains the "Location Number" and the corresponding Location
        final Map<Integer, Location> incrementedLocations = new ConcurrentHashMap<>();

        // We need to use the speedrun instance to get the max teams
        Speedrun speedrun = plugin.getSpeedrun();

        // Calculate Angle Step based on # of Teams: e.g., if max teams is 4, each team location will be 90 degrees
        // apart; if max teams is 8, each team location will be 45 degrees apart, etc.
        double angleStep = 2 * Math.PI / speedrun.getMaxTeams();

        // Create a List of Futures for each Location Calculation - we use CompletableFutures here so that
        // we can properly alert the sender of "/speedrun generateworld" when locations are located
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Loop through each team: i = 0 to (maxTeams - 1)
        for (int i = 0; i < speedrun.getMaxTeams(); i++) {

            // The angle from (0, 0) for a specific team is i * angleStep
            double angle = i * angleStep;

            // Going off of above example (maxTeams = 4)...
            // Team 0 spawns @ 0 degrees, team 1 spawns at (1 * 90) = 90 degrees, 2 @ (2 * 90) = 180, etc..
            final int increment = i;

            /* Run each location calculation asynchronously, ensure that an integer is mapped to each location so
               we know what number corresponds to what location. Because this is asynchronous, locations are not
               returned in order. */
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Location location = findSafeLocation(plugin, angle, increment);
                incrementedLocations.put(increment, location);
            }); futures.add(future);
        }

        /* Because locations are located asynchronously, they are not returned in proper order. The code below
           ensures that locations are returned in order and correspond to the correct number. */
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
            List<Location> safeLocations = new ArrayList<>();
            for (int i = 0; i < speedrun.getMaxTeams(); i++) {
                safeLocations.add(incrementedLocations.get(i));
            } return safeLocations;
        });
    }

    /* After the team location is calculated, we need to ensure it is safe for a team to spawn there
       If unsafe, 100 attempts will be made to find a safe location. Safe locations are searched for along
       the spawn radius between the current spawn location & the next spawn location.
       If a safe spawn location is not found after 100 attempts, an admin will be notified. */
    private static Location findSafeLocation(Main plugin, double initialAngle, int increment) {

        // Get Spawn Radius
        int radius = plugin.getSpeedrun().getSpawnRadius();

        // Get Speedrun World
        World world = plugin.getSpeedrun().getSpeedrunWorld();

        /* "initialAngle" is the current angle (angleStep * i) passed from getTeamSpawnLocations
           This is ~ initial angle ~ calculated for this specific team prior to now. */
        double angle = initialAngle;

        // We store potential safe locations here
        Location potentialLocation;

        // We start at 0 attempts
        int attempts = 0;

        // Max Attempts = 100
        // TODO - This option should be configurable
        int maxAttempts = 100;  // Limit to avoid infinite loop

        /* The new angle step is equal to the initial angle step (the angle between two team spawnpoints), divided
           by the max attempts. e.g., if there are 4 teams, the angle step is 90 degrees - There is 90 degrees worth of
           space between the initial spawn point located & the next one. We will search this space evenly depending on
           maximum number of attempts specified. In this case, 100, thus we will increment the angle by (90/100) or 0.9
           degrees until we find a safe location. */
        double angleStep = ((2 * Math.PI)/plugin.getSpeedrun().getMaxTeams())/maxAttempts;

        // Loop only breaks if a safe location is found, or attempts surpasses limit
        while (true) {

            // Find coordinates that correspond to angle @ team spawn radius
            int x = (int) Math.round(radius * Math.cos(angle));
            int z = (int) Math.round(radius * Math.sin(angle));

            // Y-coordinate is the highest block at (x, z)
            int highestY = world.getHighestBlockYAt(x, z);

            // If location is safe, we return the location (adding 1 to y-coordinate so player doesn't spawn in block)
            // as the safe location found
            potentialLocation = new Location(world, x, highestY + 1, z); // +1 to spawn above the block

            // Identify the block at that location
            Block block = world.getBlockAt(x, highestY, z);

            // Identify the material of the block at that location
            Material type = block.getType();

            // If the block passes any of the following break conditions, then it is determined to be safe

            // Check if the block below is solid & not water/lava
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

            /* Essentially, water, lava, cactus, sweet berries, & deep snow are unsafe
               To avoid leaving something out, we specify scenarios that ARE safe rather than scenarios that aren't. */
            if (attempts >= maxAttempts) {
                plugin.getLogger().warning("Couldn't find a safe location, returning last tried location.");
                plugin.asyncBroadcast(plugin.getMessages().getUnsafeSpawnAlert(increment + 1), "ksu.speedrun.admin");
                break;
            }

        }

        /* If a safe location is found, potential location will be the first safe location discovered at the spawn
           radius to the initially calculated point. If a safe location is not found, the location returned will be
           the (failing) potential location calculated on attempt 100 (assuming the number wasn't changed). */
        /* TODO - Theoretically, if a location fails after 100 attempts, according to the formula, this will be right
            next to the spawn location of the next player. If all attempts fail, the spawn point returned should be
            the initial location to at least ensure players are spaced correctly */
        return potentialLocation;

    }
}