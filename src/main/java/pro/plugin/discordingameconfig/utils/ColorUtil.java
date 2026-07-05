package pro.plugin.discordingameconfig.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtil {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().character('&').hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static Component color(String text) {
        return text == null ? Component.empty() : SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }
}