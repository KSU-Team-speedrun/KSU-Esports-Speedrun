package edu.Kennesaw.ksumcspeedrun.Utilities;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.*;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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

    public static Book getObjectiveBookMain() {

        MiniMessage mm = MiniMessage.miniMessage();

        Component page = mm.deserialize("<gold><b>   KSU Speedrun</b></gold>").appendNewline();
        page = page.append(mm.deserialize("<gold><b> Make a Selection:</b></gold>")).appendNewline().appendNewline();

        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_red>View Incomplete Objectives</dark_red></b>'><click:run_command:'/objectives incomplete'><dark_red><b>   -----------</b></dark_red></click></hover>")).appendNewline();
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_red>View Incomplete Objectives</dark_red></b>'><click:run_command:'/objectives incomplete'><dark_red><b>    INCOMPLETE</b></dark_red></click></hover>").appendNewline());
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_red>View Incomplete Objectives</dark_red></b>'><click:run_command:'/objectives incomplete'><dark_red><b>    OBJECTIVES</b></dark_red></click></hover>").appendNewline());
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_red>View Incomplete Objectives</dark_red></b>'><click:run_command:'/objectives incomplete'><dark_red><b>   -----------</b></dark_red></click></hover>")).appendNewline().appendNewline();

        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_green>View Complete Objectives</dark_green></b>'><click:run_command:'/objectives complete'><dark_green><b>   -----------</b></dark_green></click></hover>")).appendNewline();
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_green>View Complete Objectives</dark_green></b>'><click:run_command:'/objectives complete'><dark_green><b>     COMPLETE</b></dark_green></click></hover>").appendNewline());
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_green>View Complete Objectives</dark_green></b>'><click:run_command:'/objectives complete'><dark_green><b>    OBJECTIVES</b></dark_green></click></hover>").appendNewline());
        page = page.append(mm.deserialize("<hover:show_text:'<b><dark_green>View Complete Objectives</dark_green></b>'><click:run_command:'/objectives complete'><dark_green><b>   -----------</b></dark_green></click></hover>")).appendNewline();

        Book.Builder book = Book.builder();
        book.addPage(page);

        return book.build();

    }

    public static Book getObjectiveBook(Team team, boolean isWeighted, boolean isIncomplete) {

        List<Component> pages = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();

        Component page = Component.empty();
        int lines = 1;

        if (isIncomplete) {

            List<Objective> incompleteObjectives = team.getIncompleteObjectives();

            lines = addObjectivesByType(page, mm, "ENTER", filterObjectives(incompleteObjectives, EnterObjective.class), isWeighted, lines, pages, false, team);
            lines = addObjectivesByType(page, mm, "KILL", filterObjectives(incompleteObjectives, KillObjective.class), isWeighted, lines, pages, false, team);
            lines = addObjectivesByType(page, mm, "MINE", filterObjectives(incompleteObjectives, MineObjective.class), isWeighted, lines, pages, false, team);
            addObjectivesByType(page, mm, "OBTAIN", filterObjectives(incompleteObjectives, ObtainObjective.class), isWeighted, lines, pages, false, team);

        } else {

            List<Objective> completeObjectives = team.getCompleteObjectives();

            if (completeObjectives.isEmpty()) {
                pages.add(page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>")));
            } else {
                lines = addObjectivesByType(page, mm, "ENTER", filterObjectives(completeObjectives, EnterObjective.class), isWeighted, lines, pages, true, team);
                lines = addObjectivesByType(page, mm, "KILL", filterObjectives(completeObjectives, KillObjective.class), isWeighted, lines, pages, true, team);
                lines = addObjectivesByType(page, mm, "MINE", filterObjectives(completeObjectives, MineObjective.class), isWeighted, lines, pages, true, team);
                addObjectivesByType(page, mm, "OBTAIN", filterObjectives(completeObjectives, ObtainObjective.class), isWeighted, lines, pages, true, team);
            }

        }

        Book.Builder book = Book.builder();
        book.pages(pages);

        return book.build();
    }

    private static <T extends Objective> List<T> filterObjectives(List<Objective> objectives, Class<T> type) {
        List<T> filteredObjectives = new ArrayList<>();
        for (Objective o : objectives) {
            if (type.isInstance(o)) {
                filteredObjectives.add(type.cast(o));
            }
        }
        return filteredObjectives;
    }

    private static int addObjectivesByType(Component page, MiniMessage mm, String type, List<? extends Objective> objectives, boolean isWeighted, int lines, List<Component> pages, boolean isComplete, Team team) {
        if (!objectives.isEmpty()) {
            String color = isComplete ? "dark_green" : "dark_red";
            objectives.sort((o1, o2) -> Integer.compare(o2.getWeight(), o1.getWeight()));
            page = page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>")).appendNewline().appendNewline().append(mm.deserialize("<" + color + "><bold>" + type + ":</bold></" + color + ">")).appendNewline()
                    .appendNewline().decoration(TextDecoration.BOLD, false).decoration(TextDecoration.STRIKETHROUGH, false);
            lines+=4;

            for (Objective obj : objectives) {
                if (lines >= 14) {
                    pages.add(page);
                    page = Component.empty();
                    lines = 4;
                    page = page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>")).appendNewline().appendNewline().append(mm.deserialize("<" + color + "><bold>" + type + ":</bold></" + color + ">")).appendNewline()
                            .appendNewline().decoration(TextDecoration.BOLD, false).decoration(TextDecoration.STRIKETHROUGH, false);
                    lines++;
                }

                page = page.append(mm.deserialize("<reset><black>" + "<hover:show_text:'<b><gold>Points Awarded: " + obj.getWeight() + "</gold></b>'>- " + obj.getTargetName() + "</hover>"));
                if (obj.getAmount() > 1) {
                    page = page.append(mm.deserialize(" <hover:show_text:'<b><gold>Current Progress: " + obj.getCount(team) + "/" + obj.getAmount() + "</gold></b>'>(x" + obj.getAmount() + ")</hover>"));
                }
                page = page.appendNewline();
                lines++;

                //if (isWeighted) {
                    //page = page.append(mm.deserialize("<white>.</white>  - " + obj.getWeight() + " points")).appendNewline();
                    //lines++;
                //}
            }
            pages.add(page);
            lines = 1;
        }
        return lines;
    }

}
