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
import java.util.Optional;

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
                    reloadConfig(sender, prefix);
                } else if (args[0].equalsIgnoreCase("addObjective")) {
                    addObjectiveHandler(sender, args, prefix);
                }
            }
        });
    }

    private void reloadConfig(CommandSender sender, Component prefix) {
        plugin.getSpeedrunConfig().load();
        sender.sendMessage(prefix.append(Component.text("Plugin config has been reloaded.")));
    }

    private void addObjectiveHandler(CommandSender sender, String[] args, Component prefix) {
        if (args.length == 3 || args.length == 5 || args.length == 7) {
            String objectiveType = args[1].toLowerCase();
            switch (objectiveType) {
                case "kill":
                    killObjectiveHandler(sender, args, prefix);
                case "enter":
                    // do something
                case "mine":
                    // do something
                case "obtain":
                    // do something
            }
        } else {
            sender.sendMessage(prefix.append(Component.text("Usage: /speedrun addObjective [event] [specifiction] <-flag(s)>")));
        }
    }

    private void killObjectiveHandler(CommandSender sender, String[] args, Component prefix) {
        try {
            EntityType e = EntityType.valueOf(args[2].toUpperCase());
            Optional<Integer> weight = parseWeightFlag(args);
            KillObjective ko = weight.map(w -> {
                try {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + e.name() + " (" + w + ")")));
                    return new KillObjective(e, w);
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a living entity.")));
                    return null;
                }
            }).orElseGet(() -> {
                try {
                    sender.sendMessage(prefix.append(Component.text("Objective Added: KILL " + e.name())));
                    return new KillObjective(e);
                } catch (NonLivingEntityException ex) {
                    sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a living entity.")));
                    return null;
                }
            });
            speedRun.addObjective(ko);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(prefix.append(Component.text("Illegal Argument: \"" + args[2] + "\" is not a valid entity type.")));
        }
    }

    private Optional<Integer> parseWeightFlag(String[] args) {
        for (int i = 3; i < args.length - 1; i++) {
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

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            suggestions.add("help");
            suggestions.add("reload");
            suggestions.add("addobjective");
        } else {
            if (args.length == 1) {
                addMatchingSuggestions(suggestions, args[0], "help", "reload", "addobjective");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("addobjective")) {
                    addMatchingSuggestions(suggestions, args[1], "kill", "enter", "mine", "obtain");
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

    private void addMatchingSuggestions(List<String> suggestions, String arg, String... possibleSuggestions) {
        for (String suggestion : possibleSuggestions) {
            if (suggestion.startsWith(arg.toLowerCase())) {
                suggestions.add(suggestion);
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "ksu.sr.user";
    }
}
