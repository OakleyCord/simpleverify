package dev.oakleycord.simpleverify;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.List;

public class Util {

    public static MessageEmbed createDiffMessage(JDA jda, GuildVerifyOptions optionsBefore, GuildVerifyOptions optionsAfter) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("SimpleVerify Configuration Difference");
        embed.setColor(Color.ORANGE);

        if(optionsBefore.equals(optionsAfter)) {
            embed.setDescription("No Difference.");
            embed.setColor(Color.GRAY);
            return embed.build();
        }

        if(!optionsBefore.getVerifyMessages().equals(optionsAfter.getVerifyMessages())) {
            optionsBefore.getVerifyMessages().stream().filter(m -> !optionsAfter.getVerifyMessages().contains(m)).forEach( m ->
                    embed.addField("Message", "-" + m, false)
            );
            optionsAfter.getVerifyMessages().stream().filter(m -> !optionsBefore.getVerifyMessages().contains(m)).forEach( m ->
                    embed.addField("Message", "+" + m, false)
            );
        }

        if(optionsBefore.getChannelID() != optionsAfter.getChannelID())
            embed.addField(channelDiff(jda, optionsAfter.getGuildID(), "Channel", optionsBefore.getChannelID(), optionsAfter.getChannelID()));

        if(optionsBefore.getLogChannelID() != optionsAfter.getLogChannelID())
            embed.addField(channelDiff(jda, optionsAfter.getGuildID(), "Log Channel", optionsBefore.getLogChannelID(), optionsAfter.getLogChannelID()));

        if(optionsBefore.getRoleID() != optionsAfter.getRoleID())
            embed.addField(roleDiff(jda, optionsAfter.getGuildID(), "Role", optionsBefore.getRoleID(), optionsAfter.getRoleID()));

        if(optionsBefore.getRemoveRoleID() != optionsAfter.getRemoveRoleID())
            embed.addField(roleDiff(jda, optionsAfter.getGuildID(), "Role", optionsBefore.getRemoveRoleID(), optionsAfter.getRemoveRoleID()));

        if (optionsBefore.isCaseSensitive() != optionsAfter.isCaseSensitive())
            embed.addField("Case Sensitive", optionsBefore.isCaseSensitive() + " -> " + optionsAfter.isCaseSensitive(), false);

        return embed.build();
    }


    private static MessageEmbed.Field channelDiff(JDA jda, long IDGuild, String name, long IDBefore, long IDAfter) {
        Guild guild = jda.getGuildById(IDGuild);

        TextChannel channelBefore = null;
        TextChannel channelAfter = null;
        if (guild != null) {
            channelBefore = guild.getTextChannelById(IDBefore);
            channelAfter = guild.getTextChannelById(IDAfter);
        }

        String before = channelBefore == null ? String.valueOf(IDBefore) : channelBefore.getAsMention();
        String after = channelAfter == null ? String.valueOf(IDAfter) : channelAfter.getAsMention();

        return new MessageEmbed.Field(
                name,
                before + " -> " + after,
                false
        );
    }

    private static MessageEmbed.Field roleDiff(JDA jda, long IDGuild, String name, long IDBefore, long IDAfter) {
        Guild guild = jda.getGuildById(IDGuild);

        Role roleBefore = null;
        Role roleAfter = null;
        if (guild != null) {
            roleBefore = guild.getRoleById(IDBefore);
            roleAfter = guild.getRoleById(IDAfter);
        }

        String before = roleBefore == null ? String.valueOf(IDBefore) : roleBefore.getAsMention();
        String after = roleAfter == null ? String.valueOf(IDAfter) : roleAfter.getAsMention();

        return new MessageEmbed.Field(
                name,
                before + " -> " + after,
                false
        );
    }

    public static MessageEmbed createConfigurationEmbed(JDA jda, GuildVerifyOptions options) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("SimpleVerify Configuration");
        embed.setColor(Color.WHITE);

        List<String> verifyMessages = options.getVerifyMessages();
        TextChannel channel = options.getChannel(jda);
        TextChannel logChannel = options.getLogChannel(jda);
        Role role = options.getRole(jda);
        Role removeRole = options.getRemoveRole(jda);
        boolean caseSensitive = options.isCaseSensitive();

        if (!verifyMessages.isEmpty())
            for (int i = 0; i < verifyMessages.size(); i++)
                embed.addField("Message", (i + 1) + ": " + verifyMessages.get(i), false);
        else embed.addField("Message", "None", false);

        embed.addField("Case Sensitive", Boolean.toString(caseSensitive), false);

        if (channel != null)
            embed.addField("Channel", channel.getAsMention(), false);
        else embed.addField("Channel", "None", false);

        if (logChannel != null)
            embed.addField("Log Channel", logChannel.getAsMention(), false);
        else embed.addField("Log Channel", "None", false);

        if (role != null)
            embed.addField("Role", role.getAsMention(), false);
        else embed.addField("Role", "None", false);

        if (removeRole != null)
            embed.addField("Remove Role", removeRole.getAsMention(), false);
        else embed.addField("Remove Role", "None", false);

        return embed.build();
    }
}
