package edu.Kennesaw.ksumcspeedrun.Utilities;

import edu.Kennesaw.ksumcspeedrun.Objects.Objective.EnterObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
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

    public static int determineRows(int itemCount) {
        int itemsPerRow = itemCount;
        while (itemsPerRow > 9) {
            itemsPerRow = (int) Math.ceil(itemsPerRow / 2.0);
        }
        return (int) Math.ceil((double) itemCount / itemsPerRow);
    }

    public static List<Integer> generateSlots(int itemCount, int rowsNeeded) {
        List<Integer> slots = new ArrayList<>();
        int itemsPerRow = (int) Math.ceil((double) itemCount / rowsNeeded);
        int remainingItems = itemCount;
        int rowIndex = 0;

        while (remainingItems > 0) {
            int currentRowItems = Math.min(remainingItems, itemsPerRow);

            if (currentRowItems % 2 == 0) {
                slots.addAll(getEvenSlots(currentRowItems, rowIndex));
            } else {
                slots.addAll(getOddSlots(currentRowItems, rowIndex));
            }

            rowIndex++;
            remainingItems -= currentRowItems;
        }
        return slots;
    }

    private static List<Integer> getEvenSlots(int currentRowItems, int rowIndex) {
        List<Integer> slots = new ArrayList<>();
        int rowStartIndex = rowIndex * 9;
        int half = currentRowItems / 2;
        int leftStart = 4 - half;
        int rightStart = 4 + 1;

        for (int i = 0; i < half; i++) {
            slots.add(rowStartIndex + leftStart + i);
            slots.add(rowStartIndex + rightStart + i);
        }
        return slots;
    }

    private static List<Integer> getOddSlots(int currentRowItems, int rowIndex) {
        List<Integer> slots = new ArrayList<>();
        int startIndex = rowIndex * 9 + (9 - currentRowItems) / 2;

        for (int i = 0; i < currentRowItems; i++) {
            slots.add(startIndex + i);
        }

        return slots;
    }

    public static BookMeta getAdminBook(Speedrun speedrun) {

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        bookMeta.setAuthor("KSU Minecraft Esports");
        bookMeta.setTitle("Objectives");

        int maxCharsPerLine = 20;
        int maxLines = 14;

        List<Objective> objectives = speedrun.getObjectives().getObjectives();

        List<String> pages = new ArrayList<>();

        StringBuilder currentPage = new StringBuilder();

        int pageLine = 1;
        int totalPageChars = 0;

        for (int i = 0; i < objectives.size(); i++) {

            if (pageLine >= maxLines || totalPageChars >= maxCharsPerLine * maxLines) {
                pages.add(currentPage.toString());
                currentPage = new StringBuilder();
                pageLine = 1;
                totalPageChars = 0;
            }

            Objective o = objectives.get(i);
            if (o.getType().equals(Objective.ObjectiveType.ENTER)) {

            }
            String line = (i + 1) + ": " + o.getType().name() + " " + o.getTargetName();

            if (line.length() > maxCharsPerLine) {
                while (line.length() > maxCharsPerLine) {
                    String part = line.substring(0, maxCharsPerLine);
                    currentPage.append(part).append("\n");
                    line = line.substring(maxCharsPerLine);
                    pageLine++;
                    totalPageChars += maxCharsPerLine;
                }
                currentPage.append(line).append("\n");
                pageLine++;
                totalPageChars += line.length();
            } else {
                currentPage.append(line).append("\n");
                pageLine++;
                totalPageChars += line.length();
            }
        }

        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }

        bookMeta.setPages(pages);
        return bookMeta;
    }


    public static BookMeta getObjectiveBook(Team team) {

        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        bookMeta.setAuthor("KSU Minecraft Esports");
        bookMeta.setTitle("Objectives");

        List<String> pages = new ArrayList<>();
        StringBuilder currentPage = new StringBuilder();

        int maxLinesPerPage = 14;
        int maxCharsPerLine = 22;
        int currentLine = 0;
        String currentColorCode;

        currentPage.append("Incomplete Objectives:").append("\n");
        currentLine++;
        currentColorCode = "§c";

        int spaceFull = 28;
        int letterFull = 19;
        double ratio = (double) spaceFull / letterFull;

        for (Objective objective : team.getIncompleteObjectives()) {
            appendLines(objective, currentLine, currentColorCode, ratio, maxCharsPerLine, maxLinesPerPage, spaceFull,
                    currentPage, pages);
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

        for (Objective objective : team.getCompleteObjectives()) {
            appendLines(objective, currentLine, currentColorCode, ratio, maxCharsPerLine, maxLinesPerPage, spaceFull,
                    currentPage, pages);
        }

        if (!currentPage.isEmpty()) {
            pages.add(currentPage.toString());
        }

        //noinspection deprecation
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

    private static void appendLines(Objective objective, int currentLine, String currentColorCode, double ratio,
                                    int maxCharsPerLine, int maxLinesPerPage, int spaceFull,
                                    StringBuilder currentPage, List<String> pages) {
        StringBuilder objectiveText;

        objectiveText = new StringBuilder(currentColorCode + "- " + objective.getType() + ": " + objective.getTargetName());

        int i = objectiveText.toString().indexOf(':');

        String left = objectiveText.substring(0, i);
        String right = objectiveText.substring(i + 1).trim();

        if (objectiveText.length() > 22) {

            objectiveText = new StringBuilder(left + ":");

            int leftSpaces;

            if (objectiveText.toString().split(" ").length == 3) {

                leftSpaces = (int) ((left.length() - 3) * ratio + 1);

            } else {
                leftSpaces = (int) ((left.length() - 4) * ratio + 2);

            }

            int spacesToAdd = spaceFull - leftSpaces - 1;

            objectiveText.append(" ".repeat(Math.max(0, spacesToAdd))).append(right);

        }

        List<String> wrappedLines = wrapText(objectiveText.toString(), maxCharsPerLine);
        for (String line : wrappedLines) {
            if (currentLine >= maxLinesPerPage) {

                pages.add(currentPage.toString());
                currentPage = new StringBuilder();
                currentLine = 0;

                currentPage.append(currentColorCode);
            }
            currentPage.append(line).append("\n");
            currentLine++;
        }
    }

}
