package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Team {

    Main plugin;

    private final Component name;
    private int points = 0;
    private final List<Player> players = new ArrayList<>();
    private ItemStack item;
    private final TeamManager tm;


    public Team(Main plugin, Component teamName, ItemStack item) {
        this.plugin = plugin;
        this.name = teamName;
        this.item = item;
        tm = plugin.getSpeedrun().getTeams();
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.sendMessage(plugin.getSpeedrunConfig()
                .getPrefix().append(Component.text("Joined team: ")).append(name));
        tm.setPlayerTeam(player, this);
        updateItemLore();
    }

    public void removePlayer(Player player) {
        players.remove(player);
        tm.removePlayerTeam(player);
        updateItemLore();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }


    public void changeLastPlayer(Team team) {
        team.addPlayer(players.getLast());
    }

    public Component getName() {
        return this.name;
    }

    public int getPoints() {
        return this.points;
    }

    public void addPoints(int points) {
        this.points += points;
        if (this.points >= plugin.getSpeedrun().getTotalWeight()) {
            plugin.getSpeedrun().endGame(this);
        }
    }

    public List<Objective> getCompletedObjectives() {
        List<Objective> completedObjectives = new ArrayList<>();
        for (Objective o : plugin.getSpeedrun().getObjectives().getObjectives()) {
            if (o.getCompleteTeams().contains(this)) {
                completedObjectives.add(o);
            }
        }
        return completedObjectives;
    }

    public List<Objective> getIncompleteObjectives() {
        return plugin.getSpeedrun().getObjectives().getIncompleteObjectives(this);
    }

    public int getSize() {
        return players.size();
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        tm.addTeamItem(this, item);
        this.item = item;
    }

    private void updateItemLore() {
        ItemMeta im = item.getItemMeta();
        List<Component> lore = im.lore();
        if (lore != null) {
            int sizeLimit = tm.getSizeLimit();
            if (getSize() == sizeLimit) {
                lore.set(1, Component.text("This team is FULL!")
                        .color(TextColor.fromHexString("#ff0000")).decorate(TextDecoration.BOLD));
            } else {
                lore.set(1, Component.text(getSize() + "/" + tm.getSizeLimit() + " players on this team.")
                        .color(TextColor.fromHexString("#c4c4c4")));
            }
        }

        im.lore(lore);
        item.setItemMeta(im);
        setItem(item);
    }

}
