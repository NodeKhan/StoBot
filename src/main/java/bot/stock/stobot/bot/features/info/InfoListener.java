package bot.stock.stobot.bot.features.info;

import bot.stock.stobot.bot.core.SlashCommandProvider;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Component
public class InfoListener extends ListenerAdapter implements SlashCommandProvider {

    private final InfoService service;

    public InfoListener(InfoService service) {
        this.service = service;
    }

    @Override
    public CommandData command() {
        return Commands.slash("info","get manga info")
                .addOption(OptionType.STRING, "name", "Manga name", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("info")) return;
        String search = event.getOption("name").getAsString();
        event.deferReply().submit()
                .thenAccept(hook -> service.process(search, hook));
    }
}
