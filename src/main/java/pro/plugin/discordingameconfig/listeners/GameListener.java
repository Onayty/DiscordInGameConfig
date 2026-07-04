package pro.plugin.discordingameconfig.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import pro.plugin.discordingameconfig.managers.DiscordManager;
import pro.plugin.discordingameconfig.managers.InputManager;

public class GameListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatInput(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (InputManager.isAwaiting(p)) {
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> InputManager.handle(p, e.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String msgLower = e.getMessage().toLowerCase();

        for (String w : ConfigManager.getList("filters.banned-words")) {
            if (msgLower.contains(w)) {
                e.setCancelled(true);

                if (ConfigManager.getBool("punishments.auto-punish")) {
                    String cmd = ConfigManager.getStr("punishments.mute-cmd").replace("%player%", p.getName())
                            .replace("%duration%", ConfigManager.getStr("punishments.mute-time"))
                            .replace("%reason%", ConfigManager.getStr("punishments.mute-reason"));
                    Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
                }

                if (ConfigManager.getBool("settings.log-suspicious")) {
                    String alert = ConfigManager.getMsg("discord-alert-banned-word").replace("%player%", p.getName()).replace("%message%", e.getMessage());
                    DiscordManager.sendAlertWithButtons(alert, p.getName());
                }
                return;
            }
        }
        if (ConfigManager.getBool("settings.log-chat")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-chat").replace("%player%", p.getName()).replace("%message%", e.getMessage()));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!ConfigManager.getBool("settings.log-suspicious")) return;
        String cmdLower = e.getMessage().toLowerCase();
        for (String s : ConfigManager.getList("filters.suspicious-commands")) {
            if (cmdLower.startsWith(s)) {
                String alert = ConfigManager.getMsg("discord-alert-sus-cmd").replace("%player%", e.getPlayer().getName()).replace("%message%", e.getMessage());
                DiscordManager.sendAlertWithButtons(alert, e.getPlayer().getName());
                break;
            }
        }
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { if (ConfigManager.getBool("settings.log-join")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-join").replace("%player%", e.getPlayer().getName())); }
    @EventHandler public void onQuit(PlayerQuitEvent e) { if (ConfigManager.getBool("settings.log-quit")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-quit").replace("%player%", e.getPlayer().getName())); }
}