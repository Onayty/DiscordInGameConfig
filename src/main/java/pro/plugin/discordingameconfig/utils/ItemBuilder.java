package pro.plugin.discordingameconfig.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ColorUtil.color(name));
        return this;
    }

    public ItemBuilder lore(String loreString) {
        List<String> list = new ArrayList<>();
        for (String s : loreString.split("\\n")) {
            list.add(ColorUtil.color(s));
        }
        meta.setLore(list);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}