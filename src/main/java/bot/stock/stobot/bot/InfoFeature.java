package bot.stock.stobot.bot;

import bot.stock.stobot.interfaces.SlashCommandProvider;
import bot.stock.stobot.services.MangaTitlesService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Component;
import bot.stock.stobot.services.AnilistService;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class InfoFeature extends ListenerAdapter implements SlashCommandProvider {

    private final AnilistService anilist;
    private final MangaTitlesService mts;

    public InfoFeature(AnilistService anilist, MangaTitlesService mts) {
        this.anilist = anilist;
        this.mts = mts;
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
                .timeout(Duration.ofSeconds(5))
                .subscribe(media -> {

                    String title = firstNonNull(
                            media.title().english(),
                            media.title().romaji()
                    );
                    String synonyms = "";
                    if(media.synonyms() != null && !media.synonyms().isEmpty()){
                        List<String> filteredSynonime = media.synonyms().stream().filter(str -> str.matches("^[a-zA-Z0-9\\s'!?:.,-]+")).toList();
                        synonyms = String.join("\n", filteredSynonime);
                    }
                    if (!media.title().romaji().equals(title)) {
                        if (synonyms.isEmpty()) synonyms = media.title().romaji();
                        else synonyms += "\n" + media.title().romaji();
                    }

                    String desc = media.description() != null
                            ? media.description().replaceAll("<.+?>", "").replaceAll("\\s*\\(Source: [^)]+\\)", "")
                            : "No description available.";

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(title)
                            .setThumbnail(media.coverImage().large())
                            .setColor(Color.decode("#7851a9"))
                            .addField("Status:", media.status().toLowerCase().strip(), true);

                    if(media.chapters() != 0){
                        embed.addField("Chapter:", "" + media.chapters(), true);
                    }
                    if(!synonyms.isEmpty()){
                        embed.addField("Alternative Names:", synonyms, false);
                    }
                    embed.addField("Summary:", desc, false);

                    event.getHook().sendMessageEmbeds(embed.build()).queue();

                    // add information into the DB
                    mts.addTitle(media.id(),title,true);
                    for(String s: synonyms.split("\n")){
                        mts.addTitle(media.id(),s,false);
                    }


                }, throwable -> {
                    String msg = "";
                    if (throwable instanceof GraphQlTransportException transport &&
                            transport.getCause() instanceof WebClientResponseException ex &&
                            ex.getStatusCode().value() == 404) {
                            msg = "\""+ search + "\" not found";
                    } else if (throwable instanceof TimeoutException) {
                        msg = "Search timed out after 5 seconds. Please try again.";
                    }else
                        msg = "An unexpected error occurred while searching.";

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
