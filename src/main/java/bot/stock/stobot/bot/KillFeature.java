package bot.stock.stobot.bot;

import bot.stock.stobot.interfaces.SlashCommandProvider;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class KillFeature  extends ListenerAdapter{
    public CommandData command() {
        return Commands.slash("kill", "kill the bot");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("kill")) return;
        event.reply("goodbye world").setEphemeral(false).queue();
        event.getJDA().shutdown();
        System.exit(0);
    }
}
