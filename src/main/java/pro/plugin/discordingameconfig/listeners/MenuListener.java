package pro.plugin.discordingameconfig.listeners;

import org.bukkit.ChatColor;
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
import java.util.List;

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

        if (type != MenuType.MAIN && s == MenuManager.getBackSlot(type)) {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 0.8f);
            MenuManager.open(p, MenuType.MAIN);
            return;
        }

        switch (type) {
            case MAIN:
                if (s == 20) MenuManager.open(p, MenuType.LOGGING);
                else if (s == 21) MenuManager.open(p, MenuType.BOT);
                else if (s == 22) MenuManager.open(p, MenuType.FILTERS);
                else if (s == 23) MenuManager.open(p, MenuType.PUNISHMENTS);
                else if (s == 24) MenuManager.open(p, MenuType.LANGUAGES);
                break;
            case BOT:
                if (s == 21) InputManager.add(p, InputManager.Type.TOKEN, type);
                else if (s == 23) InputManager.add(p, InputManager.Type.CHANNEL, type);
                break;
            case LOGGING:
                if (s == 19) ConfigManager.toggle("settings.log-chat");
                else if (s == 21) ConfigManager.toggle("settings.log-join");
                else if (s == 23) ConfigManager.toggle("settings.log-quit");
                else if (s == 25) ConfigManager.toggle("settings.log-suspicious");
                MenuManager.update(e.getInventory(), type, page);
                break;
            case PUNISHMENTS:
                if (s == 19) { ConfigManager.toggle("punishments.auto-punish"); MenuManager.update(e.getInventory(), type, page); }
                else if (s == 20) InputManager.add(p, InputManager.Type.MUTE_TIME, type);
                else if (s == 22) InputManager.add(p, InputManager.Type.MUTE_REASON, type);
                else if (s == 24) InputManager.add(p, InputManager.Type.BAN_TIME, type);
                else if (s == 25) InputManager.add(p, InputManager.Type.BAN_REASON, type);
                break;
            case LANGUAGES:
                if (s == 11) ConfigManager.setLanguage("en");
                else if (s == 13) ConfigManager.setLanguage("ru");
                else if (s == 15) ConfigManager.setLanguage("es");
                else if (s == 29) ConfigManager.setLanguage("de");
                else if (s == 31) ConfigManager.setLanguage("fr");
                else if (s == 33) ConfigManager.setLanguage("cn");
                MenuManager.open(p, MenuType.MAIN);
                break;
            case FILTERS:
                if (s == 48 && page > 0) {
                    MenuManager.open(p, MenuType.FILTERS, page - 1);
                } else if (s == 50) {
                    List<String> words = ConfigManager.getList("filters.banned-words");
                    if ((page + 1) * 45 < words.size()) {
                        MenuManager.open(p, MenuType.FILTERS, page + 1);
                    }
                } else if (s == 49) {
                    InputManager.add(p, InputManager.Type.WORD, type);
                } else if (s < 45 && e.getCurrentItem().getType() == Material.PAPER) {
                    if (e.isRightClick()) {
                        String word = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                        ConfigManager.removeBannedWord(word);
                        MenuManager.update(e.getInventory(), type, page);
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.4f, 1.2f);
                        return;
                    }
                }
                break;
        }
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.2f);
    }
}