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
    public enum MenuType { MAIN, LOGGING, BOT, HUB_MUTE, HUB_BAN, FILTERS_MUTE, FILTERS_BAN, FILTERS_CMD, FILTERS_SECRET, PUNISHMENTS, LANGUAGES }

    public static class MenuHolder implements InventoryHolder {
        private final MenuType type;
        private int page;
        public MenuHolder(MenuType type, int page) { this.type = type; this.page = page; }
        public MenuType getType() { return type; }
        public int getPage() { return page; }
        @Override public Inventory getInventory() { return null; }
    }

    public static int getBackSlot(MenuType type) { return type.name().startsWith("FILTERS_") ? 45 : 49; }
    public static void open(Player p, MenuType type) { open(p, type, 0); }

    public static void open(Player p, MenuType type, int page) {
        String titleKey = type.name().toLowerCase().replace("_", "-");
        Inventory inv = Bukkit.createInventory(new MenuHolder(type, page), 54, ColorUtil.color(ConfigManager.getMsg("gui-title-" + titleKey)));
        p.openInventory(inv);

        ItemStack bg = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        ItemStack blue = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name(" ").build();
        ItemStack cyan = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(" ").build();

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                if (i == 0 || i == 8 || i == 45 || i == 53) inv.setItem(i, cyan);
                else inv.setItem(i, blue);
            } else if (!type.name().startsWith("FILTERS_")) {
                inv.setItem(i, bg);
            }
        }

        int backSlot = getBackSlot(type);
        if (type != MenuType.MAIN) inv.setItem(backSlot, new ItemBuilder(Material.BARRIER).name(ConfigManager.getMsg("gui-btn-back")).build());

        List<Integer> targets = getAnimationSlots(type);
        int itemsPerTick = type.name().startsWith("FILTERS_") ? 7 : 1;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                Inventory top = p.getOpenInventory().getTopInventory();
                if (top == null || top.getHolder() != inv.getHolder() || step >= targets.size()) {
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
        for (int slot : getAnimationSlots(type)) inv.setItem(slot, getSlotItem(type, slot, page));
    }

    private static List<Integer> getAnimationSlots(MenuType type) {
        if (type.name().startsWith("FILTERS_")) {
            List<Integer> slots = new ArrayList<>();
            for (int i : new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43}) slots.add(i);
            slots.addAll(Arrays.asList(48, 49, 50));
            return slots;
        }
        return switch (type) {
            case MAIN -> Arrays.asList(20, 21, 22, 23, 24, 30, 32);
            case HUB_MUTE -> Arrays.asList(21, 23);
            case HUB_BAN -> Arrays.asList(20, 21, 22, 23, 24);
            case BOT -> Arrays.asList(21, 23);
            case LOGGING -> Arrays.asList(20, 21, 22, 23, 24);
            case PUNISHMENTS -> Arrays.asList(13, 19, 20, 21, 23, 24, 25);
            case LANGUAGES -> Arrays.asList(21, 22, 23, 30, 31, 32);
            default -> new ArrayList<>();
        };
    }

    private static ItemStack getSlotItem(MenuType type, int s, int page) {
        if (type != MenuType.MAIN && s == getBackSlot(type)) return new ItemBuilder(Material.BARRIER).name(ConfigManager.getMsg("gui-btn-back")).build();

        switch (type) {
            case MAIN:
                if (s == 20) return new ItemBuilder(Material.RECOVERY_COMPASS).name(ConfigManager.getMsg("gui-btn-logging")).build();
                if (s == 21) return new ItemBuilder(Material.ECHO_SHARD).name(ConfigManager.getMsg("gui-btn-bot")).build();
                if (s == 22) return new ItemBuilder(Material.MACE).name(ConfigManager.getMsg("gui-btn-punishments")).build();
                if (s == 23) return new ItemBuilder(Material.NOTE_BLOCK).name(ConfigManager.getMsg("gui-btn-hub-mute")).build();
                if (s == 24) return new ItemBuilder(Material.OMINOUS_TRIAL_KEY).name(ConfigManager.getMsg("gui-btn-hub-ban")).build();
                if (s == 30) return new ItemBuilder(Material.SPYGLASS).name(ConfigManager.getMsg("gui-btn-filters-secret")).build();
                if (s == 32) return new ItemBuilder(Material.MAP).name(ConfigManager.getMsg("gui-btn-lang")).lore(ConfigManager.getMsg("gui-lore-lang")).build();
                break;
            case HUB_MUTE:
                if (s == 21) return new ItemBuilder(Material.PAPER).name(ConfigManager.getMsg("gui-btn-filter-mute")).build();
                if (s == 23) return getActionItem("gui-btn-act-spam", "anticheat.spam-enabled", "anticheat.spam-limit");
                break;
            case HUB_BAN:
                if (s == 20) return new ItemBuilder(Material.MAP).name(ConfigManager.getMsg("gui-btn-filter-ban")).build();
                if (s == 21) return new ItemBuilder(Material.COMMAND_BLOCK).name(ConfigManager.getMsg("gui-btn-filter-cmd")).build();
                if (s == 22) return getActionItem("gui-btn-act-nuker", "anticheat.nuker-enabled", "anticheat.nuker-limit");
                if (s == 23) return getActionItem("gui-btn-act-fastplace", "anticheat.fastplace-enabled", "anticheat.fastplace-limit");
                if (s == 24) return getActionItem("gui-btn-act-clicker", "anticheat.clicker-enabled", "anticheat.clicker-limit");
                break;
            case BOT:
                if (s == 21) return new ItemBuilder(Material.PAPER).name(ConfigManager.getMsg("gui-btn-bot-token")).lore(ConfigManager.getMsg("gui-lore-bot-token").replace("%val%", ConfigManager.getStr("bot-token").length() > 10 ? "***" : "None")).build();
                if (s == 23) return new ItemBuilder(Material.COMPASS).name(ConfigManager.getMsg("gui-btn-bot-channel")).lore(ConfigManager.getMsg("gui-lore-bot-channel").replace("%val%", ConfigManager.getStr("channel-id"))).build();
                break;
            case LOGGING:
                if (s == 20) return getToggleItem(ConfigManager.getBool("settings.log-chat"), "gui-log-chat");
                if (s == 21) return getToggleItem(ConfigManager.getBool("settings.log-commands"), "gui-log-cmd");
                if (s == 22) return getToggleItem(ConfigManager.getBool("settings.log-join"), "gui-log-join");
                if (s == 23) return getToggleItem(ConfigManager.getBool("settings.log-quit"), "gui-log-quit");
                if (s == 24) return getToggleItem(ConfigManager.getBool("settings.log-suspicious"), "gui-log-sus");
                break;
            case PUNISHMENTS:
                if (s == 13) return getToggleItem(ConfigManager.getBool("punishments.auto-punish"), "gui-btn-punish-auto");
                if (s == 19) return new ItemBuilder(Material.CLOCK).name(ConfigManager.getMsg("gui-btn-punish-time")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.mute-time"))).build();
                if (s == 20) return new ItemBuilder(Material.OAK_SIGN).name(ConfigManager.getMsg("gui-btn-punish-reason-m")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.mute-reason"))).build();
                if (s == 21) return new ItemBuilder(Material.EMERALD).name(ConfigManager.getMsg("gui-btn-unmute-cmd")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.unmute-cmd"))).build();
                if (s == 23) return new ItemBuilder(Material.RECOVERY_COMPASS).name(ConfigManager.getMsg("gui-btn-punish-ban-time")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.ban-time"))).build();
                if (s == 24) return new ItemBuilder(Material.CRIMSON_SIGN).name(ConfigManager.getMsg("gui-btn-punish-reason-b")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.ban-reason"))).build();
                if (s == 25) return new ItemBuilder(Material.DIAMOND).name(ConfigManager.getMsg("gui-btn-unban-cmd")).lore(ConfigManager.getMsg("gui-lore-punish-val").replace("%val%", ConfigManager.getStr("punishments.unban-cmd"))).build();
                break;
            case LANGUAGES:
                if (s == 21) return new ItemBuilder(Material.LIGHT_BLUE_WOOL).name("&eEnglish").build();
                if (s == 22) return new ItemBuilder(Material.RED_WOOL).name("&eРусский").build();
                if (s == 23) return new ItemBuilder(Material.YELLOW_WOOL).name("&eEspañol").build();
                if (s == 30) return new ItemBuilder(Material.BLACK_WOOL).name("&eDeutsch").build();
                if (s == 31) return new ItemBuilder(Material.BLUE_WOOL).name("&eFrançais").build();
                if (s == 32) return new ItemBuilder(Material.ORANGE_WOOL).name("&e中文").build();
                break;
            case FILTERS_MUTE:
            case FILTERS_BAN:
            case FILTERS_CMD:
            case FILTERS_SECRET:
                String listKey = ""; Material mat = Material.PAPER;
                if (type == MenuType.FILTERS_MUTE) { listKey = "filters.banned-words"; mat = Material.PAPER; }
                else if (type == MenuType.FILTERS_BAN) { listKey = "filters.ban-words"; mat = Material.MAP; }
                else if (type == MenuType.FILTERS_CMD) { listKey = "filters.suspicious-commands"; mat = Material.COMMAND_BLOCK; }
                else if (type == MenuType.FILTERS_SECRET) { listKey = "privacy.hidden-commands"; mat = Material.SPYGLASS; }

                if (s == 48) return page > 0 ? new ItemBuilder(Material.ARROW).name(ConfigManager.getMsg("gui-btn-prev-page")).build() : new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name(" ").build();
                if (s == 49) return new ItemBuilder(Material.EMERALD).name(ConfigManager.getMsg("gui-btn-add-word")).build();
                if (s == 50) return (page + 1) * 28 < ConfigManager.getList(listKey).size() ? new ItemBuilder(Material.ARROW).name(ConfigManager.getMsg("gui-btn-next-page")).build() : new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name(" ").build();

                List<String> words = ConfigManager.getList(listKey);

                int localIndex = -1;
                int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
                for (int i = 0; i < slots.length; i++) { if (slots[i] == s) { localIndex = i; break; } }

                if (localIndex != -1) {
                    int realIndex = page * 28 + localIndex;
                    if (realIndex < words.size()) return new ItemBuilder(mat).name("&f" + words.get(realIndex)).lore(ConfigManager.getMsg("gui-lore-word-delete")).build();
                }
                break;
        }
        return null;
    }

    private static ItemStack getToggleItem(boolean enabled, String key) {
        return new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE).name(ConfigManager.getMsg(key)).lore(ConfigManager.getMsg(enabled ? "gui-status-on" : "gui-status-off") + ConfigManager.getMsg(enabled ? "gui-click-disable" : "gui-click-enable")).build();
    }

    private static ItemStack getActionItem(String nameKey, String togglePath, String limitPath) {
        boolean enabled = ConfigManager.getBool(togglePath);
        String status = ConfigManager.getMsg(enabled ? "gui-status-on" : "gui-status-off");
        return new ItemBuilder(enabled ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD).name(ConfigManager.getMsg(nameKey)).lore(ConfigManager.getMsg("gui-lore-act-limit").replace("%status%", status).replace("%val%", String.valueOf(ConfigManager.getInt(limitPath)))).build();
    }
}