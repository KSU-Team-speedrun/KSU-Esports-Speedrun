package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpeedrun implements BasicCommand {

    Main plugin;

    public CommandSpeedrun(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        if (args.length == 0) {
            // help list
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                // help list
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.getSpeedrunConfig().load();
                commandSourceStack.getSender().sendMessage(Component.text("Plugin config has been reloaded."));
            } else {
                // continue
            }
        } else {
            //continue
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 0) {
            suggestions.add("help");
            suggestions.add("reload");
        }
        return suggestions;
    }

    @Override
    public @Nullable String permission() {
        return "ksu.sr.user";
    }
}
