package pro.plugin.discordingameconfig.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import pro.plugin.discordingameconfig.listeners.DiscordBotListener;

public class DiscordManager {
    private static JDA jda;

    public static void init() {
        if (jda != null) { jda.shutdownNow(); jda = null; }
        String token = ConfigManager.getStr("bot-token");
        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN")) return;
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(new DiscordBotListener())
                    .build();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[DiscordInGameConfig] Invalid Bot Token or connection error.");
        }
    }

    public static void shutdown() { if (jda != null) jda.shutdownNow(); }

    public static void sendSimple(String text) {
        if (jda == null) return;
        TextChannel ch = jda.getTextChannelById(ConfigManager.getStr("channel-id"));
        if (ch != null) ch.sendMessage(text).queue();
    }

    public static void sendAlertWithButtons(String text, String player) {
        if (jda == null) return;
        TextChannel ch = jda.getTextChannelById(ConfigManager.getStr("channel-id"));
        if (ch != null) {
            ch.sendMessage(text)
                    .addActionRow(
                            Button.danger("mute:" + player, ConfigManager.getMsg("discord-btn-mute")),
                            Button.danger("ban:" + player, ConfigManager.getMsg("discord-btn-ban")),
                            Button.secondary("ignore:" + player, ConfigManager.getMsg("discord-btn-ignore"))
                    ).queue();
        }
    }
}