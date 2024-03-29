package dev.oakleycord.simpleverify;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Objects;

public class GuildVerifyOptions implements Cloneable {
    private final long guildID;
    private String verifyMessage;
    private long channelID, logChannelID, roleID, removeRoleID;

    public GuildVerifyOptions(Long guildID, String verifyMessage, Long channelID, long roleID) {
        this.guildID = guildID;
        this.verifyMessage = verifyMessage;
        this.channelID = channelID;
        this.roleID = roleID;
    }

    public Guild getGuild(JDA jda) {
        return jda.getGuildById(this.getGuildID());
    }

    public Role getRole(JDA jda) {
        Guild guild = jda.getGuildById(this.getGuildID());
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getRoleById(this.getRoleID());
    }

    public Role getRemoveRole(JDA jda) {
        Guild guild = jda.getGuildById(this.getGuildID());
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getRoleById(this.getRemoveRoleID());
    }

    public TextChannel getChannel(JDA jda) {
        Guild guild = jda.getGuildById(this.getGuildID());
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getTextChannelById(this.getChannelID());
    }

    public TextChannel getLogChannel(JDA jda) {
        Guild guild = jda.getGuildById(this.getGuildID());
        if (guild == null)
            throw new IllegalArgumentException();
        return guild.getTextChannelById(this.getLogChannelID());
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

    public long getLogChannelID() {
        return logChannelID;
    }

    public void setLogChannelID(Long channelID) {
        this.logChannelID = channelID;
    }
    public String getVerifyMessage() {
        return verifyMessage;
    }

    public void setVerifyMessage(String verifyMessage) {
        this.verifyMessage = verifyMessage;
    }

    public long getRemoveRoleID() {
        return removeRoleID;
    }

    public void setRemoveRoleID(long roleID) {
        this.removeRoleID = roleID;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildVerifyOptions options = (GuildVerifyOptions) o;
        return getGuildID() == options.getGuildID() && getChannelID() == options.getChannelID() && getLogChannelID() == options.getLogChannelID() && getRoleID() == options.getRoleID() && getRemoveRoleID() == options.getRemoveRoleID() && Objects.equals(getVerifyMessage(), options.getVerifyMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGuildID(), getVerifyMessage(), getChannelID(), getLogChannelID(), getRoleID(), getRemoveRoleID());
    }

    @Override
    public GuildVerifyOptions clone() {
        try {
            return (GuildVerifyOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
