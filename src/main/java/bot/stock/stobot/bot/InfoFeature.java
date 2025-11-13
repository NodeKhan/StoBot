package bot.stock.stobot.bot;

import bot.stock.stobot.interfaces.SlashCommandProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import bot.stock.stobot.services.AnilistService;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class InfoFeature extends ListenerAdapter implements SlashCommandProvider {

    private final AnilistService anilist;

    public InfoFeature(AnilistService anilist) {
        this.anilist = anilist;
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

        event.deferReply().queue();


        anilist.searchManga(search)
                // If nothing emits in 5 seconds, trigger TimeoutException
                .timeout(Duration.ofSeconds(5))
                .subscribe(media -> {
                    
                    if (media == null) {
                        event.getHook().sendMessage("No results found.").queue();
                        return;
                    }

                    String title = firstNonNull(
                            media.title().english(),
                            media.title().romaji()
                    );

                    String synonyms = media.synonyms() != null && !media.synonyms().isEmpty()
                            ? String.join(", ", media.synonyms())
                            : "None";

                    if (!media.title().romaji().equals(title)) {
                        if (synonyms.equals("None")) synonyms = media.title().romaji();
                        else synonyms += ", " + media.title().romaji();
                    }

                    String desc = media.description() != null
                            ? media.description().replaceAll("<.+?>", "")
                            : "No description available.";

                    if (desc.length() > 800) desc = desc.substring(0, 800) + "...";

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(title)
                            .setThumbnail(media.coverImage().large())
                            .setColor(Color.decode("#7851a9"))
                            .addField("Status: ", media.status().toLowerCase().strip(), true)
                            .addField("Alternative Names:", synonyms, false)
                            .addField("Summary:", desc, false);

                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                }, throwable -> {
                    String msg = (throwable instanceof TimeoutException)
                            ? "Search timed out after 5 seconds. Please try again."
                            : "An unexpected error occurred while searching.";
                    event.getHook().sendMessage(msg).queue();
                });
    }

    private String firstNonNull(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "Unknown";
    }
}
