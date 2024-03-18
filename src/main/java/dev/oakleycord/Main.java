package dev.oakleycord;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
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
import java.util.Timer;

public class Main extends ListenerAdapter {


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
                        .addOptions(
                                new OptionData(OptionType.CHANNEL, "channel", "The channel to watch for messages.")
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
        if (!GUILDS.stream().anyMatch(opt -> opt.getGuildID() == guild.getIdLong()))
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
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getGuild() == null)
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

        if (!GUILDS.stream().anyMatch(opt -> opt.getGuildID() == guild.getIdLong())) {
            event.reply("Your server is not configured yet.").queue();
            return;
        }

        GuildVerifyOptions options = GUILDS.stream().filter(opt -> opt.getGuildID() == guild.getIdLong()).findFirst().get();


        String message = "Configuration: \n";
        message+= "Message: " + options.getVerifyMessage() + " \n";
        message+= "Channel: #" + options.getChannel(event.getJDA()).getName() + " \n";
        message+= "Role: " + options.getRole(event.getJDA()).getName() + " \n";

        event.reply(message).queue();
    }

    public void configure(SlashCommandInteractionEvent event) {

        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("This command can only be used in Discord Servers.").queue();
            return;
        }

        TextChannel channel = null;

        Role role = null;

        String message = null;


        if (event.getOption("channel") != null) {
            channel = event.getOption("channel").getAsChannel().asTextChannel();
        }

        if (event.getOption("role") != null) {
            role = event.getOption("role").getAsRole();
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

            if (role != null)
                options.setRoleID(role.getIdLong());

            saveGuilds();
        } else {

            if(message == null || channel == null || role == null) {
                event.reply("When configuring for the first time you must include all options.").queue();
                return;
            }

            GuildVerifyOptions options = new GuildVerifyOptions(guild.getIdLong(), channel.getIdLong(), role.getIdLong(), message);
            GUILDS.add(options);

            saveGuilds();
        }

        event.reply("Configuration successfully updated!").queue();
    }

}