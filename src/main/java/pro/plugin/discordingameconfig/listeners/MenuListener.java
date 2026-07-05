package pro.plugin.discordingameconfig.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import pro.plugin.discordingameconfig.managers.InputManager;
import pro.plugin.discordingameconfig.managers.MenuManager;
import pro.plugin.discordingameconfig.managers.MenuManager.MenuType;

public class MenuListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuManager.MenuHolder holder)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Player p = (Player) e.getWhoClicked();
        int s = e.getRawSlot();
        MenuType type = holder.getType();
        int page = holder.getPage();

        int backSlot = type.name().startsWith("FILTERS_") ? 45 : 49;
        if (type != MenuType.MAIN && s == backSlot) {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 0.8f);
            MenuManager.open(p, type == MenuType.FILTERS_MUTE ? MenuType.HUB_MUTE : (type == MenuType.FILTERS_BAN || type == MenuType.FILTERS_CMD ? MenuType.HUB_BAN : MenuType.MAIN));
            return;
        }

        switch (type) {
            case MAIN -> {
                if (s == 20) MenuManager.open(p, MenuType.LOGGING);
                else if (s == 21) MenuManager.open(p, MenuType.BOT);
                else if (s == 22) MenuManager.open(p, MenuType.PUNISHMENTS);
                else if (s == 23) MenuManager.open(p, MenuType.HUB_MUTE);
                else if (s == 24) MenuManager.open(p, MenuType.HUB_BAN);
                else if (s == 30) MenuManager.open(p, MenuType.FILTERS_SECRET);
                else if (s == 32) MenuManager.open(p, MenuType.LANGUAGES);
            }
            case HUB_MUTE -> {
                if (s == 21) MenuManager.open(p, MenuType.FILTERS_MUTE);
                else if (s == 23) {
                    if (e.isRightClick()) InputManager.add(p, InputManager.Type.LIMIT_SPAM, type);
                    else { ConfigManager.toggle("anticheat.spam-enabled"); MenuManager.update(e.getInventory(), type, page); }
                }
            }
            case HUB_BAN -> {
                if (s == 20) MenuManager.open(p, MenuType.FILTERS_BAN);
                else if (s == 21) MenuManager.open(p, MenuType.FILTERS_CMD);
                else if (s == 22) {
                    if (e.isRightClick()) InputManager.add(p, InputManager.Type.LIMIT_NUKER, type);
                    else { ConfigManager.toggle("anticheat.nuker-enabled"); MenuManager.update(e.getInventory(), type, page); }
                }
                else if (s == 23) {
                    if (e.isRightClick()) InputManager.add(p, InputManager.Type.LIMIT_FASTPLACE, type);
                    else { ConfigManager.toggle("anticheat.fastplace-enabled"); MenuManager.update(e.getInventory(), type, page); }
                }
                else if (s == 24) {
                    if (e.isRightClick()) InputManager.add(p, InputManager.Type.LIMIT_CLICKER, type);
                    else { ConfigManager.toggle("anticheat.clicker-enabled"); MenuManager.update(e.getInventory(), type, page); }
                }
            }
            case BOT -> {
                if (s == 21) InputManager.add(p, InputManager.Type.TOKEN, type);
                else if (s == 23) InputManager.add(p, InputManager.Type.CHANNEL, type);
            }
            case LOGGING -> {
                if (s == 20) ConfigManager.toggle("settings.log-chat");
                else if (s == 21) ConfigManager.toggle("settings.log-commands");
                else if (s == 22) ConfigManager.toggle("settings.log-join");
                else if (s == 23) ConfigManager.toggle("settings.log-quit");
                else if (s == 24) ConfigManager.toggle("settings.log-suspicious");
                MenuManager.update(e.getInventory(), type, page);
            }
            case PUNISHMENTS -> {
                if (s == 13) { ConfigManager.toggle("punishments.auto-punish"); MenuManager.update(e.getInventory(), type, page); }
                else if (s == 19) InputManager.add(p, InputManager.Type.MUTE_TIME, type);
                else if (s == 20) InputManager.add(p, InputManager.Type.MUTE_REASON, type);
                else if (s == 21) InputManager.add(p, InputManager.Type.UNMUTE_CMD, type);
                else if (s == 23) InputManager.add(p, InputManager.Type.BAN_TIME, type);
                else if (s == 24) InputManager.add(p, InputManager.Type.BAN_REASON, type);
                else if (s == 25) InputManager.add(p, InputManager.Type.UNBAN_CMD, type);
            }
            case LANGUAGES -> {
                if (s == 21) ConfigManager.setLanguage("en");
                else if (s == 22) ConfigManager.setLanguage("ru");
                else if (s == 23) ConfigManager.setLanguage("es");
                else if (s == 30) ConfigManager.setLanguage("de");
                else if (s == 31) ConfigManager.setLanguage("fr");
                else if (s == 32) ConfigManager.setLanguage("cn");
                MenuManager.open(p, MenuType.MAIN);
            }
            case FILTERS_MUTE, FILTERS_BAN, FILTERS_CMD, FILTERS_SECRET -> {
                String listKey = ""; InputManager.Type inType = InputManager.Type.WORD_MUTE;
                if (type == MenuType.FILTERS_MUTE) { listKey = "filters.banned-words"; inType = InputManager.Type.WORD_MUTE; }
                else if (type == MenuType.FILTERS_BAN) { listKey = "filters.ban-words"; inType = InputManager.Type.WORD_BAN; }
                else if (type == MenuType.FILTERS_CMD) { listKey = "filters.suspicious-commands"; inType = InputManager.Type.WORD_CMD; }
                else if (type == MenuType.FILTERS_SECRET) { listKey = "privacy.hidden-commands"; inType = InputManager.Type.WORD_SECRET; }

                if (s == 48 && page > 0) MenuManager.open(p, type, page - 1);
                else if (s == 50 && (page + 1) * 28 < ConfigManager.getList(listKey).size()) MenuManager.open(p, type, page + 1);
                else if (s == 49) InputManager.add(p, inType, type);
                else if (s < 45 && (e.getCurrentItem().getType() == Material.PAPER || e.getCurrentItem().getType() == Material.MAP || e.getCurrentItem().getType() == Material.COMMAND_BLOCK || e.getCurrentItem().getType() == Material.SPYGLASS)) {
                    if (e.isRightClick()) {
                        String word = PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().getItemMeta().displayName());
                        ConfigManager.removeWord(listKey, word);
                        MenuManager.update(e.getInventory(), type, page);
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1.2f);
                        return;
                    }
                }
            }
        }
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.2f);
    }
}