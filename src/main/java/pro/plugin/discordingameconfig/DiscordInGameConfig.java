package pro.plugin.discordingameconfig;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pro.plugin.discordingameconfig.commands.DiscordConfigCommand;
import pro.plugin.discordingameconfig.listeners.GameListener;
import pro.plugin.discordingameconfig.listeners.MenuListener;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import pro.plugin.discordingameconfig.managers.DiscordManager;
import pro.plugin.discordingameconfig.managers.PacketManager;

public final class DiscordInGameConfig extends JavaPlugin {
    private static DiscordInGameConfig instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.init(this);
        DiscordManager.init();
        try { PacketManager.register(this); } catch (Exception ignored) {}

        getCommand("discordconfig").setExecutor(new DiscordConfigCommand());
        Bukkit.getPluginManager().registerEvents(new GameListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
    }

    @Override
    public void onDisable() { DiscordManager.shutdown(); }

    public static DiscordInGameConfig getInstance() { return instance; }
}