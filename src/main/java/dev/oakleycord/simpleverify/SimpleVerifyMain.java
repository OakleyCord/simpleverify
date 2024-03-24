package dev.oakleycord.simpleverify;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dev.oakleycord.simpleverify.commands.ConfigureCommand;
import dev.oakleycord.simpleverify.commands.SVSlashCommand;
import dev.oakleycord.simpleverify.commands.ShowConfigurationCommand;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class SimpleVerifyMain extends ListenerAdapter {


    private static final List<SVSlashCommand> COMMANDS = List.of(
            new ConfigureCommand(),
            new ShowConfigurationCommand()
    );
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

        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("BOT_TOKEN must not be null");


        JDA jda = JDABuilder.createLight(token, EnumSet.of(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                ))
                .addEventListeners(new SimpleVerifyMain())
                .build();

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            final long start = System.currentTimeMillis();
            @Override
            public void run() {

                long now = System.currentTimeMillis();
                long diff = now - start;
                long seconds = diff / 1000;
                String uptime = String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));

                jda.getPresence().setActivity(Activity.customStatus("Uptime: " + uptime));
            }
        }, 5000,5000);


        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(COMMANDS.stream().map(SVSlashCommand::getCommandData).toList()).queue();

        commands.queue(cmds ->
                cmds.forEach(cmd -> System.out.println("Registered Comamnd: " + cmd.getName()))
        );
    }

    public static List<GuildVerifyOptions> getGuilds(){
        return GUILDS;
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (SVSlashCommand command : COMMANDS) {
            if (command.getName().equals(event.getName())) {
                command.run(event);
                return;
            }
        }
    }
}