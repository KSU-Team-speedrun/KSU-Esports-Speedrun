package edu.Kennesaw.ksumcspeedrun.Utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ComponentHelper {

    static MiniMessage mm = MiniMessage.miniMessage();

    // Convert a MiniMessage String into a Component
    public static Component mmStringToComponent(String txt) {

        return mm.deserialize(txt);

    }

    // Convert a MiniMessage String into a Component w/ Tag Resolvers
    public static Component mmStringToComponent(String txt, TagResolver... resolvers) {

        TagResolver combinedResolvers = TagResolver.resolver(resolvers);
        return mm.deserialize(txt, combinedResolvers);

    }

}
