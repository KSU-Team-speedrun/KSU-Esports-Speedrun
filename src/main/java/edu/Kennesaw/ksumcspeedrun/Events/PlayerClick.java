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

public class PlayerClick implements Listener {

    Main plugin;

    private final TeamManager tm;

    public PlayerClick(Main plugin) {
        this.plugin = plugin;
        tm = plugin.getSpeedrun().getTeams();
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {

        Player p = e.getPlayer();

        if (e.getAction().isLeftClick() || e.getAction().isLeftClick()) {

            if (p.getInventory().getItemInMainHand().equals(Items.getTeamSelector())) {

                p.openInventory(tm.getTeamInventory().getInventory());

            }

        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (e.getWhoClicked() instanceof Player p) {

            if (e.getInventory().equals(tm.getTeamInventory().getInventory())) {

                e.setCancelled(true);

                if (plugin.getSpeedrun().teamCooldown.contains(p)) {
                    p.sendMessage(plugin.getMessages().getTeamCooldownMessage());
                    return;
                }

                ItemStack currentItem = e.getCurrentItem();

                if (currentItem != null) {

                    TrueTeam trueTeam = plugin.getSpeedrun().getTeams().getTeam(currentItem);

                    if (trueTeam != null) {

                        if (trueTeam.isFull()) {
                            p.sendMessage(plugin.getMessages().getTeamIsFull());
                            return;
                        }

                        TrueTeam oldTrueTeam = (TrueTeam) tm.getTeam(p);

                        if (oldTrueTeam != null) {

                            if (oldTrueTeam.equals(trueTeam)) {
                                p.sendMessage(plugin.getMessages().getAlreadyOnTeam());
                                return;
                            }

                            oldTrueTeam.removePlayer(p);
                            tm.getTeamInventory().updateTeamInventory(oldTrueTeam);
                        }

                        trueTeam.addPlayer(p);
                        tm.getTeamInventory().updateTeamInventory(trueTeam);

                        plugin.getSpeedrun().teamCooldown.add(p);

                        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask ->
                                        plugin.getSpeedrun().teamCooldown.remove(p),
                                plugin.getConfig().getInt("teams.inventory.cooldown"), TimeUnit.SECONDS);
                    }
                }
            }
        }
    }
}
