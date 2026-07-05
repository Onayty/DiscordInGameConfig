package pro.plugin.discordingameconfig.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import pro.plugin.discordingameconfig.DiscordInGameConfig;
import pro.plugin.discordingameconfig.managers.ConfigManager;
import java.util.ArrayList;
import java.util.List;

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

        List<Button> newButtons = new ArrayList<>();
        for (Button b : event.getMessage().getActionRows().get(0).getButtons()) {
            if (b.getId().equals(event.getComponentId())) {
                if (action.equals("mute")) newButtons.add(Button.success("unmute:" + target, ConfigManager.getMsg("discord-btn-unmute")));
                else if (action.equals("ban")) newButtons.add(Button.success("unban:" + target, ConfigManager.getMsg("discord-btn-unban")));
                else if (action.equals("unmute")) newButtons.add(Button.danger("mute:" + target, ConfigManager.getMsg("discord-btn-mute")));
                else if (action.equals("unban")) newButtons.add(Button.danger("ban:" + target, ConfigManager.getMsg("discord-btn-ban")));
            } else { newButtons.add(b); }
        }

        event.editComponents(ActionRow.of(newButtons)).queue(v -> {
            event.getHook().sendMessage(ConfigManager.getMsg("discord-reply-punished").replace("%player%", target)).setEphemeral(true).queue();
        });

        Bukkit.getScheduler().runTask(DiscordInGameConfig.getInstance(), () -> {
            String cmd = switch (action) {
                case "mute" -> ConfigManager.getStr("punishments.mute-cmd").replace("%duration%", ConfigManager.getStr("punishments.mute-time")).replace("%reason%", ConfigManager.getStr("punishments.mute-reason"));
                case "ban" -> ConfigManager.getStr("punishments.ban-cmd").replace("%duration%", ConfigManager.getStr("punishments.ban-time")).replace("%reason%", ConfigManager.getStr("punishments.ban-reason"));
                case "unmute" -> ConfigManager.getStr("punishments.unmute-cmd");
                case "unban" -> ConfigManager.getStr("punishments.unban-cmd");
                default -> "";
            };
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", target));
        });
    }
}