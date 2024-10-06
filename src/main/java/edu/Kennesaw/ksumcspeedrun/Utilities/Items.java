package edu.Kennesaw.ksumcspeedrun.Utilities;

import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {

    public static ItemStack getTeamSelector() {

        ItemStack selector = new ItemStack(Material.COMPASS);
        ItemMeta meta = selector.getItemMeta();

        TextComponent name = Component.text("Team Selector").color(TextColor.fromHexString("#FFFF55")).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true);
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        TextComponent lore1 = Component.text("Click me to select your team!").color(TextColor.fromHexString("#AAAAAA")).decoration(TextDecoration.ITALIC, false);
        lore.add(lore1);
        meta.lore(lore);

        selector.setItemMeta(meta);

        return selector;

    }

    public static Inventory getTeamInventory(Player p) {

        Inventory inv = Bukkit.createInventory(p, 36, Component.text("Team Selection").color(TextColor.fromHexString("#FFFF55")).decoration(TextDecoration.BOLD, true));

        ItemStack white = new ItemStack(Material.WHITE_WOOL);
        ItemMeta whitemeta = white.getItemMeta();
        whitemeta.displayName(Component.text("White Team").color(TextColor.fromHexString("#FFFFFF")).decoration(TextDecoration.ITALIC, false));
        white.setItemMeta(whitemeta);
        inv.setItem(3, white);

        ItemStack lightGray = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta lightGraymeta = lightGray.getItemMeta();
        lightGraymeta.displayName(Component.text("Light Gray Team").color(TextColor.fromHexString("#D3D3D3")).decoration(TextDecoration.ITALIC, false));
        lightGray.setItemMeta(lightGraymeta);
        inv.setItem(4, lightGray);

        ItemStack gray = new ItemStack(Material.GRAY_WOOL);
        ItemMeta graymeta = gray.getItemMeta();
        graymeta.displayName(Component.text("Gray Team").color(TextColor.fromHexString("#808080")).decoration(TextDecoration.ITALIC, false));
        gray.setItemMeta(graymeta);
        inv.setItem(5, gray);

        ItemStack black = new ItemStack(Material.BLACK_WOOL);
        ItemMeta blackmeta = black.getItemMeta();
        blackmeta.displayName(Component.text("Black Team").color(TextColor.fromHexString("#000000")).decoration(TextDecoration.ITALIC, false));
        black.setItemMeta(blackmeta);
        inv.setItem(11, black);

        ItemStack brown = new ItemStack(Material.BROWN_WOOL);
        ItemMeta brownmeta = brown.getItemMeta();
        brownmeta.displayName(Component.text("Brown Team").color(TextColor.fromHexString("#8B4513")).decoration(TextDecoration.ITALIC, false));
        brown.setItemMeta(brownmeta);
        inv.setItem(12, brown);

        ItemStack red = new ItemStack(Material.RED_WOOL);
        ItemMeta redmeta = red.getItemMeta();
        redmeta.displayName(Component.text("Red Team").color(TextColor.fromHexString("#FF0000")).decoration(TextDecoration.ITALIC, false));
        red.setItemMeta(redmeta);
        inv.setItem(13, red);

        ItemStack orange = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta orangemeta = orange.getItemMeta();
        orangemeta.displayName(Component.text("Orange Team").color(TextColor.fromHexString("#FFA500")).decoration(TextDecoration.ITALIC, false));
        orange.setItemMeta(orangemeta);
        inv.setItem(14, orange);

        ItemStack yellow = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta yellowmeta = yellow.getItemMeta();
        yellowmeta.displayName(Component.text("Yellow Team").color(TextColor.fromHexString("#FFFF00")).decoration(TextDecoration.ITALIC, false));
        yellow.setItemMeta(yellowmeta);
        inv.setItem(15, yellow);

        ItemStack lime = new ItemStack(Material.LIME_WOOL);
        ItemMeta limemeta = lime.getItemMeta();
        limemeta.displayName(Component.text("Lime Team").color(TextColor.fromHexString("#32CD32")).decoration(TextDecoration.ITALIC, false));
        lime.setItemMeta(limemeta);
        inv.setItem(20, lime);

        ItemStack green = new ItemStack(Material.GREEN_WOOL);
        ItemMeta greenmeta = green.getItemMeta();
        greenmeta.displayName(Component.text("Green Team").color(TextColor.fromHexString("#008000")).decoration(TextDecoration.ITALIC, false));
        green.setItemMeta(greenmeta);
        inv.setItem(21, green);

        ItemStack cyan = new ItemStack(Material.CYAN_WOOL);
        ItemMeta cyanmeta = cyan.getItemMeta();
        cyanmeta.displayName(Component.text("Cyan Team").color(TextColor.fromHexString("#00FFFF")).decoration(TextDecoration.ITALIC, false));
        cyan.setItemMeta(cyanmeta);
        inv.setItem(22, cyan);

        ItemStack lightBlue = new ItemStack(Material.LIGHT_BLUE_WOOL);
        ItemMeta lightBluemeta = lightBlue.getItemMeta();
        lightBluemeta.displayName(Component.text("Light Blue Team").color(TextColor.fromHexString("#ADD8E6")).decoration(TextDecoration.ITALIC, false));
        lightBlue.setItemMeta(lightBluemeta);
        inv.setItem(23, lightBlue);

        ItemStack blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta bluemeta = blue.getItemMeta();
        bluemeta.displayName(Component.text("Blue Team").color(TextColor.fromHexString("#0000FF")).decoration(TextDecoration.ITALIC, false));
        blue.setItemMeta(bluemeta);
        inv.setItem(24, blue);

        ItemStack purple = new ItemStack(Material.PURPLE_WOOL);
        ItemMeta purplemeta = purple.getItemMeta();
        purplemeta.displayName(Component.text("Purple Team").color(TextColor.fromHexString("#800080")).decoration(TextDecoration.ITALIC, false));
        purple.setItemMeta(purplemeta);
        inv.setItem(30, purple);

        ItemStack magenta = new ItemStack(Material.MAGENTA_WOOL);
        ItemMeta magentameta = magenta.getItemMeta();
        magentameta.displayName(Component.text("Magenta Team").color(TextColor.fromHexString("#FF00FF")).decoration(TextDecoration.ITALIC, false));
        magenta.setItemMeta(magentameta);
        inv.setItem(31, magenta);

        ItemStack pink = new ItemStack(Material.PINK_WOOL);
        ItemMeta pinkmeta = pink.getItemMeta();
        pinkmeta.displayName(Component.text("Pink Team").color(TextColor.fromHexString("#FFC0CB")).decoration(TextDecoration.ITALIC, false));
        pink.setItemMeta(pinkmeta);
        inv.setItem(32, pink);

        return inv;

    }

    public static BookMeta getObjectiveBook(Team team) {

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        bookMeta.setAuthor("AuthorName");
        bookMeta.setTitle("Objectives");

        List<String> pages = new ArrayList<>();
        StringBuilder currentPage = new StringBuilder();

        int maxLinesPerPage = 14;
        int maxCharsPerLine = 22;
        int currentLine = 0;
        String currentColorCode = "";

        currentPage.append("Incomplete Objectives:").append("\n");
        currentLine++;
        currentColorCode = "§c";

        for (Objective objective : team.getIncompleteObjectives()) {
            String objectiveText = currentColorCode + "- " + objective.getType() + " " + objective.getTargetName();

            List<String> wrappedLines = wrapText(objectiveText, maxCharsPerLine);
            for (String line : wrappedLines) {
                if (currentLine >= maxLinesPerPage) {

                    pages.add(currentPage.toString());
                    currentPage = new StringBuilder();
                    currentLine = 0;

                    if (!currentColorCode.isEmpty()) {
                        currentPage.append(currentColorCode);
                    }
                }
                currentPage.append(line).append("\n");
                currentLine++;
            }
        }

        if (currentLine < maxLinesPerPage) {
            currentPage.append("\n§0Completed Objectives:\n");
            currentLine += 2;
        } else {

            pages.add(currentPage.toString());
            currentPage = new StringBuilder("§0Completed Objectives:\n");
            currentLine = 1;
        }
        currentColorCode = "§a";

        for (Objective objective : team.getCompletedObjectives()) {
            String objectiveText = currentColorCode + "- " + objective.getType() + " " + objective.getTargetName();

            List<String> wrappedLines = wrapText(objectiveText, maxCharsPerLine);
            for (String line : wrappedLines) {
                if (currentLine >= maxLinesPerPage) {

                    pages.add(currentPage.toString());
                    currentPage = new StringBuilder();
                    currentLine = 0;

                    if (!currentColorCode.isEmpty()) {
                        currentPage.append(currentColorCode);
                    }
                }
                currentPage.append(line).append("\n");
                currentLine++;
            }
        }

        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }

        bookMeta.setPages(pages);

        return bookMeta;
    }

    private static List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> wrappedLines = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            wrappedLines.add(text.substring(index, Math.min(index + maxCharsPerLine, text.length())));
            index += maxCharsPerLine;
        }
        return wrappedLines;
    }
}
