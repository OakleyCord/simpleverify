package dev.oakleycord.simpleverify.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

// I love abstractions of abstractions
public abstract class SVSlashCommand {
    public SlashCommandData getCommandData() {
        return this.getAdditionalData(Commands.slash(this.getName(), this.getDescription()))
                .addOptions(this.getOptions());
    }


    @NotNull
    public abstract String getName();
    @NotNull
    public abstract String getDescription();

    @NotNull
    public abstract OptionData[] getOptions();

    @NotNull
    public abstract SlashCommandData getAdditionalData(SlashCommandData commandData);

    public abstract void run(SlashCommandInteractionEvent event);
}
