package dev.oakleycord.simpleverify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Main extends ListenerAdapter {


    //TODO: use maps instead of list
    private static final List<GuildVerifyOptions> GUILDS = new ArrayList<>();
    private static final File GUILDS_FILE = new File("guilds.json");

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv
                .configure()
                .ignoreIfMissing()
                .load();
        loadGuilds();

        String token = dotenv.get("BOT_TOKEN");

        if (token == null)
            throw new IllegalArgumentException("BOT_TOKEN must not be null");



        JDA jda = JDABuilder.createLight(token, EnumSet.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                ))
                .addEventListeners(new Main())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("configure", "Configure Verify Options.")
                        .addOption(OptionType.STRING, "message", "The message used for verification.")
                        .addOption(OptionType.ROLE, "role", "The role to give to verified users.")
                        .addOption(OptionType.ROLE, "removerole", "The role to remove once verified.")
                        .addOptions(
                                new OptionData(OptionType.CHANNEL, "channel", "The channel to watch for messages.")
                                        .setChannelTypes(ChannelType.TEXT)
                        )
                        .addOptions(
                                new OptionData(OptionType.CHANNEL, "logchannel", "The channel to log verifications.")
                                        .setChannelTypes(ChannelType.TEXT)
                        )
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("showconfiguration", "Show Configuration.")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );

        commands.queue();
    }

    public static void loadGuilds() {

        Gson gson = new Gson();

        if (!GUILDS_FILE.exists())
            return;

        try {
            Reader reader = new FileReader(GUILDS_FILE);

            JsonArray guildList = gson.fromJson(reader, JsonArray.class);

            guildList.forEach(guild -> GUILDS.add(GuildVerifyOptions.fromJson(guild.toString())));

        } catch (Exception exception) {
            System.out.println("Could not read guilds file");
        }
    }

    public static void saveGuilds() {

        Gson gson = new Gson();

        try {
            Writer writer = new FileWriter(GUILDS_FILE);

            gson.toJson(GUILDS, writer);

            writer.flush();
            writer.close();
        } catch (Exception exception) {
            System.out.println("Could not write guilds file");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        if (GUILDS.stream().noneMatch(opt -> opt.getGuildID() == guild.getIdLong()))
            return;

        GuildVerifyOptions options = GUILDS.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();

        if (event.getChannel().getIdLong() != options.getChannelID())
            return;

        if (event.getAuthor().isBot())
            return;

        String message = event.getMessage().getContentRaw();

        event.getMessage().delete().queue();

        if (!message.equals(options.getVerifyMessage()))
            return;

        String mention = event.getAuthor().getAsMention();

        event.getGuild().addRoleToMember(event.getAuthor(), options.getRole(event.getJDA())).queue();

        Role removeRole = options.getRemoveRole(event.getJDA());
        if (removeRole != null)
            event.getGuild().removeRoleFromMember(event.getAuthor(), removeRole).queue();

        event.getChannel().sendMessage(mention + " verified!").queue(msg -> {
            try {
                // TODO: this is probably stupid
                synchronized(msg) {
                    msg.wait(2000);
                    msg.delete().queue();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        AuditLogger.logVerified(event.getJDA(), options, event.getAuthor());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null)
            return;

        Member member = event.getMember();
        // just to make sure no commands get somehow run by regular users
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR))
            return;


        switch (event.getName()) {
            case "configure":
                configure(event);
                break;

            case "showconfiguration":
                showConfiguration(event);
                break;
        }

    }

    public void showConfiguration(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("This command can only be used in Discord Servers.").queue();
            return;
        }

        if (GUILDS.stream().noneMatch(opt -> opt.getGuildID() == guild.getIdLong())) {
            event.reply("Your server is not configured yet.").queue();
            return;
        }

        GuildVerifyOptions options = GUILDS.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();

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

    public void configure(SlashCommandInteractionEvent event) {

        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("This command can only be used in Discord Servers.").queue();
            return;
        }

        TextChannel channel = null;

        TextChannel logChannel = null;

        Role role = null;

        Role removeRole = null;

        String message = null;


        if (event.getOption("channel") != null) {
            channel = event.getOption("channel").getAsChannel().asTextChannel();
        }

        if (event.getOption("logchannel") != null) {
            logChannel = event.getOption("logchannel").getAsChannel().asTextChannel();
        }

        if (event.getOption("role") != null) {
            role = event.getOption("role").getAsRole();
        }

        if (event.getOption("removerole") != null) {
            removeRole = event.getOption("removerole").getAsRole();
        }

        if (event.getOption("message") != null) {
            message = event.getOption("message").getAsString();
        }

        if (GUILDS.stream().anyMatch(opt -> opt.getGuildID() == guild.getIdLong())) {

            GuildVerifyOptions options = GUILDS.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();

            if (message != null)
                options.setVerifyMessage(message);

            if (channel != null)
                options.setChannelID(channel.getIdLong());

            if (logChannel != null)
                options.setLogChannelID(logChannel.getIdLong());

            if (role != null)
                options.setRoleID(role.getIdLong());

            if (removeRole != null)
                options.setRemoveRoleID(removeRole.getIdLong());


            saveGuilds();
        } else {

            if(message == null || channel == null || role == null) {
                //TODO: fix this whole thing up so weird stuff like this doesn't happen
                event.reply("When configuring for the first time you must include all options, except remove role.").queue();
                return;
            }

            GuildVerifyOptions options = new GuildVerifyOptions(guild.getIdLong(), channel.getIdLong(), role.getIdLong(), message);
            GUILDS.add(options);

            saveGuilds();
        }

        event.reply("Configuration successfully updated!").queue();
    }

}