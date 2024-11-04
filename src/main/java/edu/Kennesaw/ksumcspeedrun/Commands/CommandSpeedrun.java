package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.*;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"UnstableApiUsage", "SpellCheckingInspection", "GrammarInspection"})
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
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        final CommandSender sender = commandSourceStack.getSender();

        // Move to a different thread for the following logic
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

            // If argument lengths is 0 (i.e. "/speedrun" with no subcommand)
            if (args.length == 0) {

                // Redirect player to regular /help command
                if (sender instanceof Player p) {

                    // Commands cannot be dispatched asyncronously
                    Bukkit.getScheduler().runTask(plugin, () -> p.performCommand("help"));

                } else {
                    // Console senders cannot be redirected
                    sender.sendMessage(Component.text("Please use /help to see the help list."));
                }

            // If argument length is greater than 0
            } else {

                // First argument is "help" (i.e. "/speedrun help")
                if (args[0].equalsIgnoreCase("help")) {

                    // Redirect player to regular /help command
                    if (sender instanceof Player p) {

                        // Commands cannot be dispatched asyncronously
                        Bukkit.getScheduler().runTask(plugin, () -> {

                            if (args.length == 2) {
                                p.performCommand("help " + args[1]);
                            } else {
                                p.performCommand("help");
                            }
                        });

                    } else {
                        // Console senders cannot be redirected
                        sender.sendMessage(Component.text("Please use /help to see the help list."));
                    }

                } else if (args[0].equalsIgnoreCase("team")) {

                    if (sender instanceof Player p) {

                        // Commands cannot be dispatched asyncronously
                        Bukkit.getScheduler().runTask(plugin, () -> {

                            if (args.length == 2) {
                                p.performCommand("team " + args[1]);
                            } else {
                                p.performCommand("team");
                            }

                        });

                    } else {
                        // Console senders cannot join teams
                        sender.sendMessage(Component.text("Error: Only players may join teams."));
                    }

                } else if (args[0].equalsIgnoreCase("addObjective")) {

                    // Call method "addObjectiveHandler" passing sender value, args string array
                    addObjectiveHandler(sender, args);

                } else if (args[0].equalsIgnoreCase("remobjective")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun remobjective [index]"));

                    } else {

                        try {

                            Bukkit.getScheduler().runTask(plugin, () -> {

                                int objectiveNum = Integer.parseInt(args[1]) - 1;
                                ObjectiveManager om = speedRun.getObjectives();

                                if (objectiveNum > om.getLength()) {
                                    sender.sendMessage(plugin.getMessages().getOutOfBounds(args[1], "the objective list"));
                                    return;
                                }
                                Objective objective = om.getObjectives().get(objectiveNum);
                                String objectiveType = objective.getType().name();
                                String target = objective.getTargetName();
                                om.removeObjective(objectiveNum);
                                sender.sendMessage(plugin.getMessages().getObjectiveRemoved(objectiveType, target));

                            });

                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "number"));
                        }

                    }
                } else if (args[0].equalsIgnoreCase("setseed")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setseed [seed]"));

                    } else {
                        speedRun.setSeed(args[1]);
                        sender.sendMessage(plugin.getMessages().getSeedSet(args[1]));
                    }

                } else if (args[0].equalsIgnoreCase("getseed")) {

                    sender.sendMessage(plugin.getMessages().getSeed(speedRun.getSeed()));

                } else if (args[0].equalsIgnoreCase("setborder")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setborder [border]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            try {
                                int size = Integer.parseInt(arguments);
                                speedRun.setBorder(size);
                                sender.sendMessage(plugin.getMessages().getWorldBorderSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getborder")) {

                    sender.sendMessage(plugin.getMessages().getWorldBorder(speedRun.getBorder() + ""));

                } else if (args[0].equalsIgnoreCase("setteamsize")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setteamsize [number]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            try {
                                int size = Integer.parseInt(arguments);
                                speedRun.setTeamSizeLimit(size);
                                sender.sendMessage(plugin.getMessages().getTeamSizeLimitSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getteamsize")) {

                    sender.sendMessage(plugin.getMessages().getTeamSizeLimit(speedRun.getTeamSizeLimit() + ""));

                } else if (args[0].equalsIgnoreCase("settimelimit")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages()
                                .getInvalidArguments("/speedrun settimelimit [timeInMinutes]"));

                    } else {

                        try {

                            speedRun.setTimeLimit(Integer.parseInt(args[1]));
                            sender.sendMessage(plugin.getMessages().getTimeLimitSet(args[1]));

                        } catch (NumberFormatException e) {

                            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "number"));

                        }
                    }

                } else if (args[0].equalsIgnoreCase("gettimelimit")) {

                    sender.sendMessage(plugin.getMessages().getTimeLimit(speedRun.getTimeLimit() + ""));

                } else if (args[0].equalsIgnoreCase("setspawnradius")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setspawnradius [number]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            try {
                                int size = Integer.parseInt(arguments);
                                speedRun.setSpawnRadius(size);
                                sender.sendMessage(plugin.getMessages().getSpawnRadiusSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getspawnradius")) {

                    sender.sendMessage(plugin.getMessages().getSpawnRadius(speedRun.getSpawnRadius() + ""));

                } else if (args[0].equalsIgnoreCase("setpointlimit")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setpointlimit [number]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            try {
                                int size = Integer.parseInt(arguments);
                                speedRun.setTotalWeight(size);
                                sender.sendMessage(plugin.getMessages().getPointLimitSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getpointlimit")) {

                    sender.sendMessage(plugin.getMessages().getPointLimit(speedRun.getTotalWeight() + ""));

                } else if (args[0].equalsIgnoreCase("resetattributes")) {

                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.resetAttributes());

                    sender.sendMessage(plugin.getMessages().getResetAttributes());

                } else if (args[0].equalsIgnoreCase("participate")) {

                    if (sender instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                sender.sendMessage(plugin.getMessages().getParticipationSet(speedRun.participate(p))));

                    }

                } else if (args[0].equalsIgnoreCase("start")) {

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Boolean isStarted = speedRun.setStarted(sender);
                        if (isStarted == null) {
                            sender.sendMessage(plugin.getMessages().getGameAlreadyStarted());
                        } else if (isStarted) {
                            sender.sendMessage(plugin.getMessages().getGameStarted());
                        } else {
                            sender.sendMessage(plugin.getMessages().getWorldGenerated());
                        }
                    });

                } else if (args[0].equalsIgnoreCase("stop")) {

                    speedRun.endGame();

                } else if (args[0].equalsIgnoreCase("test")) {

                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.createTeams(Integer.parseInt(args[1])));

                } else if (args[0].equalsIgnoreCase("toggleTeams")) {

                    sender.sendMessage(plugin.getMessages().getToggleTeams(!speedRun.getTeamsEnabled()));

                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.setTeamsEnabled(!speedRun.getTeamsEnabled()));

                } else {

                    Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin.getMessages().getUnknownCommand(args[0])));

                }
            }
        });
    }

    // Method called upon subcommand "/speedrun addObjective"
    private void addObjectiveHandler(CommandSender sender, String[] args) {

        // Argument length must be 3 (no flags), 5 (1 flag), or 7 (2 flags)
        if (args.length == 3 || args.length == 5 || args.length == 7) {

            // String objectiveType is args[1] (the second argument)
            String objectiveType = args[1].toLowerCase();

            // String objectiveType must be either "kill", "enter", "mine", or "obtain"
            switch (objectiveType) {

                // Individual methods called based on case of args[1] (the second argument)
                case "kill":
                    killObjectiveHandler(sender, args);
                    break;
                case "enter":
                    enterObjectiveHandler(sender, args);
                    break;
                case "mine":
                    mineObjectiveHandler(sender, args);
                    break;
                case "obtain":
                    obtainObjectiveHandler(sender, args);
                    break;
                default:
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "valid event"));
                    break;
            }
        } else {
            sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun addObjective [event] [specifiction] <-flag(s)>"));
        }
    }

    // Method called upon subcommand "/speedrun addObjective kill"
    private void killObjectiveHandler(CommandSender sender, String[] args) {

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
                    KillObjective newKo = new KillObjective(e, w, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("KILL", e.name(), w));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            }).orElseGet(() -> {

                // Same as above, but the weight flag was not included in the sender's arguments.
                try {
                    KillObjective newKo = new KillObjective(e, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAdded("KILL", e.name()));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            });

            // Add the returned kill objective to the objectives in the Speedrun instance
            speedRun.addObjective(ko);

        // Catch Illegal Argument if args[2] (third argument) is not an EntityType
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "valid entity type"));
        }
    }

    // Method called upon subcommand "/speedrun addObjective enter"
    private void enterObjectiveHandler(CommandSender sender, String[] args) {

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
                    System.out.println("New Portal: " + ((Portal) object).portalType());
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
                EnterObjective newEo = new EnterObjective(finalObject, w, plugin);
                sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("ENTER", arg2UpperCase, w));
                return newEo;
            } catch (InvalidTargetLocationException e) {
                sender.sendMessage(plugin.getMessages().getIllegalArguments(arg2UpperCase, "valid biome," +
                        " structure, or portal type"));
                return null;
            }
        }).orElseGet(() -> {
            try {
                EnterObjective newEo = new EnterObjective(finalObject, plugin);
                sender.sendMessage(plugin.getMessages().getObjectiveAdded("ENTER", arg2UpperCase));
                return newEo;
            } catch (InvalidTargetLocationException e) {
                sender.sendMessage(plugin.getMessages().getIllegalArguments(arg2UpperCase, "valid biome," +
                        " structure, or portal type"));
                return null;
            }
        });

        // Add the returned EnterObjective to the objectives in the Speedrun instance
        if (eo != null) speedRun.addObjective(eo);
    }

    // Method called upon subcommand "/speedrun addObjective mine"
    private void mineObjectiveHandler(CommandSender sender, String[] args) {

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
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("MINE", m.name(), w));
                    return new MineObjective(m, w, plugin);
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.getMessages().getObjectiveAdded("MINE", m.name()));
                    return new MineObjective(m, plugin);
                });

                // Add the returned objective to the objective list in Speedrun instance
                speedRun.addObjective(mo);

            } else {

                // If material is not a block:
                sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2].toUpperCase(), "valid block"));

            }
        } else {

            // If string is not a valid material:
            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2].toUpperCase(), "valid material"));

        }
    }

    // Method called upon subcommand "/speedrun addObjective obtain"
    private void obtainObjectiveHandler(CommandSender sender, String[] args) {

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
                sender.sendMessage(plugin.getMessages().getObjectiveAddedPointsNumber("OBTAIN",
                        m.name(), a, w));
                return new ObtainObjective(m, w, a, plugin);
            }).orElseGet(() -> {
                sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("OBTAIN",
                        m.name(), w));
                return new ObtainObjective(m, w, plugin);
            })).orElseGet(() -> amount.map(a -> {
                sender.sendMessage(plugin.getMessages().getObjectiveAddedNumber("OBTAIN",
                        m.name(), a));
                return new ObtainObjective(m, 1, a, plugin);
            }).orElseGet(() ->{
                sender.sendMessage(plugin.getMessages().getObjectiveAdded("OBTAIN",
                        m.name()));
                return new ObtainObjective(m, plugin);
            }));

            speedRun.addObjective(oo);

        } else {

            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2].toUpperCase(), "valid material"));

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
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String @NotNull [] args) {

        // Empty suggestion array list
        List<String> suggestions = new ArrayList<>();

        // If no arguments have been typed, main subcommands are suggested.
        if (args.length == 0) {

            suggestions.add("addObjective");
            suggestions.add("getBorder");
            suggestions.add("getPointLimit");
            suggestions.add("getSeed");
            suggestions.add("getSpawnRadius");
            suggestions.add("getTeamSize");
            suggestions.add("getTimeLimit");
            suggestions.add("help");
            suggestions.add("participate");
            suggestions.add("team");
            suggestions.add("reload");
            suggestions.add("remObjective");
            suggestions.add("resetAttributes");
            suggestions.add("setBorder");
            suggestions.add("setPointLimit");
            suggestions.add("setSeed");
            suggestions.add("setSpawnRadius");
            suggestions.add("setTeamSize");
            suggestions.add("setTimeLimit");
            suggestions.add("start");
            suggestions.add("stop");
            suggestions.add("toggleTeams");

        // If arguments have been typed, the following logic runs:
        } else {

            /* If the sender starts to fill in characters for the first argument (args[0]), recommendations will be made
               based on what has already been typed, e.g., "/speedrun he" will suggest "help", "/speedrun addo" will suggest
               "addObjective", etc. */
            if (args.length == 1) {

                addMatchingSuggestions(suggestions, args[0], "help", "team", "reload", "addObjective",
                        "remObjective", "setTeamSize", "getTeamSize", "start", "stop",
                        "setTimeLimit", "getTimeLimit", "setBorder", "getBorder", "participate",
                        "setSeed", "getSeed", "resetAttributes", "setSpawnRadius", "getSpawnRadius",
                        "setPointLimit", "getPointLimit", "toggleTeams");


            /* The same continues for the second argument: If the first argument is addobjective, suggestions are made
               for the second argument: kill, enter, obtain, or mine */
            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("addobjective")) {
                    addMatchingSuggestions(suggestions, args[1], "kill", "enter", "mine", "obtain");
                } else if (args[0].equalsIgnoreCase("remobjective")) {
                    suggestions.add("[number");
                } else if (args[0].equalsIgnoreCase("setteamsize")) {
                    suggestions.add("[number]");
                } else if (args[0].equalsIgnoreCase("settimelimit")) {
                    suggestions.add("[numberInMinutes]");
                } else if (args[0].equalsIgnoreCase("setborder")) {
                    suggestions.add("[radius]");
                } else if (args[0].equalsIgnoreCase("setseed")) {
                    suggestions.add("[seed]");
                } else if (args[0].equalsIgnoreCase("setspawnradius")) {
                    suggestions.add("[radius]");
                } else if (args[0].equalsIgnoreCase("setpointlimit")) {
                    suggestions.add("[number]");
                } else if (args[0].equalsIgnoreCase("team")) {
                    addMatchingSuggestions(suggestions, args[1], speedRun.getTeams().getStrippedTeamNames(true));
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
            if (suggestion.toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
    }

    // Same as above, but takes different args
    private void addMatchingSuggestions(List<String> suggestions, String arg, List<String> teamNames) {
        for (String suggestion : teamNames) {
            if (suggestion.toLowerCase().startsWith(arg.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
    }

    /* This defines the default permission node for this command. Further permissions will need to be defined for admin
       commands */
    @Override
    public @Nullable String permission() {
        return "ksu.speedrun.admin";
    }
}
