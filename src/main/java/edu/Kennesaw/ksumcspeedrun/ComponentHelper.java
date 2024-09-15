package edu.Kennesaw.ksumcspeedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public class ComponentHelper {

    static MiniMessage mm = MiniMessage.miniMessage();

    // Replace a string within a Component, e.g., replace "<Player>" with "player.getName()"
    public static Component replaceComponent(Component component, String regex, String replacement) {

        return component.replaceText(builder -> builder.matchLiteral(regex).replacement(replacement));

    }

    // Convert a MiniMessage String into a Component
    public static Component mmStringToComponent(String txt) {

        return mm.deserialize(txt);

    }

    // Convert a Component into a MiniMessage String
    public static String componentToMM(Component component) {

        return mm.serialize(component);

    }

    // Convert a Minecraft JSON String into a Component
    public static Component jsonToComponent(String json) {

        return JSONComponentSerializer.json().deserializeOrNull(json);

    }

    // Convert a Component into a Minecraft JSON String
    public static String componentToJson(Component component) {

        return JSONComponentSerializer.json().serializeOrNull(component);

    }

}
