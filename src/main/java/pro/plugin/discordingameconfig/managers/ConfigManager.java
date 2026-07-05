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
        checkDefaults();
        for (String lang : LANGS) {
            File f = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
            if (!f.exists()) {
                try { plugin.saveResource("messages_" + lang + ".yml", false); }
                catch (Exception ignored) {}
            }
        }
        loadLocale();
    }

    private static void checkDefaults() {
        if (!config.contains("settings.log-commands")) config.set("settings.log-commands", true);
        if (!config.contains("punishments.unmute-cmd")) config.set("punishments.unmute-cmd", "unmute %player%");
        if (!config.contains("punishments.unban-cmd")) config.set("punishments.unban-cmd", "unban %player%");
        if (!config.contains("filters.ban-words")) config.set("filters.ban-words", Arrays.asList("ddos", "swat"));
        if (!config.contains("filters.suspicious-commands")) config.set("filters.suspicious-commands", Arrays.asList("/op", "/stop"));
        if (!config.contains("privacy.hidden-commands")) config.set("privacy.hidden-commands", Arrays.asList("/login", "/register", "/l", "/reg"));

        if (!config.contains("anticheat.nuker-enabled")) config.set("anticheat.nuker-enabled", true);
        if (!config.contains("anticheat.nuker-limit")) config.set("anticheat.nuker-limit", 20);
        if (!config.contains("anticheat.clicker-enabled")) config.set("anticheat.clicker-enabled", true);
        if (!config.contains("anticheat.clicker-limit")) config.set("anticheat.clicker-limit", 25);
        if (!config.contains("anticheat.fastplace-enabled")) config.set("anticheat.fastplace-enabled", true);
        if (!config.contains("anticheat.fastplace-limit")) config.set("anticheat.fastplace-limit", 15);
        if (!config.contains("anticheat.spam-enabled")) config.set("anticheat.spam-enabled", true);
        if (!config.contains("anticheat.spam-limit")) config.set("anticheat.spam-limit", 5);
        save();
    }

    public static void loadLocale() {
        String lang = config.getString("lang", "en");
        File file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!file.exists()) file = new File(plugin.getDataFolder(), "messages_en.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static void setLanguage(String lang) { config.set("lang", lang); save(); loadLocale(); }
    public static void toggle(String path) { config.set(path, !config.getBoolean(path)); save(); }
    public static void setString(String path, String value) { config.set(path, value); save(); }
    public static void setInt(String path, int value) { config.set(path, value); save(); }

    public static void addWord(String list, String word) {
        List<String> w = config.getStringList(list);
        if (!w.contains(word.toLowerCase())) { w.add(word.toLowerCase()); config.set(list, w); save(); }
    }

    public static void removeWord(String list, String word) {
        List<String> w = config.getStringList(list);
        w.remove(word.toLowerCase()); config.set(list, w); save();
    }

    private static void save() { plugin.saveConfig(); }

    public static String getMsg(String path) { return messages.getString(path, path); }
    public static boolean getBool(String path) { return config.getBoolean(path); }
    public static String getStr(String path) { return config.getString(path); }
    public static int getInt(String path) { return config.getInt(path); }
    public static List<String> getList(String path) { return config.getStringList(path); }
}