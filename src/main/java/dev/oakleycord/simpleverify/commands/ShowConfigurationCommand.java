package dev.oakleycord.simpleverify.commands;

import dev.oakleycord.simpleverify.GuildVerifyOptions;
import dev.oakleycord.simpleverify.SimpleVerifyMain;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ShowConfigurationCommand extends SVSlashCommand {
    @NotNull
    @Override
    public String getName() {
        return "showconfiguration";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Show SimpleVerify Configuration.";
    }

    @NotNull
    @Override
    public OptionData[] getOptions() {
        return new OptionData[0];
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

        List<GuildVerifyOptions> guilds = SimpleVerifyMain.getGuilds();

        if (guilds.stream().noneMatch(opt -> opt.getGuildID() == guild.getIdLong())) {
            event.reply("Your server is not configured yet.").queue();
            return;
        }

        GuildVerifyOptions options = guilds.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();

        JDA jda = event.getJDA();

        String verifyMessage = options.getVerifyMessage();
        TextChannel channel = options.getChannel(jda);
        TextChannel logChannel = options.getLogChannel(jda);
        Role role = options.getRole(jda);
        Role removeRole = options.getRemoveRole(jda);

        // wow, this is horrid
        String message = "Configuration: \n";
        message+= "Message: " + verifyMessage + " \n";
        message+= "Channel: " + channel.getAsMention() + " \n";
        message+= "Log Channel: " + (logChannel == null ? "None" : logChannel.getAsMention()) + "\n";
        message+= "Role: " + role.getName() + " \n";
        message+= "Remove Role: " + (removeRole == null ? "None" : removeRole.getName()) + "\n";

        event.reply(message).queue();
    }
}
