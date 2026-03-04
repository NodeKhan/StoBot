package bot.stock.stobot.bot.features;

import bot.stock.stobot.bot.core.CommandsProvider;
import bot.stock.stobot.services.AnilistService;
import bot.stock.stobot.utils.Embed;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class SearchCommand extends ListenerAdapter implements CommandsProvider.PublicSlashCommand {

    private final AnilistService anilist;
    private final Embed embed;

    public SearchCommand(AnilistService anilist, Embed embed) {
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
                                .editOriginal(handleError(error, search))
                                .queue()
                );
    }

    private String handleError(Throwable error, String search) {
        if (isNotFound(error)) {
            return "\"%s\" not found on AniList.".formatted(search);
        }
        if (error instanceof TimeoutException) {
            log.warn("Timeout for '{}'", search);
            return "Search timed out after 5 seconds. Please try again.";
        }
        log.error("Unexpected error during search for '{}'", search, error);
        return "An unexpected error occurred while searching.";
    }

    private boolean isNotFound(Throwable error) {
        return error instanceof GraphQlTransportException t
                && t.getCause() instanceof WebClientResponseException ex
                && ex.getStatusCode().value() == 404;
    }
}