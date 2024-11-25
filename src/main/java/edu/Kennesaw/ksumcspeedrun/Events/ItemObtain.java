package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObtainObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * The ItemObtain class is responsible for handling events related to item obtaining and crafting
 * in a speedrun context. It listens to events such as item pickups and item crafts, and checks
 * if the obtained items fulfill certain objectives for the player's team.
 */
public class ItemObtain implements Listener {

    Main plugin;

    private final Speedrun speedrun;
    private final TeamManager tm;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public ItemObtain(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        this.tm = speedrun.getTeams();

    }

    @EventHandler
    public void pickupItem(EntityPickupItemEvent e) {

        // Only listen if the speedrun is started
        if (speedrun.isStarted()) {

            Entity entity = e.getEntity();

            ItemStack is = e.getItem().getItemStack();

            // If the entity who picked up an item is a player, continue
            if (entity instanceof Player p) {

                Team team = tm.getTeam(p);

                // Ensure that the player is on a team
                if (team == null) {
                    return;
                }

                ObtainObjective matchedObtainObjective = null;

                // See if an objective exists to obtain the item that was picked up
                for (Objective o : team.getIncompleteObjectives()) {

                    if (o.getType().equals(Objective.ObjectiveType.OBTAIN)) {

                        ObtainObjective oo = (ObtainObjective) o;

                        if (oo.getItem().equals(is.getType())) {

                            matchedObtainObjective = oo;
                            break;

                        }

                    }

                }

                // If no objective exists, return
                if (matchedObtainObjective == null) {
                    return;
                }

                // Determine the total amount of items required to complete the objective
                int totalNumber = matchedObtainObjective.getAmount();

                // If the player alone has the total number of items required to complete the objective, set it as complete
                if (getInventoryItemCount(p, is.getType()) >= totalNumber) {
                    matchedObtainObjective.setComplete(team);
                    return;
                }

                /* If not, check and see if the sum of the number of the specified item in all teammates inventories
                 is greater than the required amount */
                int playerItemCount = 0;

                if (speedrun.getTeamsEnabled() && team instanceof TrueTeam trueTeam) {

                    for (Player teamPlayer : trueTeam.getPlayers()) {

                        playerItemCount += getInventoryItemCount(teamPlayer, is.getType());

                        System.out.println("total number: " + totalNumber);
                        System.out.println("total amount: " + playerItemCount);

                        matchedObtainObjective.setIncrementNumber(team, playerItemCount);

                        // If the combined number for all teammates is greater than required, the team completes the objective
                        if (playerItemCount >= totalNumber) {
                            matchedObtainObjective.setComplete(team);
                            return;
                        }
                    }
                }

            }

        }

    }

    // TODO: Add event handler for item crafting

    @SuppressWarnings("StatementWithEmptyBody")
    @EventHandler
    public void craftItem(CraftItemEvent e) {

        if (speedrun.isStarted()) {



        }

    }

    // Get the number of items (materials) in a specific player's inventory
    private int getInventoryItemCount(Player p, Material m) {

        Inventory i = p.getInventory();
        int count = 1;

        for (ItemStack itemStack : i.getContents()) {

            if (itemStack != null && itemStack.getType().equals(m)) {

                count += itemStack.getAmount();

            }

        }

        return count;

    }
}
