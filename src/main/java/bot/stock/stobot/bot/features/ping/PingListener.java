package bot.stock.stobot.bot.features.ping;

import bot.stock.stobot.bot.core.SlashCommandProvider;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class PingListener extends ListenerAdapter implements SlashCommandProvider {
    @Override
    public CommandData command() {
        return Commands.slash("ping", "Check the bot latency");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ping")) return;

        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! `" + gatewayPing + " ms`").setEphemeral(false).queue();

        System.out.println("PingListener received event: " + event.getName());
    }
}

