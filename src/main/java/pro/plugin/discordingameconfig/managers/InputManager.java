package pro.plugin.discordingameconfig.managers;

import org.bukkit.entity.Player;
import pro.plugin.discordingameconfig.utils.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InputManager {
    public enum Type { TOKEN, CHANNEL, WORD, MUTE_TIME, MUTE_REASON, BAN_TIME, BAN_REASON }

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
            case TOKEN: ConfigManager.setString("bot-token", msg); DiscordManager.init(); break;
            case CHANNEL: ConfigManager.setString("channel-id", msg); DiscordManager.init(); break;
            case WORD: ConfigManager.addBannedWord(msg); break;
            case MUTE_TIME: ConfigManager.setString("punishments.mute-time", msg); break;
            case MUTE_REASON: ConfigManager.setString("punishments.mute-reason", msg); break;
            case BAN_TIME: ConfigManager.setString("punishments.ban-time", msg); break;
            case BAN_REASON: ConfigManager.setString("punishments.ban-reason", msg); break;
        }
        p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-saved")));
        MenuManager.open(p, data.previousMenu);
    }
}