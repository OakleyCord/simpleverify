package dev.oakleycord.simpleverify;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuildVerifyOptions implements Cloneable {
    private final long guildID;
    private String verifyMessage;
    private List<String> verifyMessages;

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    private boolean caseSensitive;
    private long channelID, logChannelID, roleID, removeRoleID;

    public GuildVerifyOptions(Long guildID, String verifyMessage, Long channelID, long roleID) {
        this.guildID = guildID;
        this.verifyMessage = verifyMessage;
        this.channelID = channelID;
        this.roleID = roleID;
        this.verifyMessages = new ArrayList<>();
        this.caseSensitive = true;
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

    public void initMessages() {
        verifyMessages = new ArrayList<>();
    }

    public List<String> getMutableMessages() {
        return verifyMessages;
    }

    public List<String> getVerifyMessages() {
        return new ArrayList<>(verifyMessages);
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
        GuildVerifyOptions that = (GuildVerifyOptions) o;
        return getGuildID() == that.getGuildID() && isCaseSensitive() == that.isCaseSensitive() && getChannelID() == that.getChannelID() && getLogChannelID() == that.getLogChannelID() && getRoleID() == that.getRoleID() && getRemoveRoleID() == that.getRemoveRoleID() && Objects.equals(getVerifyMessage(), that.getVerifyMessage()) && Objects.equals(getVerifyMessages(), that.getVerifyMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGuildID(), getVerifyMessage(), getVerifyMessages(), isCaseSensitive(), getChannelID(), getLogChannelID(), getRoleID(), getRemoveRoleID());
    }

    @Override
    public GuildVerifyOptions clone() {
        try {
            GuildVerifyOptions options = (GuildVerifyOptions) super.clone();
            options.verifyMessages = new ArrayList<>(verifyMessages);
            return options;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
