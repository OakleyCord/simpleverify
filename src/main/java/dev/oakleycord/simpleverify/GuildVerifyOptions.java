package dev.oakleycord.simpleverify;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GuildVerifyOptions {
    private final long guildID;

    private long channelID, roleID;

    private String verifyMessage;

    public GuildVerifyOptions(Long guildID, Long channelID, long roleID, String verifyMessage) {
        this.guildID = guildID;
        this.channelID = channelID;
        this.roleID = roleID;
        this.verifyMessage = verifyMessage;
    }

    public Guild getGuild(JDA jda) {
        return jda.getGuildById(guildID);
    }

    public Role getRole(JDA jda) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getRoleById(roleID);
    }

    public TextChannel getChannel(JDA jda) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getTextChannelById(channelID);
    }

    public long getGuildID() {
        return guildID;
    }

    public long getChannelID() {
        return channelID;
    }

    public void setChannelID(Long channelID) {
        this.channelID = channelID;
    }

    public String getVerifyMessage() {
        return verifyMessage;
    }

    public void setVerifyMessage(String verifyMessage) {
        this.verifyMessage = verifyMessage;
    }

    public long getRoleID() {
        return roleID;
    }

    public void setRoleID(long roleID) {
        this.roleID = roleID;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);

    }
    public static GuildVerifyOptions fromJson(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, GuildVerifyOptions.class);
    }
}
