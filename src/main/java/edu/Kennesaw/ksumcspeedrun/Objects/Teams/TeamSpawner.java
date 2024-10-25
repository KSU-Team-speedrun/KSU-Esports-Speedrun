package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.List;

public class TeamSpawner {

    public static void spawnTeamsInCircle(World world, List<List<Player>> teams, double radius) {
        // Calculate angle step based on the number of teams
        double angleStep = 2 * Math.PI / teams.size();

        for (int i = 0; i < teams.size(); i++) {
            // Calculate each team's angle and position in the circle
            double angle = i * angleStep;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            // Get the highest block at this (x, z) position to spawn on the surface
            int highestY = world.getHighestBlockYAt((int) x, (int) z);
            Location teamSpawnLocation = new Location(world, x, highestY + 1, z); // Add 1 to avoid spawning inside the block

            // Teleport each player to the team's spawn location and set their respawn point
            for (Player player : teams.get(i)) {
                player.teleport(teamSpawnLocation);

                // Set the player's respawn location
                player.setRespawnLocation(teamSpawnLocation);

                // Notify each player individually about their spawn point
                player.sendMessage("Your team has been spawned at " + teamSpawnLocation);
            }

            // Manually broadcast message to all online players
            String broadcastMessage = "Team " + (i + 1) + " has been spawned at " + teamSpawnLocation;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(broadcastMessage);
            }
        }
    }
}
