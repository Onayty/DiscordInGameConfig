package pro.plugin.discordingameconfig.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import pro.plugin.discordingameconfig.managers.ConfigManager;

public class DiscordBotListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] args = event.getComponentId().split(":");
        if (args.length < 2) return;
        String action = args[0], target = args[1];

        if (!event.getMember().hasPermission(Permission.MODERATE_MEMBERS)) {
            event.reply(ConfigManager.getMsg("discord-reply-no-perm")).setEphemeral(true).queue();
            return;
        }

        if (action.equals("ignore")) {
            event.getMessage().delete().queue();
            event.reply(ConfigManager.getMsg("discord-reply-ignored")).setEphemeral(true).queue();
            return;
        }

        Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> {
            String cmd = action.equals("mute") ? ConfigManager.getStr("punishments.mute-cmd") : ConfigManager.getStr("punishments.ban-cmd");
            cmd = cmd.replace("%player%", target)
                    .replace("%duration%", action.equals("mute") ? ConfigManager.getStr("punishments.mute-time") : ConfigManager.getStr("punishments.ban-time"))
                    .replace("%reason%", action.equals("mute") ? ConfigManager.getStr("punishments.mute-reason") : ConfigManager.getStr("punishments.ban-reason"));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });

        event.reply(ConfigManager.getMsg("discord-reply-punished").replace("%player%", target)).queue();
        event.getMessage().delete().queue();
    }
}