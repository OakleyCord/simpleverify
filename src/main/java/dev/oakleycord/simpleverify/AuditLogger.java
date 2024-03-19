package dev.oakleycord.simpleverify;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class AuditLogger {

    public static void logVerified(JDA jda, GuildVerifyOptions options, User user) {

        TextChannel channel = options.getLogChannel(jda);

        if (channel == null)
            return;

        channel.sendMessage("User " + user.getName() + " was verified, DiscordID: " + user.getId()).queue();
    }
}
