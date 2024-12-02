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

/**
 * This class declares certain static items that are used throughout the plugin or perhaps referenced in
 * multiple classes.
 *
 * These items include:
 * - The Team Selector Compass
 * - The Admin Objective Book
 * - The Player Objective Book
 *
 * This class also contains static methods that implement the logic used to determine the size and contents
 * of the Team Inventory.
 */

public class Items {

    // This method returns the Team Selector and can be accessed in any class
    // TODO - The Team Selector should be configurable.
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

    /* This method calculates the number of rows required to display a given number of items, ensuring that each row
       contains no more than 9 items. It dynamically adjusts the number of items per row by halving the count until
       it meets the maximum limit and then computes the rows needed to fit all items. */
    public static int determineRows(int itemCount) {
        int itemsPerRow = itemCount;
        while (itemsPerRow > 9) {
            itemsPerRow = (int) Math.ceil(itemsPerRow / 2.0);
        }
        return (int) Math.ceil((double) itemCount / itemsPerRow);
    }

    /* This method generates a list of slot indices for displaying items across a specified number of rows,
       ensuring even distribution based on the number of items per row. It determines whether each row's
       slots should use even or odd indices and adjusts dynamically as rows are processed. */
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

    /* This method generates a list of slot indices for a row containing an even number of items, distributing them
       symmetrically from the center of the row. It calculates the starting positions for items on the left and right
       sides and fills the row accordingly. */
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

    /* This method generates a list of slot indices for a row containing an odd number of items, centering
       them within the row. It calculates the starting index to ensure the items are evenly distributed and
       sequentially fills the slots. */
    private static List<Integer> getOddSlots(int currentRowItems, int rowIndex) {
        List<Integer> slots = new ArrayList<>();
        int startIndex = rowIndex * 9 + (9 - currentRowItems) / 2;

        for (int i = 0; i < currentRowItems; i++) {
            slots.add(startIndex + i);
        }

        return slots;
    }

    /* This method returns the ADMIN objective book - which actually contains much fewer data than the player objective
       book - the admin book is not designed to look pretty - it simply serves two functions:
       1. See all objectives that are created
       2. Determine the number that corresponds to a specific objective so it can be removed */
    public static BookMeta getAdminBook(Speedrun speedrun) {

        // Create a book and extract its BookMeta
        ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        // Author and Title don't actually matter as they're never shown to the player
        bookMeta.setAuthor("KSU Minecraft Esports");
        bookMeta.setTitle("Objectives");

        // Set these numbers to ensure no data is lost if it goes off a line & data is properly
        // added to a following page if the current page is full
        int maxCharsPerLine = 20;
        int maxLines = 14;

        // Extract all Objectives
        List<Objective> objectives = speedrun.getObjectives().getObjectives();

        // Create a String list called "pages" - each individual String is a whole page
        List<String> pages = new ArrayList<>();

        // Create a starting string for the first page - create it using stringbuilder
        StringBuilder currentPage = new StringBuilder();

        // The first line is line 1
        int pageLine = 1;

        // We also keep track of TOTAL page characters as a fallback security method to tracking # of lines
        int totalPageChars = 0;

        // Loop through every objective - here, (i + 1) is the number that corresponds to objective[i]
        for (int i = 0; i < objectives.size(); i++) {

            // If the page is full of lines or has surpassed the total page character limit, we add the current page
            // to the list of pages, and then start a new page for the remaining objectives
            if (pageLine >= maxLines || totalPageChars >= maxCharsPerLine * maxLines) {
                pages.add(currentPage.toString());
                currentPage = new StringBuilder();
                pageLine = 1;
                totalPageChars = 0;
            }

            // Get objective[i]
            Objective o = objectives.get(i);

            // This is the line that gets printed in the book
            // e.g.: "1: KILL ENDER_DRAGON" <- ENDER_DRAGON would be objective[0], the first in the list.
            String line = (i + 1) + ": " + o.getType().name() + " " + o.getTargetName();

            // If the objective takes up more than one line, properly write it without losing data
            // Ensure page lines & total page chars are incremented properly
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

        // Make sure empty pages are not added
        if (!currentPage.isEmpty()) {
            pages.add(currentPage.toString());
        }

        // Update the book meta w/ created pages
        // TODO - USE "BOOK" INSTEAD OF DEPRECATED BOOKMETA (SEE BELOW METHODS)
        bookMeta.setPages(pages);

        // Return updated book meta
        return bookMeta;
    }

    /* Main page displayed to players when they type /objectives - players can interact w/ the book
       to view complete or incomplete objectives (Using MiniMessage) */
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

    /* This method creates and returns a `Book` containing objectives for a team.
       The book can display either incomplete or complete objectives, grouped by type (e.g., ENTER, KILL, etc.),
       and optionally weighted by their point value.

       If the book is for incomplete objectives:
       1. It adds objectives filtered by type, such as ENTER, KILL, MINE, or OBTAIN, and formats them accordingly.
       2. The objectives are displayed with hoverable and clickable components.

       If the book is for complete objectives:
       1. If no objectives exist, a single page with a "Go Back" option is displayed.
       2. Otherwise, objectives are grouped, sorted by type, and formatted with their weights and progress.
    */
    public static Book getObjectiveBook(Team team, boolean isWeighted, boolean isIncomplete) {
        // Create an empty list for pages and initialize MiniMessage for text formatting
        List<Component> pages = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();

        // Initialize the first page and set line counter
        Component page = Component.empty();
        int lines = 1;

        // If we're displaying incomplete objectives...
        if (isIncomplete) {
            // Get all incomplete objectives for the team and add them by type
            List<Objective> incompleteObjectives = team.getIncompleteObjectives();

            // Add objectives for each type (ENTER, KILL, etc.) and update line counts
            lines = addObjectivesByType(page, mm, "ENTER", filterObjectives(incompleteObjectives, EnterObjective.class), isWeighted, lines, pages, false, team);
            lines = addObjectivesByType(page, mm, "KILL", filterObjectives(incompleteObjectives, KillObjective.class), isWeighted, lines, pages, false, team);
            lines = addObjectivesByType(page, mm, "MINE", filterObjectives(incompleteObjectives, MineObjective.class), isWeighted, lines, pages, false, team);
            addObjectivesByType(page, mm, "OBTAIN", filterObjectives(incompleteObjectives, ObtainObjective.class), isWeighted, lines, pages, false, team);

            // If we're displaying complete objectives...
        } else {
            // Get all completed objectives for the team
            List<Objective> completeObjectives = team.getCompleteObjectives();

            // If no objectives are completed, display a "Go Back" page
            if (completeObjectives.isEmpty()) {
                pages.add(page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>")));
            } else {
                // Otherwise, add completed objectives for each type (ENTER, KILL, etc.)
                lines = addObjectivesByType(page, mm, "ENTER", filterObjectives(completeObjectives, EnterObjective.class), isWeighted, lines, pages, true, team);
                lines = addObjectivesByType(page, mm, "KILL", filterObjectives(completeObjectives, KillObjective.class), isWeighted, lines, pages, true, team);
                lines = addObjectivesByType(page, mm, "MINE", filterObjectives(completeObjectives, MineObjective.class), isWeighted, lines, pages, true, team);
                addObjectivesByType(page, mm, "OBTAIN", filterObjectives(completeObjectives, ObtainObjective.class), isWeighted, lines, pages, true, team);
            }
        }

        // Build and return the final book with the created pages
        Book.Builder book = Book.builder();
        book.pages(pages);

        return book.build();
    }

    /* This helper method filters a list of objectives by their specific type.

       1. It iterates through the provided list of objectives and checks if each matches the given type.
       2. If an objective matches, it is cast to the given type and added to the result list.
       3. The method returns the filtered list of objectives for further processing. */
    private static <T extends Objective> List<T> filterObjectives(List<Objective> objectives, Class<T> type) {
        // Create an empty list for filtered objectives
        List<T> filteredObjectives = new ArrayList<>();

        // Loop through all objectives and add those matching the given type
        for (Objective o : objectives) {
            if (type.isInstance(o)) {
                filteredObjectives.add(type.cast(o));
            }
        }
        return filteredObjectives;
    }

    /* This helper method adds objectives of a specific type to the book pages.

     1. Objectives are grouped and displayed under a header with their type (e.g., ENTER, KILL).
     2. If the objectives are complete, they are displayed in green; otherwise, in red.
     3. Each objective is formatted with hoverable text showing details like points and progress.
     4. If a page becomes full, it starts a new page and resets the line counter.
     5. The method returns the updated line count after processing the objectives. */
    private static int addObjectivesByType(Component page, MiniMessage mm, String type, List<? extends Objective> objectives, boolean isWeighted, int lines, List<Component> pages, boolean isComplete, Team team) {

        // Skip if there are no objectives of this type
        if (!objectives.isEmpty()) {

            // Determine the color for the header based on completion status
            String color = isComplete ? "dark_green" : "dark_red";

            // Determine the color for the header based on completion status
            objectives.sort((o1, o2) -> Integer.compare(o2.getWeight(), o1.getWeight()));

            // Add a header for this type of objective to the page
            page = page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>"))
                    .appendNewline()
                    .appendNewline()
                    .append(mm.deserialize("<" + color + "><bold>" + type + ":</bold></" + color + ">"))
                    .appendNewline()
                    .appendNewline()
                    .decoration(TextDecoration.BOLD, false).decoration(TextDecoration.STRIKETHROUGH, false);

            // Increment the line count for the added header
            lines+=4;

            // Loop through each objective and add it to the page
            for (Objective obj : objectives) {

                // If the current page is full, add it to the list and start a new page
                if (lines >= 14) {
                    pages.add(page);
                    page = Component.empty();

                    // Reset the line count and add a new header to the new page
                    lines = 4;
                    page = page.append(mm.deserialize("<hover:show_text:'<b><gold>Go Back</gold></b>'><click:run_command:'/objectives'><i>< Back</i></click></hover>")).appendNewline().appendNewline().append(mm.deserialize("<" + color + "><bold>" + type + ":</bold></" + color + ">")).appendNewline()
                            .appendNewline().decoration(TextDecoration.BOLD, false).decoration(TextDecoration.STRIKETHROUGH, false);
                    lines++;
                }

                // Add the objective to the page with hoverable details about points and progress
                page = page.append(mm.deserialize("<reset><black>" + "<hover:show_text:'<b><gold>Points Awarded: "
                        + obj.getWeight() + "</gold></b>'>- " + obj.getTargetName() + "</hover>"));
                if (obj.getAmount() > 1) {
                    page = page.append(mm.deserialize(" <hover:show_text:'<b><gold>Current Progress: " + obj.getCount(team) + "/" + obj.getAmount() + "</gold></b>'>(x" + obj.getAmount() + ")</hover>"));
                }
                page = page.appendNewline();

                // Increment the line count for the objective
                lines++;

                /* COMMENTED OUT: This adds a "bullet" point under each objective that says the points.
                   It was removed because you can now hover over objectives to see the points they award.
                   You can uncomment this to implement this feature. */
                //if (isWeighted) {
                    //page = page.append(mm.deserialize("<white>.</white>  - " + obj.getWeight() + " points")).appendNewline();
                    //lines++;
                //}
            }
            // Add the completed page to the list of pages
            pages.add(page);

            // Reset the line count for the next type
            lines = 1;
        }
        // TODO - Returning the value of lines even though it's always 1 is unnecessary, this can be optimized
        return lines;
    }

}
