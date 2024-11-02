package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Team {

    Main plugin;

    private final Component name;
    private String strippedName;
    private int points = 0;
    private final List<Player> players = new ArrayList<>();
    private ItemStack item;
    private final TeamManager tm;
    private final List<Objective> completedObjectives = new ArrayList<>();


    public Team(Main plugin, Component teamName, ItemStack item) {
        this.plugin = plugin;
        this.name = teamName;
        this.strippedName = PlainTextComponentSerializer.plainText().serialize(teamName);
        this.item = item;
        tm = plugin.getSpeedrun().getTeams();
    }

    public void addPlayer(Player player) {
        if (players.contains(player)) {
            player.sendMessage(plugin.getMessages().getAlreadyOnTeam());
            return;
        }
        if (isFull()) {
            player.sendMessage(plugin.getMessages().getTeamIsFull());
            return;
        }
        players.add(player);
        player.sendMessage(plugin.getMessages().getTeamJoinMessage(name));
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

    @SuppressWarnings("unused")
    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    @SuppressWarnings("unused")
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

    public List<Objective> getIncompleteObjectives() {
        return plugin.getSpeedrun().getObjectives().getIncompleteObjectives(this);
    }

    public List<Objective> getCompleteObjectives() {
        return completedObjectives;
    }

    public void addCompleteObjective(Objective o) {
        completedObjectives.add(o);
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

    public boolean isFull() {
        return (players.size() == tm.getSizeLimit());
    }

    private void updateItemLore() {
        ItemMeta im = item.getItemMeta();
        List<Component> lore = im.lore();
        if (lore != null) {
            if (isFull()) {
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

    public String getStrippedName() {
        return strippedName;
    }

    public void setStrippedName(String strippedName) {
        this.strippedName = strippedName;
    }

}
