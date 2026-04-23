package bot.stock.stobot.bot.features;

import bot.stock.stobot.bot.core.CommandsProvider;
import bot.stock.stobot.services.API.AnilistService;
import bot.stock.stobot.utils.Embed;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class SearchCommands extends ListenerAdapter implements CommandsProvider.PublicSlashCommand {

    private final AnilistService anilist;
    private final Embed embed;

    public SearchCommands(AnilistService anilist, Embed embed) {
        this.anilist = anilist;
        this.embed = embed;
    }

    @Override
    public CommandData command() {
        return Commands.slash("search", "Get manga info")
                .addOption(OptionType.STRING, "name", "Manga name", true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("search")) return;

        String search = event.getOption("name").getAsString();
        event.deferReply().queue();
        log.info("User searched for '{}'", search);

        anilist.searchManga(search)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        manga -> event.getHook()
                                .editOriginalEmbeds(embed.buildEmbedFromManga(manga))
                                .queue(),
                        error -> event.getHook()
                                .editOriginal(anilist.handleError(error, search))
                                .queue()
                );
    }


}