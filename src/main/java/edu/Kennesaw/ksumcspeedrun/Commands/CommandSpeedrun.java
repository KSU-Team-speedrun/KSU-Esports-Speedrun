package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.*;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamSpawner;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Utilities.SpeedrunSuggestions;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The CommandSpeedrun class is responsible for handling the "/speedrun" command and its subcommands.
 * It retrieves necessary instances from the Main plugin class and utilizes them to perform various actions
 * related to speedrunning objectives within the game.
 */

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
        plugin.runAsyncTask(() -> {

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

                } else if (args[0].equalsIgnoreCase("objectives")) {

                    // Redirect player to regular /objectives command
                    if (sender instanceof Player p) {

                        // Commands cannot be dispatched asyncronously
                        Bukkit.getScheduler().runTask(plugin, () -> {

                            if (args.length == 2) {
                                p.performCommand("objectives " + args[1]);
                            } else {
                                p.performCommand("objectives");
                            }
                        });

                    } else {
                        // Console senders cannot be redirected
                        sender.sendMessage(Component.text("This command cannot be run from the console."));
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

                } else if (args[0].equalsIgnoreCase("remObjective")) {

                    // Argument length must be 2
                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun remobjective [index]"));

                    } else {

                        try {

                            // Syncronously run the following:
                            Bukkit.getScheduler().runTask(plugin, () -> {

                                /* Objectives start at 0, but for readability we increment them to start at 1 on the
                                   front end. */
                                int objectiveNum = Integer.parseInt(args[1]) - 1;
                                ObjectiveManager om = speedRun.getObjectives();

                                // Ensure the number provided exists in the list
                                if (objectiveNum > om.getLength()) {
                                    sender.sendMessage(plugin.getMessages().getOutOfBounds(args[1], "the objective list"));
                                    return;
                                }

                                // Get the objective that corresponds to the number in the argument & remove it
                                Objective objective = om.getObjectives().get(objectiveNum);
                                String objectiveType = objective.getType().name();
                                String target = objective.getTargetName();
                                speedRun.remObjective(objectiveNum);
                                sender.sendMessage(plugin.getMessages().getObjectiveRemoved(objectiveType, target));

                            });

                            // The argument must be a number
                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "number"));
                        }

                    }

                } else if (args[0].equalsIgnoreCase("setseed")) {

                    // Argument length must be 2
                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setseed [seed]"));

                    } else {

                        // The seed cannot be set if the speedrun has already started.
                        if (!speedRun.isStarted()) {

                            // Set the seed attribute in the speedrun instance
                            speedRun.setSeed(args[1]);

                            // If a world has already been generated, regenerate the world with the correct seed
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (speedRun.getSpeedrunWorld() != null) {
                                    speedRun.generateWorld(sender);
                                }
                            });

                        } else {
                            sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                            return;
                        }
                        sender.sendMessage(plugin.getMessages().getSeedSet(args[1]));
                    }

                } else if (args[0].equalsIgnoreCase("getseed")) {

                    // Return the current seed in that is set
                    sender.sendMessage(plugin.getMessages().getSeed(speedRun.getSeed()));

                } else if (args[0].equalsIgnoreCase("setborder")) {

                    // Argument length must be 2
                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setborder [border]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            // The argument provided must be a number
                            try {

                                // If the argument is a number, set the border to the argument
                                int size = Integer.parseInt(arguments);
                                speedRun.setBorder(size);
                                sender.sendMessage(plugin.getMessages().getWorldBorderSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getborder")) {

                    // Return the current world border that is set
                    sender.sendMessage(plugin.getMessages().getWorldBorder(speedRun.getBorder() + ""));

                } else if (args[0].equalsIgnoreCase("setteamsize")) {

                    // Argument length must be 2
                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setteamsize [number]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            // The argument provided must be a number
                            try {
                                int size = Integer.parseInt(arguments);

                                // Team size cannot change if the speedrun has started
                                if (!speedRun.isStarted()) {

                                    // If the game is not yet started, change the team size limit
                                    speedRun.setTeamSizeLimit(size);
                                } else {
                                    sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                                    return;
                                }
                                sender.sendMessage(plugin.getMessages().getTeamSizeLimitSet(arguments));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getteamsize")) {

                    // Return the current team size that is set
                    sender.sendMessage(plugin.getMessages().getTeamSizeLimit(speedRun.getTeamSizeLimit() + ""));

                } else if (args[0].equalsIgnoreCase("settimelimit")) {

                    // The argument length must be 2
                    if (args.length != 2) {
                        sender.sendMessage(plugin.getMessages()
                                .getInvalidArguments("/speedrun settimelimit [timeInMinutes]"));
                    } else {

                        // The argument provided must be a number
                        try {

                            // If the argument is a number, set the time limit attribute to the argument
                            Bukkit.getScheduler().runTask(plugin, () -> speedRun.setTimeLimit(Integer.parseInt(args[1])));
                            sender.sendMessage(plugin.getMessages().getTimeLimitSet(args[1]));

                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "number"));
                        }
                    }

                    // Return the set time limit
                } else if (args[0].equalsIgnoreCase("gettimelimit")) {

                    sender.sendMessage(plugin.getMessages().getTimeLimit(speedRun.getTimeLimit() + ""));

                } else if (args[0].equalsIgnoreCase("setspawnradius")) {

                    if (args.length != 2) {

                        sender.sendMessage(plugin.getMessages().getInvalidArguments("/speedrun setspawnradius [number]"));

                    } else {

                        Bukkit.getScheduler().runTask(plugin, () -> {

                            final String arguments = args[1];

                            // The argument provided must be a number
                            try {

                                int size = Integer.parseInt(arguments);

                                // The command cannot run if the speedrun is started
                                if (!speedRun.isStarted()) {

                                    // Change the spawn radius attribute
                                    speedRun.setSpawnRadius(size);

                                    /* If the world has already been generated, then the team spawn locations
                                       must also be relocated. */
                                    if (speedRun.getSpeedrunWorld() != null) {

                                        sender.sendMessage(plugin.getMessages().getSpawnsGenerating());

                                        // Locate new team spawn locations
                                        TeamSpawner.getTeamSpawnLocations(plugin).thenAccept(locations -> {

                                            // Set new team spawn locations
                                            speedRun.setTeamSpawnLocations(locations);

                                            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(
                                                    plugin.getMessages().getSpawnsGenerated())
                                            );
                                        });
                                    }

                                } else {
                                    sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                                    return;
                                }

                                sender.sendMessage(plugin.getMessages().getSpawnRadiusSet(arguments));

                            } catch (NumberFormatException e) {
                                sender.sendMessage(plugin.getMessages().getIllegalArguments(arguments, "number"));
                            }

                        });

                    }

                } else if (args[0].equalsIgnoreCase("getspawnradius")) {

                    sender.sendMessage(plugin.getMessages().getSpawnRadius(speedRun.getSpawnRadius() + ""));

                } else if (args[0].equalsIgnoreCase("setpointlimit")) {

                    // Arg length must be 2 - updates the point limit required to win the game
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

                    Bukkit.getScheduler().runTask(plugin, () -> {

                        // If the speedrun has not yet started, all speedrun attributes are reset
                        // The generated world is also reset: team points are NOT reset (bug)
                        // Recommended to restart the server between speedruns instead of using this command
                        if (!speedRun.isStarted()) {
                            speedRun.resetAttributes();
                        } else {
                            sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                            return;
                        }
                        sender.sendMessage(plugin.getMessages().getResetAttributes());
                    });

                    /* The game does not include admins in the game by default, this command allows them
                       to participate */
                } else if (args[0].equalsIgnoreCase("participate")) {

                    if (sender instanceof Player p) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (!speedRun.isStarted()) {
                                sender.sendMessage(plugin.getMessages().getParticipationSet(speedRun.participate(p)));
                            } else {
                                sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                            }
                        });
                    }

                    // If the game has not already started and there is at least one team, start the game
                } else if (args[0].equalsIgnoreCase("start")) {

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            Boolean isStarted = speedRun.setStarted(sender);
                            if (isStarted == null) {
                                sender.sendMessage(plugin.getMessages().getGameAlreadyStarted());
                            } else if (isStarted) {
                                sender.sendMessage(plugin.getMessages().getGameStarted());
                            }
                        } catch (NoSuchElementException e) {
                            sender.sendMessage(plugin.getMessages().getCannotStartGame());
                            plugin.getLogger().warning("A user attempted to start the game while all teams are empty.");
                        }
                    });

                    // Force stop the game
                } else if (args[0].equalsIgnoreCase("stop")) {

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (speedRun.isStarted()) {
                            speedRun.endGame();
                        } else {
                            sender.sendMessage(plugin.getMessages().getGameNotStarted());
                        }
                    });

                    // Debug command - "/speedrun test (args)" virtually updates the amount of players that are on the
                    // server for purposes of testing team inventory UI
                } else if (args[0].equalsIgnoreCase("test")) {

                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.createTeams(Integer.parseInt(args[1])));

                    // Toggle teams as either enabled (default) or disabled
                } else if (args[0].equalsIgnoreCase("toggleTeams")) {

                    // Cannot toggle teams if the game is started
                    if (speedRun.isStarted()) {
                        sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                        return;
                    }

                    sender.sendMessage(plugin.getMessages().getToggleTeams(!speedRun.getTeamsEnabled()));
                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.setTeamsEnabled(!speedRun.getTeamsEnabled()));


                    /* Return a list of all team spawn locations alongside a number - this returns the spawn locations
                       of all possible teams, not just the currently active teams */
                } else if (args[0].equalsIgnoreCase("getTeamSpawnLocations")) {

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        List<Location> locations = speedRun.getTeamSpawnLocations();

                        // Cannot get team locations if the world is not generated
                        if (locations.isEmpty()) {
                            sender.sendMessage(plugin.getMessages().getWorldNotGenerated());
                        } else {
                            sender.sendMessage(plugin.getMessages().getTeamSpawnLocations());
                            for (Location loc : locations) {
                                sender.sendMessage(plugin.getMessages().getTeamSpawnLocation(sender.getName(), loc, locations.indexOf(loc) + 1));
                            }
                        }

                    });

                    // Update the spawn location of a team (corresponds to the number returned in getTeamSpawnLocations)
                } else if (args[0].equalsIgnoreCase("setTeamSpawnLocation")) {

                    // Sender must be a player (this should probably be updated)
                    if (sender instanceof Player p) {

                        // Argument length must be 2
                        if (args.length != 2) {
                            Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(plugin
                                    .getMessages().getInvalidArguments("/speedrun setTeamSpawnLocation [teamNumber]"))
                            );

                        } else {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                if (speedRun.getTeamSpawnLocations().isEmpty()) {
                                    sender.sendMessage(plugin.getMessages().getWorldNotGenerated());
                                } else {
                                    try {

                                        // Get the team that corresponds to the argument number
                                        int arg = Integer.parseInt(args[1]);
                                        try {
                                            if (speedRun.isStarted()) {

                                                // Teams go from 0 to x, but are displayed as 1 to (x+1)
                                                Team team = speedRun.getTeamFromSpawnLocationIndex(arg - 1);
                                                if (team != null) {
                                                    team.setRespawnLocation(p.getLocation());
                                                }

                                            }
                                            speedRun.setTeamSpawnLocation(arg - 1, p.getLocation());
                                            sender.sendMessage(plugin.getMessages().getTeamSpawnSet(arg));

                                        } catch (IndexOutOfBoundsException e) {
                                            sender.sendMessage(plugin.getMessages().getOutOfBounds(args[1], "teams"));
                                        }
                                    } catch (NumberFormatException e) {
                                        sender.sendMessage(plugin.getMessages().getIllegalArguments(args[1], "number"));
                                    }
                                }
                            });
                        }
                    }

                    // Delete the speedrun world if it is generated
                } else if (args[0].equalsIgnoreCase("deleteWorld")) {

                    // Cannot run this command if the game is started
                    if (speedRun.isStarted()) {
                        sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                        return;
                    }

                    sender.sendMessage(plugin.getMessages().getWorldDeleted());
                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.deleteSpeedrunWorld());

                    // Generate the speedrun world according to the attributes set
                } else if (args[0].equalsIgnoreCase("generateWorld")) {

                    // World cannot be regenerated if a game is already started
                    if (speedRun.isStarted()) {
                        sender.sendMessage(plugin.getMessages().getGameStartedCannotChange());
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> speedRun.generateWorld(sender));

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
            Optional<Integer> amount = parseAmountFlag(args);

            KillObjective ko = weight.map(w -> amount.map(a -> {
                /* If both the weight and amount flags are included in the arguments, we attempt to assign ko to a new
                   KillObjective with the preassigned EntityType as a target and specified weight and amount.
                   If the EntityType is not-living (e.g., EXPERIENCE_ORB), an exception will be thrown, as non-living
                   entities cannot be killed. */
                try {
                    KillObjective newKo = new KillObjective(e, w, a, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPointsNumber("KILL", e.name(), a, w));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            }).orElseGet(() -> {
                /* If only the weight flag is included in the argument, we attempt to assign ko to a new KillObjective with
                   the preassigned EntityType as a target and specified weight. If the EntityType is not-living (e.g., EXPERIENCE_ORB),
                   an exception will be thrown, as non-living entities cannot be killed. */
                try {
                    KillObjective newKo = new KillObjective(e, w, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("KILL", e.name(), w));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            })).orElseGet(() -> amount.map(a -> {
                /* If only the amount flag is included in the argument, we assign ko to a new KillObjective with
                   the preassigned EntityType as a target and default weight of 1. If the EntityType is not-living,
                   an exception will be thrown. */
                try {
                    KillObjective newKo = new KillObjective(e, 1, a, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPointsNumber("KILL", e.name(), a, 1));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            }).orElseGet(() -> {
                /* If neither the weight nor amount flags are included in the argument, we attempt to assign ko to a
                   new KillObjective with only the preassigned EntityType as a target. If the EntityType is not-living,
                   an exception will be thrown, as non-living entities cannot be killed. */
                try {
                    KillObjective newKo = new KillObjective(e, plugin);
                    sender.sendMessage(plugin.getMessages().getObjectiveAdded("KILL", e.name()));
                    return newKo;
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(plugin.getMessages().getIllegalArguments(args[2], "living entity"));
                    return null;
                }
            }));


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
                Optional<Integer> amount = parseAmountFlag(args);

                MineObjective mo = weight.map(w -> amount.map(a -> {
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPointsNumber("MINE", m.name(), a, w));
                    return new MineObjective(m, w, a, plugin);
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPoints("MINE", m.name(), w));
                    return new MineObjective(m, w, plugin);
                })).orElseGet(() -> amount.map(a -> {
                    sender.sendMessage(plugin.getMessages().getObjectiveAddedPointsNumber("MINE", m.name(), a, 1));
                    return new MineObjective(m, 1, a, plugin);
                }).orElseGet(() -> {
                    sender.sendMessage(plugin.getMessages().getObjectiveAdded("MINE", m.name()));
                    return new MineObjective(m, plugin);
                }));

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
            suggestions.add("objectives");
            suggestions.add("getBorder");
            suggestions.add("getPointLimit");
            suggestions.add("generateWorld");
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
            suggestions.add("getTeamSpawnLocations");
            suggestions.add("setTeamSpawnLocation");
            suggestions.add("deleteWorld");

        // If arguments have been typed, the following logic runs:
        } else {

            /* If the sender starts to fill in characters for the first argument (args[0]), recommendations will be made
               based on what has already been typed, e.g., "/speedrun he" will suggest "help", "/speedrun addo" will suggest
               "addObjective", etc. */
            if (args.length == 1) {

                addMatchingSuggestions(suggestions, args[0], "help", "team", "reload", "addObjective",
                        "remObjective", "setTeamSize", "getTeamSize", "start", "stop", "objectives",
                        "setTimeLimit", "getTimeLimit", "setBorder", "getBorder", "participate", "deleteWorld",
                        "setSeed", "getSeed", "resetAttributes", "setSpawnRadius", "getSpawnRadius", "generateWorld",
                        "setPointLimit", "getPointLimit", "toggleTeams", "getTeamSpawnLocations", "setTeamSpawnLocation");


            /* The same continues for the second argument: If the first argument is addobjective, suggestions are made
               for the second argument: kill, enter, obtain, or mine */
            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("addobjective")) {
                    addMatchingSuggestions(suggestions, args[1], "kill", "enter", "mine", "obtain");
                } else if (args[0].equalsIgnoreCase("remobjective")) {
                    suggestions.add("[number]");
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
                } else if (args[0].equalsIgnoreCase("setteamspawnlocation")) {
                    suggestions.add("[teamNumber]");
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
