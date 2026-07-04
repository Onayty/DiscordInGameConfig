package pro.plugin.discordingameconfig.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import pro.plugin.discordingameconfig.utils.ColorUtil;
import pro.plugin.discordingameconfig.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuManager {
    public enum MenuType { MAIN, LOGGING, BOT, FILTERS, PUNISHMENTS, LANGUAGES }

    public static class MenuHolder implements InventoryHolder {
        private final MenuType type;
        private int page;
        public MenuHolder(MenuType type, int page) { this.type = type; this.page = page; }
        public MenuType getType() { return type; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        @Override public Inventory getInventory() { return null; }
    }

    public static int getBackSlot(MenuType type) {
        return (type == MenuType.FILTERS) ? 45 : 40;
    }

    public static void open(Player p, MenuType type) {
        open(p, type, 0);
    }

    public static void open(Player p, MenuType type, int page) {
        int size = (type == MenuType.FILTERS) ? 54 : 45;
        String titleKey = type == MenuType.LANGUAGES ? "lang" : type.name().toLowerCase();
        String title = ConfigManager.getMsg("gui-title-" + titleKey);

        Inventory inv = Bukkit.createInventory(new MenuHolder(type, page), size, ColorUtil.color(title));
        p.openInventory(inv);

        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        ItemStack corners = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }
        inv.setItem(0, corners); inv.setItem(8, corners);
        inv.setItem(size - 9, corners); inv.setItem(size - 1, corners);

        if (type != MenuType.MAIN) {
            inv.setItem(getBackSlot(type), new ItemBuilder(Material.BARRIER).name(ConfigManager.getMsg("gui-btn-back")).build());
        }

        List<Integer> targets = getAnimationSlots(type, size, page);
        int itemsPerTick = (type == MenuType.FILTERS) ? 9 : 1;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (p.getOpenInventory().getTopInventory() != inv) {
                    cancel();
                    return;
                }
                if (step >= targets.size()) {
                    cancel();
                    return;
                }
                for (int k = 0; k < itemsPerTick; k++) {
                    if (step >= targets.size()) break;
                    int slot = targets.get(step);
                    inv.setItem(slot, getSlotItem(type, slot, page));
                    step++;
                }
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.8f, 1.5f);
            }
        }.runTaskTimer(DiscordInGameConfig.getInstance(), 1L, 1L);
    }

    public static void update(Inventory inv, MenuType type, int page) {
        int size = (type == MenuType.FILTERS) ? 54 : 45;
        List<Integer> slots = getAnimationSlots(type, size, page);
        for (int slot : slots) {
            inv.setItem(slot, getSlotItem(type, slot, page));
        }
    }

    private static List<Integer> getAnimationSlots(MenuType type, int size, int page) {
        if (type == MenuType.FILTERS) {
            List<Integer> filterSlots = new ArrayList<>();
            for (int i = 0; i < 45; i++) filterSlots.add(i);
            filterSlots.add(48);
            filterSlots.add(49);
            filterSlots.add(50);
            return filterSlots;
        }
        switch (type) {
            case MAIN: return Arrays.asList(20, 21, 22, 23, 24);
            case BOT: return Arrays.asList(21, 23);
            case LOGGING: return Arrays.asList(19, 21, 23, 25);
            case PUNISHMENTS: return Arrays.asList(19, 20, 22, 24, 25);
            case LANGUAGES: return Arrays.asList(11, 13, 15, 29, 31, 33);
            default: return new ArrayList<>();
        }
    }

    private static ItemStack getSlotItem(MenuType type, int s, int page) {
        if (type != MenuType.MAIN && s == getBackSlot(type)) {
            return new ItemBuilder(Material.BARRIER).name(ConfigManager.getMsg("gui-btn-back")).build();
        }

        switch (type) {
            case MAIN:
                switch (s) {
                    case 20: return new ItemBuilder(Material.BOOK).name(ConfigManager.getMsg("gui-btn-logging")).build();
                    case 21: return new ItemBuilder(Material.DIAMOND).name(ConfigManager.getMsg("gui-btn-bot")).build();
                    case 22: return new ItemBuilder(Material.HOPPER).name(ConfigManager.getMsg("gui-btn-filters")).build();
                    case 23: return new ItemBuilder(Material.NETHERITE_AXE).name(ConfigManager.getMsg("gui-btn-punishments")).build();
                    case 24: return new ItemBuilder(Material.MAP).name(ConfigManager.getMsg("gui-btn-lang")).lore(ConfigManager.getMsg("gui-lore-lang")).build();
                }
                break;
            case BOT:
                switch (s) {
                    case 21:
                        String token = ConfigManager.getStr("bot-token");
                        String obfuscated = token.length() > 10 ? "***" : "None";
                        return new ItemBuilder(Material.PAPER).name(ConfigManager.getMsg("gui-btn-bot-token"))
                                .lore(ConfigManager.getMsg("gui-lore-bot-token").replace("%val%", obfuscated)).build();
                    case 23:
                        return new ItemBuilder(Material.COMPASS).name(ConfigManager.getMsg("gui-btn-bot-channel"))
                                .lore(ConfigManager.getMsg("gui-lore-bot-channel").replace("%val%", ConfigManager.getStr("channel-id"))).build();
                }
                break;
            case LOGGING:
                switch (s) {
                    case 19: return getToggleItem(ConfigManager.getBool("settings.log-chat"), "gui-log-chat");
                    case 21: return getToggleItem(ConfigManager.getBool("settings.log-join"), "gui-log-join");
                    case 23: return getToggleItem(ConfigManager.getBool("settings.log-quit"), "gui-log-quit");
                    case 25: return getToggleItem(ConfigManager.getBool("settings.log-suspicious"), "gui-log-sus");
                }
                break;
            case PUNISHMENTS:
                switch (s) {
                    case 19: return getToggleItem(ConfigManager.getBool("punishments.auto-punish"), "gui-btn-punish-auto");
                    case 20: return new ItemBuilder(Material.CLOCK).name(ConfigManager.getMsg("gui-btn-punish-time")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.mute-time"))).build();
                    case 22: return new ItemBuilder(Material.OAK_SIGN).name(ConfigManager.getMsg("gui-btn-punish-reason-m")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.mute-reason"))).build();
                    case 24: return new ItemBuilder(Material.RECOVERY_COMPASS).name(ConfigManager.getMsg("gui-btn-punish-ban-time")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.ban-time"))).build();
                    case 25: return new ItemBuilder(Material.CRIMSON_SIGN).name(ConfigManager.getMsg("gui-btn-punish-reason-b")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.ban-reason"))).build();
                }
                break;
            case LANGUAGES:
                switch (s) {
                    case 11: return new ItemBuilder(Material.LIGHT_BLUE_WOOL).name("&eEnglish").build();
                    case 13: return new ItemBuilder(Material.RED_WOOL).name("&eРусский").build();
                    case 15: return new ItemBuilder(Material.YELLOW_WOOL).name("&eEspañol").build();
                    case 29: return new ItemBuilder(Material.BLACK_WOOL).name("&eDeutsch").build();
                    case 31: return new ItemBuilder(Material.BLUE_WOOL).name("&eFrançais").build();
                    case 33: return new ItemBuilder(Material.ORANGE_WOOL).name("&e中文").build();
                }
                break;
            case FILTERS:
                if (s == 48) {
                    if (page > 0) return new ItemBuilder(Material.ARROW).name(ConfigManager.getMsg("gui-btn-prev-page")).build();
                    return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
                }
                if (s == 49) return new ItemBuilder(Material.EMERALD).name(ConfigManager.getMsg("gui-btn-add-word")).build();
                if (s == 50) {
                    List<String> words = ConfigManager.getList("filters.banned-words");
                    if ((page + 1) * 45 < words.size()) return new ItemBuilder(Material.ARROW).name(ConfigManager.getMsg("gui-btn-next-page")).build();
                    return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
                }
                List<String> words = ConfigManager.getList("filters.banned-words");
                int index = page * 45 + s;
                if (s < 45 && index < words.size()) {
                    return new ItemBuilder(Material.PAPER).name("&f" + words.get(index)).lore(ConfigManager.getMsg("gui-lore-word-delete")).build();
                }
                break;
        }
        return null;
    }

    private static ItemStack getToggleItem(boolean enabled, String key) {
        return new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(ConfigManager.getMsg(key))
                .lore(ConfigManager.getMsg(enabled ? "gui-status-on" : "gui-status-off"))
                .build();
    }
}