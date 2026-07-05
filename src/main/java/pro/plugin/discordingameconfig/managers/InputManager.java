package pro.plugin.discordingameconfig.managers;

import org.bukkit.entity.Player;
import pro.plugin.discordingameconfig.utils.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InputManager {
    public enum Type { TOKEN, CHANNEL, WORD_MUTE, WORD_BAN, WORD_CMD, WORD_SECRET, MUTE_TIME, MUTE_REASON, UNMUTE_CMD, BAN_TIME, BAN_REASON, UNBAN_CMD, LIMIT_NUKER, LIMIT_CLICKER, LIMIT_SPAM, LIMIT_FASTPLACE }

    private static class InputData {
        Type type;
        MenuManager.MenuType previousMenu;
        InputData(Type t, MenuManager.MenuType m) { this.type = t; this.previousMenu = m; }
    }

    private static final Map<UUID, InputData> map = new HashMap<>();

    public static void add(Player p, Type t, MenuManager.MenuType previousMenu) {
        map.put(p.getUniqueId(), new InputData(t, previousMenu));
        p.closeInventory();
        p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-input")));
    }

    public static boolean isAwaiting(Player p) { return map.containsKey(p.getUniqueId()); }

    public static void handle(Player p, String msg) {
        InputData data = map.remove(p.getUniqueId());
        if (data == null) return;

        if (msg.equalsIgnoreCase("cancel")) {
            p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-cancel")));
            MenuManager.open(p, data.previousMenu);
            return;
        }

        if (data.type == Type.MUTE_TIME || data.type == Type.BAN_TIME) {
            if (!msg.matches("^(?i)(perm|\\d+[smhd])$")) {
                p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-invalid-time")));
                map.put(p.getUniqueId(), data);
                p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-input")));
                return;
            }
        }

        switch (data.type) {
            case TOKEN -> { ConfigManager.setString("bot-token", msg); DiscordManager.init(); }
            case CHANNEL -> { ConfigManager.setString("channel-id", msg); DiscordManager.init(); }
            case WORD_MUTE -> ConfigManager.addWord("filters.banned-words", msg);
            case WORD_BAN -> ConfigManager.addWord("filters.ban-words", msg);
            case WORD_CMD -> ConfigManager.addWord("filters.suspicious-commands", msg.startsWith("/") ? msg : "/" + msg);
            case WORD_SECRET -> ConfigManager.addWord("privacy.hidden-commands", msg.startsWith("/") ? msg : "/" + msg);
            case MUTE_TIME -> ConfigManager.setString("punishments.mute-time", msg);
            case MUTE_REASON -> ConfigManager.setString("punishments.mute-reason", msg);
            case UNMUTE_CMD -> ConfigManager.setString("punishments.unmute-cmd", msg);
            case BAN_TIME -> ConfigManager.setString("punishments.ban-time", msg);
            case BAN_REASON -> ConfigManager.setString("punishments.ban-reason", msg);
            case UNBAN_CMD -> ConfigManager.setString("punishments.unban-cmd", msg);
            case LIMIT_NUKER -> setLimit(p, "anticheat.nuker-limit", msg, data);
            case LIMIT_CLICKER -> setLimit(p, "anticheat.clicker-limit", msg, data);
            case LIMIT_FASTPLACE -> setLimit(p, "anticheat.fastplace-limit", msg, data);
            case LIMIT_SPAM -> setLimit(p, "anticheat.spam-limit", msg, data);
        }
        if (data.type != Type.LIMIT_NUKER && data.type != Type.LIMIT_CLICKER && data.type != Type.LIMIT_SPAM && data.type != Type.LIMIT_FASTPLACE) {
            p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-saved")));
            MenuManager.open(p, data.previousMenu);
        }
    }

    private static void setLimit(Player p, String path, String msg, InputData data) {
        try {
            int val = Integer.parseInt(msg);
            ConfigManager.setInt(path, Math.max(val, 0));
            p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-saved")));
            MenuManager.open(p, data.previousMenu);
        } catch (NumberFormatException e) {
            p.sendMessage(ColorUtil.color("&cEnter a valid number!"));
            map.put(p.getUniqueId(), data);
            p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-input")));
        }
    }
}