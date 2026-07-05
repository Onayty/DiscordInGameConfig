package pro.plugin.discordingameconfig.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import pro.plugin.discordingameconfig.managers.DiscordManager;
import pro.plugin.discordingameconfig.managers.InputManager;
import pro.plugin.discordingameconfig.utils.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameListener implements Listener {
    private final Map<UUID, int[]> actData = new HashMap<>();

    private boolean checkAction(Player p, String togglePath, String limitPath, int countIdx, int timeIdx) {
        if (!ConfigManager.getBool(togglePath)) return false;
        int limit = ConfigManager.getInt(limitPath);
        if (limit <= 0) return false;

        int[] data = actData.computeIfAbsent(p.getUniqueId(), k -> new int[8]);
        int now = (int) (System.currentTimeMillis() / 1000);
        if (now != data[timeIdx]) { data[countIdx] = 0; data[timeIdx] = now; }
        data[countIdx]++;
        if (data[countIdx] > limit) { data[countIdx] = 0; return true; }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatInput(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (InputManager.isAwaiting(p)) {
            e.setCancelled(true);
            String msg = PlainTextComponentSerializer.plainText().serialize(e.message());
            Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> InputManager.handle(p, msg));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message());
        String msgLower = msg.toLowerCase();

        if (checkAction(p, "anticheat.spam-enabled", "anticheat.spam-limit", 4, 5)) {
            e.setCancelled(true);
            triggerMute(p, "Chat Spam");
            return;
        }

        for (String w : ConfigManager.getList("filters.ban-words")) {
            if (msgLower.contains(w)) {
                e.setCancelled(true);
                p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-forbidden-word")));
                triggerBan(p, msg);
                return;
            }
        }

        for (String w : ConfigManager.getList("filters.banned-words")) {
            if (msgLower.contains(w)) {
                e.setCancelled(true);
                p.sendMessage(ColorUtil.color(ConfigManager.getMsg("msg-forbidden-word")));
                triggerMute(p, msg);
                return;
            }
        }
        if (ConfigManager.getBool("settings.log-chat")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-chat").replace("%player%", p.getName()).replace("%message%", msg));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (checkAction(e.getPlayer(), "anticheat.nuker-enabled", "anticheat.nuker-limit", 0, 1)) triggerBan(e.getPlayer(), "Nuker/FastBreak");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (checkAction(e.getPlayer(), "anticheat.fastplace-enabled", "anticheat.fastplace-limit", 6, 7)) triggerBan(e.getPlayer(), "FastPlace/Scaffold");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction().name().startsWith("LEFT_CLICK")) {
            if (checkAction(e.getPlayer(), "anticheat.clicker-enabled", "anticheat.clicker-limit", 2, 3)) triggerBan(e.getPlayer(), "AutoClicker/FastClick");
        }
    }

    private void triggerBan(Player p, String context) {
        boolean auto = ConfigManager.getBool("punishments.auto-punish");
        if (auto) {
            String cmd = ConfigManager.getStr("punishments.ban-cmd").replace("%player%", p.getName()).replace("%duration%", ConfigManager.getStr("punishments.ban-time")).replace("%reason%", ConfigManager.getStr("punishments.ban-reason"));
            Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
        if (ConfigManager.getBool("settings.log-suspicious")) DiscordManager.sendAlertWithButtons(ConfigManager.getMsg("discord-alert-sus-act").replace("%player%", p.getName()).replace("%message%", context), p.getName(), auto ? "ban" : null);
    }

    private void triggerMute(Player p, String context) {
        boolean auto = ConfigManager.getBool("punishments.auto-punish");
        if (auto) {
            String cmd = ConfigManager.getStr("punishments.mute-cmd").replace("%player%", p.getName()).replace("%duration%", ConfigManager.getStr("punishments.mute-time")).replace("%reason%", ConfigManager.getStr("punishments.mute-reason"));
            Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
        if (ConfigManager.getBool("settings.log-suspicious")) DiscordManager.sendAlertWithButtons(ConfigManager.getMsg("discord-alert-sus-act").replace("%player%", p.getName()).replace("%message%", context), p.getName(), auto ? "mute" : null);
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { if (ConfigManager.getBool("settings.log-join")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-join").replace("%player%", e.getPlayer().getName())); }
    @EventHandler public void onQuit(PlayerQuitEvent e) { if (ConfigManager.getBool("settings.log-quit")) DiscordManager.sendSimple(ConfigManager.getMsg("discord-format-quit").replace("%player%", e.getPlayer().getName())); }
}