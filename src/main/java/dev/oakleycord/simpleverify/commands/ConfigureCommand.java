package dev.oakleycord.simpleverify.commands;

import dev.oakleycord.simpleverify.GuildVerifyOptions;
import dev.oakleycord.simpleverify.SimpleVerifyMain;
import dev.oakleycord.simpleverify.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigureCommand extends SVSlashCommand {

    private final String MESSAGE_OPTION = "message";
    private final String REMOVE_MESSAGE_OPTION = "removemessage";
    private final String CHANNEL_OPTION = "channel";
    private final String LOG_CHANNEL_OPTION = "logchannel";

    private final String ROLE_OPTION = "role";

    private final String REMOVE_ROLE_OPTION = "removerole";

    private final String CASE_SENSITIVE =  "casesensitive";


    @Override
    public SlashCommandData getCommandData() {
        return super.getCommandData();
    }

    @NotNull
    @Override
    public String getName() {
        return "configure";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Configure SimpleVerify.";
    }

    @NotNull
    @Override
    public OptionData[] getOptions() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, MESSAGE_OPTION, "Adds a message used for verification."),
                new OptionData(OptionType.INTEGER, REMOVE_MESSAGE_OPTION, "Removes a message from the list of messages used for verification, use the number in the list."),
                new OptionData(OptionType.CHANNEL, CHANNEL_OPTION, "The channel to watch for messages.")
                        .setChannelTypes(ChannelType.TEXT),
                new OptionData(OptionType.CHANNEL, LOG_CHANNEL_OPTION, "The channel to log verifications.")
                        .setChannelTypes(ChannelType.TEXT),
                new OptionData(OptionType.ROLE, ROLE_OPTION, "The role to give to verified users."),
                new OptionData(OptionType.ROLE, REMOVE_ROLE_OPTION, "The role to remove once verified."),
                new OptionData(OptionType.BOOLEAN, CASE_SENSITIVE, "Changes if verified messages should be case sensitive.")
        };
    }

    @NotNull
    @Override
    public SlashCommandData getAdditionalData(SlashCommandData commandData) {
        return commandData
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("This command can only be used in Discord Servers.").queue();
            return;
        }

        Member member = event.getMember();
        // just to make sure no commands get somehow run by regular users
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR))
            return;


        String message = null;

        int removeMessage = -1;

        TextChannel channel = null;

        TextChannel logChannel = null;

        Role role = null;

        Role removeRole = null;

        Boolean caseSensitive = null;


        if (event.getOption(MESSAGE_OPTION) != null)
            message = event.getOption(MESSAGE_OPTION).getAsString();

        if (event.getOption(REMOVE_MESSAGE_OPTION) != null)
            removeMessage = event.getOption(REMOVE_MESSAGE_OPTION).getAsInt();

        if (event.getOption(CHANNEL_OPTION) != null)
            channel = event.getOption(CHANNEL_OPTION).getAsChannel().asTextChannel();

        if (event.getOption(LOG_CHANNEL_OPTION) != null)
            logChannel = event.getOption(LOG_CHANNEL_OPTION).getAsChannel().asTextChannel();

        if (event.getOption(ROLE_OPTION) != null)
            role = event.getOption(ROLE_OPTION).getAsRole();

        if (event.getOption(REMOVE_ROLE_OPTION) != null) 
            removeRole = event.getOption(REMOVE_ROLE_OPTION).getAsRole();

        if (event.getOption(CASE_SENSITIVE) != null)
            caseSensitive = event.getOption(CASE_SENSITIVE).getAsBoolean();



        List<GuildVerifyOptions> guilds = SimpleVerifyMain.getGuilds();

        GuildVerifyOptions options;

        if (guilds.stream().anyMatch(opt -> opt.getGuildID() == guild.getIdLong())) {
            options = guilds.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();
        } else {
            if(message == null || channel == null || role == null) {
                event.reply("When configuring for the first time you must include message, channel and role options.").queue();
                return;
            }
            options = new GuildVerifyOptions(guild.getIdLong(), message, channel.getIdLong(), role.getIdLong());
            guilds.add(options);
        }

        GuildVerifyOptions beforeOptions = options.clone();

        if (message != null)
            options.getMutableMessages().add(message);

        if (removeMessage != -1) {
            try {
                options.getMutableMessages().remove(removeMessage - 1);
            } catch (Exception ex) {
                event.reply("Error occurred during removing message, (maybe set number to non existent message)").queue();
            }
        }


        if (channel != null)
            options.setChannelID(channel.getIdLong());

        if (logChannel != null)
            options.setLogChannelID(logChannel.getIdLong());

        if (role != null)
            options.setRoleID(role.getIdLong());

        if (removeRole != null)
            options.setRemoveRoleID(removeRole.getIdLong());

        if (caseSensitive != null)
            options.setCaseSensitive(caseSensitive);

        SimpleVerifyMain.saveGuilds();
        event.replyEmbeds(Util.createDiffMessage(event.getJDA(), beforeOptions, options)).queue();
    }
}
