package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpeedrun implements BasicCommand {

    Main plugin;
    Speedrun speedRun;
    List<String> livingEntityNames;

    public CommandSpeedrun(Main plugin) {
        this.plugin = plugin;
        this.speedRun = plugin.getSpeedrun();
        livingEntityNames = SpeedrunSuggestions.getLivingEntities();
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {

        Component prefix = plugin.getSpeedrunConfig().getPrefix();

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

            CommandSender sender = commandSourceStack.getSender();

            if (args.length == 0) {
                // help list
            } else  {
                if (args[0].equalsIgnoreCase("help")) {
                    // help list
                } else if (args[0].equalsIgnoreCase("reload")) {
                    plugin.getSpeedrunConfig().load();
                    sender.sendMessage(prefix.append(Component.text("Plugin config has been reloaded.")));
                } else if (args[0].equalsIgnoreCase("addObjective")) {
                    if (args.length < 3 || args.length > 7) {
                        sender.sendMessage(prefix.append(Component.text("Usage: /speedrun addObjective [event] [specifiction] <-flag(s)>")));
                    } else {
                        if (args[1].equalsIgnoreCase("kill")) {
                            EntityType entity;
                            try {
                                entity = EntityType.valueOf(args[2]);
                                if (args.length == 3) {
                                    speedRun.addObjective(new KillObjective(entity));
                                    sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + entity.name())));
                                } else if (args.length == 5) {
                                    if (args[3].equalsIgnoreCase("-w")) {
                                        try {
                                            int weight = Integer.parseInt(args[4]);
                                            speedRun.addObjective(new KillObjective(entity, weight));
                                            sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + entity.name() + " (" + weight + ")")));
                                        } catch (NumberFormatException e) {
                                            sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[4] + "\" is not a valid number.")));
                                        }
                                    } else {
                                        sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[3] + "\" is not a valid flag.")));
                                        sender.sendMessage(prefix.append(Component.text("Valid Flags: '-w' - Set an Objective's weight.")));
                                    }
                                } else {
                                    sender.sendMessage(prefix.append(Component.text("Usage: /speedrun addObjective [event] [specifiction] <-flag(s)>")));
                                }
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a valid entity type.")));
                                sender.sendMessage(prefix.append(Component.text("For a list of possible entity types, type: /speedrun entitytypes")));
                            } catch (NonLivingEntityException e) {
                                sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a living entity.")));
                                sender.sendMessage(prefix.append(Component.text("For a list of possible entity types, type: /speedrun entitytypes")));
                            }
                        } else if (args[1].equalsIgnoreCase("enter")) {

                        } else if (args[1].equalsIgnoreCase("mine")) {

                        } else if (args[1].equalsIgnoreCase("obtain")) {

                        } else {
                            sender.sendMessage(prefix.append(Component.text("Invalid argument for [event]. Possible arguments: KILL, ENTER, MINE, OBTAIN")));
                        }
                    }
                } else if (args[0].equalsIgnoreCase("entitytypes")) {
                    StringBuilder entityTypes = new StringBuilder();
                    int livingEntities = 0;
                    for (String str : livingEntityNames) {
                        entityTypes.append(str);
                        livingEntities++;
                        if (livingEntities % 5 == 0) {
                            sender.sendMessage(entityTypes.toString());
                            entityTypes = new StringBuilder();
                        }
                    }
                }
            }

        });

    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            suggestions.add("help");
            suggestions.add("reload");
            suggestions.add("addobjective");
        } else {
            if (args.length == 1) {
                if ("help".startsWith(args[0])) {
                    suggestions.clear();
                    suggestions.add("help");
                }
                if ("reload".startsWith(args[0])) {
                    suggestions.clear();
                    suggestions.add("reload");
                }
                if ("addobjective".startsWith(args[0])) {
                    suggestions.clear();
                    suggestions.add("addobjective");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("addobjective")) {
                    if ("kill".startsWith(args[1])) {
                        suggestions.add("kill");
                    }
                    if ("enter".startsWith(args[1])) {
                        suggestions.add("enter");
                    }
                    if ("obtain".startsWith(args[1])) {
                        suggestions.add("obtain");
                    }
                    if ("mine".startsWith(args[1])) {
                        suggestions.add("mine");
                    }
                }
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("kill")) {
                    for (String str : livingEntityNames) {
                        if (str.toLowerCase().startsWith(args[2].toLowerCase())) {
                            suggestions.add(str);
                        }
                    }
                }
            } else if (args.length == 4) {
                suggestions.add("<-flag(s)>");
            }
        }
        return suggestions;
    }

    @Override
    public @Nullable String permission() {
        return "ksu.sr.user";
    }
}
