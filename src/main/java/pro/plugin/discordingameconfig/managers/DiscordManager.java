package pro.plugin.discordingameconfig.managers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pro.plugin.discordingameconfig.listeners.DiscordBotListener;

public class DiscordManager {
    private static JDA jda;

    public static void init() {
        if (jda != null) { jda.shutdownNow(); jda = null; }
        String token = ConfigManager.getStr("bot-token");
        if (token == null || token.isEmpty() || token.equals("YOUR_BOT_TOKEN")) return;
        try { jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES).addEventListeners(new DiscordBotListener()).build(); }
        catch (Exception ignored) {}
    }

    public static void shutdown() { if (jda != null) jda.shutdownNow(); }

    public static void sendSimple(String text) {
        if (jda == null) return;
        TextChannel ch = jda.getTextChannelById(ConfigManager.getStr("channel-id"));
        if (ch != null) ch.sendMessage(text).queue();
    }

    public static void sendAlertWithButtons(String text, String player, String punishedType) {
        if (jda == null) return;
        TextChannel ch = jda.getTextChannelById(ConfigManager.getStr("channel-id"));
        if (ch != null) {
            Button muteBtn = "mute".equals(punishedType) ? Button.success("unmute:" + player, ConfigManager.getMsg("discord-btn-unmute")) : Button.danger("mute:" + player, ConfigManager.getMsg("discord-btn-mute"));
            Button banBtn = "ban".equals(punishedType) ? Button.success("unban:" + player, ConfigManager.getMsg("discord-btn-unban")) : Button.danger("ban:" + player, ConfigManager.getMsg("discord-btn-ban"));
            Button ignoreBtn = Button.secondary("ignore:" + player, ConfigManager.getMsg("discord-btn-ignore"));
            ch.sendMessage(text).addActionRow(muteBtn, banBtn, ignoreBtn).queue();
        }
    }
}