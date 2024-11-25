package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * The PlayerClick class implements the Listener interface and handles player interactions
 * with the team selection UI in the game. It determines actions performed when a player
 * clicks with a specific item and manages team assignments through an interactive inventory UI.
 */
public class PlayerClick implements Listener {

    Main plugin;

    private final TeamManager tm;

    public PlayerClick(Main plugin) {
        this.plugin = plugin;
        tm = plugin.getSpeedrun().getTeams();
    }

    // Logic to open team UI when a player lefts or right clicks w/ the compass
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {

        Player p = e.getPlayer();

        if (e.getAction().isLeftClick() || e.getAction().isRightClick()) {

            if (p.getInventory().getItemInMainHand().equals(Items.getTeamSelector())) {

                if (plugin.getSpeedrun().isParticipating(p)) {
                    p.openInventory(tm.getTeamInventory().getInventory());
                }
            }
        }
    }

    // Logic for clicks within inventory UI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        // If clicker is player
        if (e.getWhoClicked() instanceof Player p) {

            // and inventory clicked is the team UI
            if (e.getInventory().equals(tm.getTeamInventory().getInventory())) {

                // then cancel the event
                e.setCancelled(true);

                // If the clicker is a part of the cooldown, cancel the event
                if (plugin.getSpeedrun().getTeamCooldown().contains(p)) {
                    p.sendMessage(plugin.getMessages().getTeamCooldownMessage());
                    return;
                }

                // If the clicked item corresponds to a team inventory item, add that player to the team
                ItemStack currentItem = e.getCurrentItem();

                if (currentItem != null) {

                    TrueTeam trueTeam = plugin.getSpeedrun().getTeams().getTeam(currentItem);

                    if (trueTeam != null) {

                        // Don't allow the player to join if the team is full
                        if (trueTeam.isFull()) {
                            p.sendMessage(plugin.getMessages().getTeamIsFull());
                            return;
                        }

                        TrueTeam oldTrueTeam = (TrueTeam) tm.getTeam(p);

                        // Don't allow the player to join if they're alrady on that team
                        if (oldTrueTeam != null) {

                            if (oldTrueTeam.equals(trueTeam)) {
                                p.sendMessage(plugin.getMessages().getAlreadyOnTeam());
                                return;
                            }

                            oldTrueTeam.removePlayer(p);
                            tm.getTeamInventory().updateTeamInventory(oldTrueTeam);
                        }

                        // Ensure the inventory is updated to reflect the changes
                        trueTeam.addPlayer(p);
                        tm.getTeamInventory().updateTeamInventory(trueTeam);

                        plugin.getSpeedrun().addTeamCooldown(p);
                    }
                }
            }
        }
    }
}
