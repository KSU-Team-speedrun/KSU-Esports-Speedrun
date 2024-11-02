package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObtainObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
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

    // TODO - ADD LOGIC TO THESE EVENT LISTENERS BELOW

    @EventHandler
    public void pickupItem(EntityPickupItemEvent e) {

        if (speedrun.isStarted()) {

            Entity entity = e.getEntity();

            ItemStack is = e.getItem().getItemStack();

            if (entity instanceof Player p) {

                if (!speedrun.getTeamsEnabled()) {

                    if (p instanceof SoloTeam soloPlayer && speedrun.getSoloPlayers().contains(p)) {

                        for (Objective o : soloPlayer.getIncompleteObjectives()) {

                            if (o.getType().equals(Objective.ObjectiveType.OBTAIN)) {

                                ObtainObjective oo = (ObtainObjective) o;

                                if (getInventoryItemCount(p, is.getType()) >= oo.getAmount()) {
                                    oo.setComplete(soloPlayer);
                                    return;
                                }

                            }

                        }

                    }
                }

                Team team = tm.getTeam(p);

                if (team == null) {
                    return;
                }

                ObtainObjective matchedObtainObjective = null;

                for (Objective o : team.getIncompleteObjectives()) {

                    if (o.getType().equals(Objective.ObjectiveType.OBTAIN)) {

                        ObtainObjective oo = (ObtainObjective) o;

                        if (oo.getItem().equals(is.getType())) {

                            matchedObtainObjective = oo;
                            break;

                        }

                    }

                }

                if (matchedObtainObjective == null) {
                    return;
                }

                int totalNumber = matchedObtainObjective.getAmount();

                if (getInventoryItemCount(p, is.getType()) >= totalNumber) {
                    matchedObtainObjective.setComplete(team);
                    return;
                }

                int playerItemCount = 0;

                for (Player teamPlayer : team.getPlayers()) {

                    playerItemCount += getInventoryItemCount(teamPlayer, is.getType());

                    System.out.println("total number: " + totalNumber);
                    System.out.println("total amount: " + playerItemCount);

                    if (playerItemCount >= totalNumber) {
                        matchedObtainObjective.setComplete(team);
                        return;
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
