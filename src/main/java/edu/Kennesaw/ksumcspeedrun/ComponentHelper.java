package edu.Kennesaw.ksumcspeedrun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public class ComponentHelper {

    static MiniMessage mm = MiniMessage.miniMessage();

    public static Component replaceComponent(Component component, String regex, String replacement) {
        return component.replaceText(builder -> builder.matchLiteral(regex).replacement(replacement));
    }

    public static Component mmStringToComponent(String txt) {
        return mm.deserialize(txt);
    }

    public static String componentToMM(Component component) {
        return mm.serialize(component);
    }

    public static Component jsonToComponent(String json) {
        return JSONComponentSerializer.json().deserializeOrNull(json);
    }

    public static String componentToJson(Component component) {
        return JSONComponentSerializer.json().serializeOrNull(component);
    }

}
