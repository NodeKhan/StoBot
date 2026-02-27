package bot.stock.stobot.bot.features.search;

import bot.stock.stobot.bot.core.CommandsProvider;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Component
public class SearchListener extends ListenerAdapter implements CommandsProvider.PublicSlashCommand {

    private final SearchService service;

    public SearchListener(SearchService service) {
        this.service = service;
    }

    @Override
    public CommandData command() {
        return Commands.slash("search","get manga info")
                .addOption(OptionType.STRING, "name", "Manga name", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("search")) return;
        String search = event.getOption("name").getAsString();
        event.deferReply().submit()
                .thenAccept(hook -> service.process(search, hook));
    }
}
