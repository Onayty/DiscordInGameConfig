package pro.plugin.discordingameconfig.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private static FileConfiguration config;
    private static FileConfiguration messages;
    private static DiscordInGameConfig plugin;
    private static final List<String> LANGS = Arrays.asList("en", "ru", "es", "de", "fr", "cn");

    public static void init(DiscordInGameConfig instance) {
        plugin = instance;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        for (String lang : LANGS) {
            File f = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
            if (!f.exists()) {
                try { plugin.saveResource("messages_" + lang + ".yml", false); }
                catch (Exception ignored) {}
            }
        }
        loadLocale();
    }

    public static void loadLocale() {
        String lang = config.getString("lang", "en");
        File file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!file.exists()) file = new File(plugin.getDataFolder(), "messages_en.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static void cycleLanguage() {
        String current = config.getString("lang", "en");
        int index = LANGS.indexOf(current);
        config.set("lang", (index == -1 || index == LANGS.size() - 1) ? LANGS.get(0) : LANGS.get(index + 1));
        save(); loadLocale();
    }

    public static void toggle(String path) { config.set(path, !config.getBoolean(path)); save(); }
    public static void setString(String path, String value) { config.set(path, value); save(); }

    public static void setLanguage(String lang) {
        config.set("lang", lang);
        save(); loadLocale();
    }

    public static void removeBannedWord(String word) {
        List<String> w = config.getStringList("filters.banned-words");
        w.remove(word.toLowerCase());
        config.set("filters.banned-words", w);
        save();
    }

    public static void addBannedWord(String word) {
        List<String> w = config.getStringList("filters.banned-words");
        w.add(word.toLowerCase()); config.set("filters.banned-words", w); save();
    }
    public static void removeLastBannedWord() {
        List<String> w = config.getStringList("filters.banned-words");
        if (!w.isEmpty()) { w.remove(w.size() - 1); config.set("filters.banned-words", w); save(); }
    }

    private static void save() { plugin.saveConfig(); }

    public static String getMsg(String path) { return messages.getString(path, path); }
    public static boolean getBool(String path) { return config.getBoolean(path); }
    public static String getStr(String path) { return config.getString(path); }
    public static List<String> getList(String path) { return config.getStringList(path); }
}
