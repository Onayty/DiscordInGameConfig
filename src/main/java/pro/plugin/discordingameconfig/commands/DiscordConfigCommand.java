package pro.plugin.discordingameconfig.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import pro.plugin.discordingameconfig.managers.MenuManager;
import pro.plugin.discordingameconfig.utils.ColorUtil;

public class DiscordConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("discordinlet.admin")) {
            player.sendMessage(ColorUtil.color(ConfigManager.getMsg("no-permission")));
            return true;
        }
        MenuManager.open(player, MenuManager.MenuType.MAIN);
        return true;
    }
}