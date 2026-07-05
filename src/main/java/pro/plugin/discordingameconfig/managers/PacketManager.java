package pro.plugin.discordingameconfig.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import pro.plugin.discordingameconfig.DiscordInGameConfig;

public class PacketManager {
    public static void register(DiscordInGameConfig plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.LOWEST, PacketType.Play.Client.CHAT_COMMAND) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                String cmd = event.getPacket().getStrings().readSafely(0);
                if (cmd == null) return;
                String fullCmd = "/" + cmd;
                String baseCmd = fullCmd.split(" ")[0].toLowerCase();

                // Проверка скрытых команд (строгое совпадение корня команды)
                for (String hidden : ConfigManager.getList("privacy.hidden-commands")) {
                    if (baseCmd.equalsIgnoreCase(hidden)) return;
                }

                if (ConfigManager.getBool("settings.log-commands")) {
                    DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-cmd").replace("%player%", event.getPlayer().getName()).replace("%message%", fullCmd));
                }

                if (ConfigManager.getBool("settings.log-suspicious")) {
                    for (String sus : ConfigManager.getList("filters.suspicious-commands")) {
                        if (fullCmd.toLowerCase().startsWith(sus.toLowerCase())) {
                            String alert = ConfigManager.getMsg("discord-alert-sus-cmd").replace("%player%", event.getPlayer().getName()).replace("%message%", fullCmd);
                            DiscordManager.sendAlertWithButtons(alert, event.getPlayer().getName(), ConfigManager.getBool("punishments.auto-punish") ? "ban" : null);
                            break;
                        }
                    }
                }
            }
        });
    }
}