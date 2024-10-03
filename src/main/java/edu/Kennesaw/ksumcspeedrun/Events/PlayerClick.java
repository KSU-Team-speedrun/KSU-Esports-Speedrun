package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Utilities.Items;
import net.kyori.adventure.text.Component;
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
        Bukkit.broadcastMessage("inventory click");
        if (e.getWhoClicked() instanceof Player p) {
            p.sendMessage("player");
            if (e.getCurrentItem() != null) {
                if (e.getCurrentItem().getType().equals(Material.WHITE_WOOL)) {
                    p.sendMessage("white wool");
                    plugin.getSpeedrun().getTeams().getTeam("white").addPlayer(p);
                    e.setCancelled(true);
                    p.sendMessage(Component.text("Joined white team"));
                }
            }
        }
    }

}
