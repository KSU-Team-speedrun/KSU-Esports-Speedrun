package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerClick implements Listener {

    Main plugin;

    private TeamManager tm;

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

                ItemStack currentItem = e.getCurrentItem();

                if (currentItem != null) {

                    Team team = plugin.getSpeedrun().getTeams().getTeam(currentItem);

                    if (team != null) {

                        Team oldTeam = tm.getTeam(p);

                        if (oldTeam != null) {
                            oldTeam.removePlayer(p);
                            tm.getTeamInventory().updateTeamInventory(oldTeam);
                        }

                        team.addPlayer(p);
                        tm.getTeamInventory().updateTeamInventory(team);

                    }
                }
            }
        }
    }
}
