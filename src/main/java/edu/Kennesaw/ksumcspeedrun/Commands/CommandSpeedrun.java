package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.EnterObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.MineObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObtainObjective;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Utilities.SpeedrunSuggestions;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpeedrun implements BasicCommand {

    // Main plugin & speedrun instances
    Main plugin;
    Speedrun speedRun;

    // String lists from "SpeedrunSuggestions" used for subcommand suggestions
    List<String> livingEntityNames;
    List<String> locationNames;
    List<String> blockNames;
    List<String> itemNames;

    /* String list from "SRStructures" which retrieves all structure names in Minecraft, used to verify argument for
       structure in EnterObjective */
    List<String> structures;

    // Constructor: Takes instance of "Main" plugin class so that Speedrun & Config instances can be retrieved
    public CommandSpeedrun(Main plugin) {

        this.plugin = plugin;
        this.speedRun = plugin.getSpeedrun();

        // Assign suggestion list values from "SpeedrunSuggestions"
        livingEntityNames = SpeedrunSuggestions.getLivingEntities();
        locationNames = SpeedrunSuggestions.getLocationNames();
        blockNames = SpeedrunSuggestions.getBlockNames();
        itemNames = SpeedrunSuggestions.getItemNames();

        // Assign structures list values from "SRStructures"
        structures = SRStructure.getStructureNames();
    }

    /* Main overridden execute method that is called when the command "/speedrun" is executed, with any arguments that
       follow passed as String[] args: Uses ASync thread for computation */
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        /* The "prefix" component is assigned in the config and the value is filled when the command is executed
           any response message will start with the plugin prefix */
        Component prefix = plugin.getSpeedrunConfig().getPrefix();

        // Move to a different thread for the following logic
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

            CommandSender sender = commandSourceStack.getSender();
            // If argument lengths is 0 (i.e. "/speedrun" with no subcommand)
            if (args.length == 0) {

                // help list

            // If argument length is greater than 0
            } else  {

                // First argument is "help" (i.e. "/speedrun help")
                if (args[0].equalsIgnoreCase("help")) {
                    // help list

                // First argument is "reload" (i.e. "/speedrun reload")
                } else if (args[0].equalsIgnoreCase("reload")) {

                    // Call method "reloadConfig()" passing sender value & prefix component
                    reloadConfig(sender, prefix);

                 // First argument is "addObjective" (i.e. "/speedrun addObjective")
                } else if (args[0].equalsIgnoreCase("addObjective")) {

                    // Call method "addObjectiveHandler" passing sender value, args string array & prefix component
                    addObjectiveHandler(sender, args, prefix);

                } else if (args[0].equalsIgnoreCase("structures")) {

                }
            }
        });
    }

    // Method called upon subcommand "/speedrun reload"
    private void reloadConfig(CommandSender sender, Component prefix) {

        // Reload config file
        plugin.getSpeedrunConfig().load();

        // Send the sender a message w/ prefix appended to start (e.g., "[Prefix] <message>")
        sender.sendMessage(prefix.append(Component.text("Plugin config has been reloaded.")));

    }

    // Method called upon subcommand "/speedrun addObjective"
    private void addObjectiveHandler(CommandSender sender, String[] args, Component prefix) {

        // Argument length must be 3 (no flags), 5 (1 flag), or 7 (2 flags)
        if (args.length == 3 || args.length == 5 || args.length == 7) {

            // String objectiveType is args[1] (the second argument)
            String objectiveType = args[1].toLowerCase();

            // String objectiveType must be either "kill", "enter", "mine", or "obtain"
            switch (objectiveType) {

                // Individual methods called based on case of args[1] (the second argument)
                case "kill":
                    killObjectiveHandler(sender, args, prefix);
                    break;
                case "enter":
                    enterObjectiveHandler(sender, args, prefix);
                    break;
                case "mine":
                    mineObjectiveHandler(sender, args, prefix);
                    break;
                case "obtain":
                    obtainObjectiveHandler(sender, args, prefix);
                    break;
                default:
                    sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[1] + "\" is not a valid event.")));
                    break;
            }
        } else {
            sender.sendMessage(prefix.append(Component.text("Usage: /speedrun addObjective [event] [specifiction] <-flag(s)>")));
        }
    }

    // Method called upon subcommand "/speedrun addObjective kill"
    private void killObjectiveHandler(CommandSender sender, String[] args, Component prefix) {

        try {

            /* Attempt to get EntityType from args[2] (third argument)
               e.g., /speedrun addObjective kill ENDER_DRAGON, catch IllegalArgumentException if fail */
            EntityType e = EntityType.valueOf(args[2].toUpperCase());

            /* This allows us to consider the optional weight flag if it is included in the argument
               e.g., /speedrun addObjective kill ENDER_DRAGON -w 10 */
            Optional<Integer> weight = parseWeightFlag(args);

            KillObjective ko = weight.map(w -> {

                /* If the weight flag is included in the argument, we attempt to assign ko to a new KillObjective with
                   the preassigned EntityType as a target. If the EntityType is not-living (e.g., EXPERIENCE_ORB), an
                   exception will be thrown, as non-living entities cannot be killed. */
                try {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + e.name() + " (" + w + ")")));
                    return new KillObjective(e, w, plugin);
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a living entity.")));
                    return null;
                }
            }).orElseGet(() -> {

                // Same as above, but the weight flag was not included in the sender's arguments.
                try {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + e.name())));
                    return new KillObjective(e, plugin);
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a living entity.")));
                    return null;
                }
            });

            // Add the returned kill objective to the objectives in the Speedrun instance
            speedRun.addObjective(ko);

        // Catch Illegal Argument if args[2] (third argument) is not an EntityType
        } catch (IllegalArgumentException e) {
            sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a valid entity type.")));
        }
    }

    // Method called upon subcommand "/speedrun addObjective enter"
    private void enterObjectiveHandler(CommandSender sender, String[] args, Component prefix) {

        // Target initially defined as Object type because it can be either a Biome, an SRStructure, or a Portal
        Object object = null;

        // Defining this string specifically so we don't have to call toUpperCase() method multiple times
        String arg2UpperCase = args[2].toUpperCase();

        try {

            /* First, we check of args[2] (third argument) is a Biome type:
               e.g., "/speedrun addObjective enter BAMBOO_JUNGLE */
            object = Biome.valueOf(arg2UpperCase);

        } catch (IllegalArgumentException e) {

            /* If the third argument is not a Biome type, we check if it is a structure:
               e.g., "/speedrun addObjective enter BASTION_REMNANT" */
            if (structures.contains(arg2UpperCase)) {

                /* We can initialize the SRStructure which can return type org.bukkit.generator.structure.Structure
                   from a matching String argument */
                object = new SRStructure(plugin, arg2UpperCase);

            } else {

                /* If the third argument is not a Biome type or Structure type, we can check if it is a Portal
                   e.g., "/speedrun addObjective enter WORLD_TO_NETHER" */
                if (Portal.getPortalTypeNames().contains(arg2UpperCase)) {
                    object = new Portal(Portal.PortalType.valueOf(arg2UpperCase));
                }

            }
        }

        // Object finalObject is either of type Biome, SRStructure, Portal, or null
        final Object finalObject = object;

        // Optional Integer weight flag, same as explained in killObjectiveHandler
        Optional<Integer> weight = parseWeightFlag(args);

        EnterObjective eo = weight.map(w -> {

            // An exception will be thrown if the finalObject is not of type Biome, SRStructure, or Portal
            try {
                sender.sendMessage(prefix.append(Component.text("Objective Added: ENTER " + arg2UpperCase + " (" + w + ")")));
                return new EnterObjective(finalObject, w, plugin);
            } catch (InvalidTargetLocationException e) {
                sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + arg2UpperCase + "\" is not a valid biome, structure, or portal type.")));
                return null;
            }
        }).orElseGet(() -> {
            try {
                sender.sendMessage(prefix.append(Component.text("Objective Added: ENTER " + arg2UpperCase)));
                return new EnterObjective(finalObject, plugin);
            } catch (InvalidTargetLocationException e) {
                sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + arg2UpperCase + "\" is not a valid biome, structure, or portal type.")));
                return null;
            }
        });

        // Add the returned EnterObjective to the objectives in the Speedrun instance
        if (eo != null) speedRun.addObjective(eo);
    }

    // Method called upon subcommand "/speedrun addObjective mine"
    private void mineObjectiveHandler(CommandSender sender, String[] args, Component prefix) {

        /* Try to assign material from third argument String value
           e.g., "/speedrun addObjective mine SANDSTONE" */
        Material m = Material.getMaterial(args[2].toUpperCase());

        // Ensure that the third argument is a valid material type
        if (m != null) {

            /* Ensure that the third argument material is a valid block type
               e.g., this will not work: /speedrun addobjective mine STICK */
            if (m.isBlock()) {

                // Optional weight integer flag, same as explained above. No try/catch needed here.
                Optional<Integer> weight = parseWeightFlag(args);
                MineObjective mo = weight.map(w -> {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: MINE " + m.name() + " (" + w + ")")));
                    return new MineObjective(m, w, plugin);
                }).orElseGet(() -> {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: MINE " + m.name())));
                    return new MineObjective(m, plugin);
                });

                // Add the returned objective to the objective list in Speedrun instance
                speedRun.addObjective(mo);

            } else {

                // If material is not a block:
                sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a valid block.")));

            }
        } else {

            // If string is not a valid material:
            sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a valid material.")));

        }
    }

    // Method called upon subcommand "/speedrun addObjective obtain"
    private void obtainObjectiveHandler(CommandSender sender, String[] args, Component prefix) {

        /* Similar to mineObjectiveHandler, we ensure the third argument is a valid material type.
           We do not, however, need to ensure it is a valid block type. */
        Material m = Material.getMaterial(args[2].toUpperCase());

        if (m != null) {

            /* This time we have two optional flags, weight (same as before), and amount, which defines the number of
               items needed before the objective is complete, e.g., "/speedrun addobjective obtain DIAMOND -n 10"
               will require 10 diamonds to be obtained before the objective is completed. Flags can be added in any
               order and are both optional, either or can be included, neither, or both. */
            Optional<Integer> weight = parseWeightFlag(args);
            Optional<Integer> amount = parseAmountFlag(args);

            ObtainObjective oo = weight.map(w -> amount.map(a -> {
                sender.sendMessage(prefix.append(Component.text("Objective Added: OBTAIN " + a + " " + m.name() + " (" + w + ")")));
                return new ObtainObjective(m, w, a, plugin);
            }).orElseGet(() -> {
                sender.sendMessage(prefix.append(Component.text("Objective Added: OBTAIN " + m.name() + " (" + w + ")")));
                return new ObtainObjective(m, w, plugin);
            })).orElseGet(() -> amount.map(a -> {
                sender.sendMessage(prefix.append(Component.text("Objective Added: OBTAIN " + a + " " + m.name())));
                return new ObtainObjective(m, a, plugin);
            }).orElseGet(() ->{
                sender.sendMessage(prefix.append(Component.text("Objective Added: OBTAIN " + m.name())));
                return new ObtainObjective(m, plugin);
            }));
        }
    }

    // Method for parsing the weight flag
    private Optional<Integer> parseWeightFlag(String[] args) {
        for (int i = 3; i < args.length - 1; i++) {
            /* If any argument after the third argument is -w, the following argument will be returned
               if it is an integer. In any other case the optional will be empty.  */
            if (args[i].equalsIgnoreCase("-w")) {
                try {
                    return Optional.of(Integer.parseInt(args[i+1]));
                } catch(NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    // Method for parsing amount flag, exact same logic as parsing weight flag.
    private Optional<Integer> parseAmountFlag(String[] args) {
        for (int i = 3; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase("-n")) {
                try {
                    return Optional.of(Integer.parseInt(args[i+1]));
                } catch(NumberFormatException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /* Main overridden suggest method that is called when the command "/speedrun" is typed but not sent, with any
       arguments that follow passed as String[] args. We use these arguments to make suggestions. */
    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        // Empty suggestion array list
        List<String> suggestions = new ArrayList<>();

        // If no arguments have been typed, main subcommands are suggested.
        if (args.length == 0) {

            suggestions.add("help");
            suggestions.add("reload");
            suggestions.add("addobjective");

        // If arguments have been typed, the following logic runs:
        } else {

            /* If the sender starts to fill in characters for the first argument (args[0]), recommendations will be made
               based on what has already been typed, e.g., "/speedrun he" will suggest "help", "/speedrun addo" will suggest
               "addObjective", etc. */
            if (args.length == 1) {

                addMatchingSuggestions(suggestions, args[0], "help", "reload", "addobjective");

            /* The same continues for the second argument: If the first argument is addobjective, suggestions are made
               for the second argument: kill, enter, obtain, or mine */
            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("addobjective")) {
                    addMatchingSuggestions(suggestions, args[1], "kill", "enter", "mine", "obtain");
                }

            // Continues w/ third argument, just one step deeper
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("addobjective")) {

                    /* If first argument is addObjective & second argument is kill, then all living entity names which
                       start with the third argument will be suggested, e.g., if the command typed is
                       "/speedrun addobjective kill b", then the following will be suggested: bat, bee, blaze, bogged,
                       breeze */
                    if (args[1].equalsIgnoreCase("kill")) {

                        for (String str : livingEntityNames) {
                            if (str.toLowerCase().startsWith(args[2].toLowerCase())) {
                                suggestions.add(str);
                            }
                        }
                    // Same as kill but with location names
                    } else if (args[1].equalsIgnoreCase("enter")) {

                        for (String str : locationNames) {
                            if (str.toLowerCase().startsWith(args[2].toLowerCase())) {
                                suggestions.add(str);
                            }
                        }

                    // ... etc
                    } else if (args[1].equalsIgnoreCase("mine")) {

                        for (String str : blockNames) {
                            if (str.toLowerCase().startsWith(args[2].toLowerCase())) {
                                suggestions.add(str);
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("obtain")) {
                        for (String str : itemNames) {
                            if (str.toLowerCase().startsWith(args[2].toLowerCase())) {
                                suggestions.add(str);
                            }
                        }
                    }
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("addobjective")) {
                    suggestions.add("<-flag(s)>");
                }
            }
        }
        return suggestions;
    }

    // This method makes it easier to add multiple possible suggestions to a list suggestion list & match them
    private void addMatchingSuggestions(List<String> suggestions, String arg, String... possibleSuggestions) {
        for (String suggestion : possibleSuggestions) {
            if (suggestion.startsWith(arg.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
    }

    /* This defines the default permission node for this command. Further permissions will need to be defined for admin
       commands */
    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.user";
    }
}
