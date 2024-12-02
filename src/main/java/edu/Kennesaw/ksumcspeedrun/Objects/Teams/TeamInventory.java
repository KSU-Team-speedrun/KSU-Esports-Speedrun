package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TeamInventory class is responsible for managing and displaying a dynamic inventory
 * interface representing various teams within a TeamManager.
 */
public class TeamInventory {

    TeamManager tm;

    private Inventory inv;
    private final Component inventoryName;

    // Map of the team's number location in the team inventory, allows for less intensive inventory updates
    private Map<TrueTeam, Integer> inventoryLoc;

    public TeamInventory(TeamManager tm, Component inventoryName) {
        this.tm = tm;
        this.inventoryName = inventoryName;
    }

    // Creates the team inventory UI dynamically based on the number of teams that are active
    public void createTeamInventory() {

        if (tm.getTeams().isEmpty()) return;

        // Item Count = number of active teams
        int itemCount;

        // If the overflow team exists, we have to make sure we are not including it in the team selection inventory
        TrueTeam overFlow = (TrueTeam) tm.getTeam(Component.text("Overflow Team"));
        if (overFlow != null) {
            itemCount = tm.getTeams().size() - 1;
        } else {
            itemCount = tm.getTeams().size();
        }

        // Determine the number of rows needed based on the item count
        int rowsNeeded = Items.determineRows(itemCount);

        // Inventories must be a multiple of 9, thus:
        int inventorySize = rowsNeeded * 9;

        Inventory inv = Bukkit.createInventory(null, inventorySize, inventoryName);

        List<ItemStack> teamItems = new ArrayList<>();

        // Team inventory is only used when teams are enabled, this all teams are instances of TrueTeams
        List<TrueTeam> trueTrueTeams = tm.convertAbstractToTeam(tm.getTeams());

        // Do not include the overflow team
        trueTrueTeams.remove(overFlow);

        // All "TrueTeams" have an assigned item, we need to add all team items into the itemstack list
        for (TrueTeam trueTeam : trueTrueTeams) {
            teamItems.add(trueTeam.getItem());
        }

        /* Dynamically determine the slots that inventory items go in the inventory so that they are
           visually centered */
        List<Integer> slots = Items.generateSlots(itemCount, rowsNeeded);

        this.inventoryLoc = new HashMap<>();

        // Map the team item to the cooresponding inventory location
        for (int i = 0; i < itemCount && i < slots.size(); i++) {
            inv.setItem(slots.get(i), teamItems.get(i));
            inventoryLoc.put(trueTrueTeams.get(i), slots.get(i));
        }

        List<HumanEntity> viewers = null;

        // Close the inventory UI for all players viewing it
        if (this.inv != null) {
            viewers = new ArrayList<>(this.inv.getViewers());
            for (HumanEntity humanEntity : viewers) {
                if (humanEntity instanceof Player p) {
                    p.closeInventory();
                }
            }
        }

        this.inv = inv;

        // Open the new inventory UI for all players viewing the old one
        if (viewers != null) {

            for (HumanEntity he : viewers) {

                if (he instanceof Player p) {
                    p.openInventory(this.inv);
                }
            }
        }
    }

    // Update the item meta of a specific team item when it changes (e.g., a player joins or leaves the team)
    public void updateTeamInventory(TrueTeam trueTeamToUpdate) {
        if (inventoryLoc.get(trueTeamToUpdate) == null) return;
        inv.setItem(inventoryLoc.get(trueTeamToUpdate), trueTeamToUpdate.getItem());
        for (HumanEntity viewer : inv.getViewers()) {
            if (viewer instanceof Player player) {
                player.getOpenInventory().getTopInventory().setItem(inventoryLoc.get(trueTeamToUpdate), trueTeamToUpdate.getItem());
            }
        }
    }

    public Inventory getInventory() {
        return inv;
    }

}
