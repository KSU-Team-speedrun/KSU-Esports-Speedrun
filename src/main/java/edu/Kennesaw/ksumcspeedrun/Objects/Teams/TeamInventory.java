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

public class TeamInventory {

    TeamManager tm;

    private Inventory inv;
    private final Component inventoryName;

    private Map<TrueTeam, Integer> inventoryLoc;

    public TeamInventory(TeamManager tm, Component inventoryName) {
        this.tm = tm;
        this.inventoryName = inventoryName;
    }

    public void createTeamInventory() {

        if (tm.getTeams().isEmpty()) return;

        int itemCount = tm.getTeams().size();
        int rowsNeeded = Items.determineRows(itemCount);
        int inventorySize = rowsNeeded * 9;

        Inventory inv = Bukkit.createInventory(null, inventorySize, inventoryName);

        List<ItemStack> teamItems = new ArrayList<>();

        List<TrueTeam> trueTrueTeams = tm.convertAbstractToTeam(tm.getTeams());

        for (TrueTeam trueTeam : trueTrueTeams) {
            teamItems.add(trueTeam.getItem());
        }

        List<Integer> slots = Items.generateSlots(itemCount, rowsNeeded);

        this.inventoryLoc = new HashMap<>();

        for (int i = 0; i < itemCount && i < slots.size(); i++) {
            inv.setItem(slots.get(i), teamItems.get(i));
            inventoryLoc.put(trueTrueTeams.get(i), slots.get(i));
        }

        List<HumanEntity> viewers = null;

        if (this.inv != null) {
            viewers = new ArrayList<>(this.inv.getViewers());
            for (HumanEntity humanEntity : viewers) {
                if (humanEntity instanceof Player p) {
                    System.out.println("Viewer: " + p.getName());
                    p.closeInventory();
                }
            }
        }

        this.inv = inv;

        if (viewers != null) {

            for (HumanEntity he : viewers) {

                if (he instanceof Player p) {
                    p.openInventory(this.inv);
                }
            }
        }
    }

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
