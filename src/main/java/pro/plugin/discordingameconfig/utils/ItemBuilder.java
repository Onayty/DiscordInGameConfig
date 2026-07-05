package pro.plugin.discordingameconfig.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
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
        if (this.meta != null) {
            for (ItemFlag flag : ItemFlag.values()) {
                this.meta.addItemFlags(flag);
            }
        }
    }

    public ItemBuilder name(String name) {
        if (this.meta != null) this.meta.displayName(ColorUtil.color(name));
        return this;
    }

    public ItemBuilder lore(String loreString) {
        if (this.meta != null) {
            List<Component> list = new ArrayList<>();
            for (String s : loreString.split("\\n")) {
                list.add(ColorUtil.color(s));
            }
            this.meta.lore(list);
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) this.item.setItemMeta(this.meta);
        return this.item;
    }
}