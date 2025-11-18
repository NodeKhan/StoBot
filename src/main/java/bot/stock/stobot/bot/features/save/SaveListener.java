package bot.stock.stobot.bot.features.save;

import bot.stock.stobot.bot.core.SlashCommandProvider;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class SaveListener extends ListenerAdapter implements SlashCommandProvider {
    @Override
    public CommandData command() {
        return Commands.slash("save", "Saves the user's last-read chapter")
                .addOption(OptionType.STRING, "name", "Manga name", true)
                .addOption(OptionType.INTEGER, "chapter", "Last-read chapter", true);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("save")) return;

        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! `" + gatewayPing + " ms`").setEphemeral(false).queue();

        gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! `" + gatewayPing + " ms`").setEphemeral(false).queue();

        gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! `" + gatewayPing + " ms`").setEphemeral(false).queue();
    }
}
