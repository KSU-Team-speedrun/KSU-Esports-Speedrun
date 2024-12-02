package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Utilities.ComponentHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TrueTeam extends Team {

    Main plugin;

    private final Component name;
    private String strippedName;
    private int points = 0;
    private final List<Player> players = new ArrayList<>();
    private ItemStack item;
    private final TeamManager tm;
    private final List<Objective> completedObjectives = new ArrayList<>();
    private Location respawnLocation;


    public TrueTeam(Main plugin, Component teamName, ItemStack item) {
        super(plugin);
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
        players.add(player);
        player.sendMessage(plugin.getMessages().getTeamJoinMessage(this.name));
        tm.setPlayerTeam(player, this);
        updateItemLore(true, player);
    }

    public void removePlayer(Player player) {
        updateItemLore(false, player);
        players.remove(player);
        tm.removePlayerTeam(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    @SuppressWarnings("unused")
    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    @SuppressWarnings("unused")
    public void changeLastPlayer(TrueTeam trueTeam) {
        trueTeam.addPlayer(players.getLast());
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

    public void removePoints(int points) {
        this.points -= points;
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

    private void updateItemLore(boolean adding, Player p) {
        ItemMeta im = item.getItemMeta();
        List<Component> lore = im.lore();
        if (lore != null) {
            if (isFull() && adding) {
                lore.set(1, Component.text("This team is FULL!")
                        .color(TextColor.fromHexString("#ff0000")).decorate(TextDecoration.BOLD));
            } else {
                lore.set(1, Component.text((getSize() + (adding ? 0 : -1)) + "/" + tm.getSizeLimit() + " players on this team.")
                        .color(TextColor.fromHexString("#c4c4c4")));
            }
            if (adding) {
                lore.add(players.indexOf(p) + 2, ComponentHelper.mmStringToComponent("<gray> - " + p.getName() + "</gray>").decoration(TextDecoration.ITALIC, false));
            } else {
                lore.remove(players.indexOf(p) + 2);
            }
        }

        im.lore(lore);
        item.setItemMeta(im);
        setItem(item);
    }

    @Override
    public String getStrippedName() {
        return strippedName;
    }

    public void setStrippedName(String strippedName) {
        this.strippedName = strippedName;
    }

    public void setRespawnLocation(Location location) {
        this.respawnLocation = location;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

}
