package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class PlayerClick implements Listener {

    Main plugin;

    public PlayerClick(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {

        Player p = e.getPlayer();

        if (e.getAction().isLeftClick() || e.getAction().isLeftClick()) {

            if (p.getInventory().getItemInMainHand().equals(Items.getTeamSelector())) {

                p.openInventory(Items.getTeamInventory(p));

            }

        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (e.getView().title().equals(Component.text("Team Selection").color(TextColor.fromHexString("#FFFF55")).decoration(TextDecoration.BOLD, true))) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null) {
                    if (e.getCurrentItem().getItemMeta().hasDisplayName()) {
                        System.out.println(e.getCurrentItem().displayName());
                        String displayName = e.getCurrentItem().getItemMeta().getDisplayName().strip();
                        String[] displaySplit = displayName.split(" ");
                        String teamName = displaySplit[0].substring(2).toLowerCase();
                        System.out.println(teamName);
                        if (plugin.getSpeedrun().getTeams().getTeam(teamName) != null) {
                            plugin.getSpeedrun().getTeams().getTeam(teamName).addPlayer(p);
                            p.sendMessage(Component.text("Joined " + displaySplit[0].toLowerCase() + " team."));
                        }
                    }
                }
            }
        }
    }

}
