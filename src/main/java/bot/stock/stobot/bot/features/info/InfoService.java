package bot.stock.stobot.bot.features.info;

import bot.stock.stobot.services.AnilistService;
import bot.stock.stobot.services.MangaTitlesService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class InfoService {
    private final AnilistService anilist;
    private final MangaTitlesService mts;

    public InfoService(AnilistService anilist, MangaTitlesService mts) {
        this.anilist = anilist;
        this.mts = mts;
    }

    public void process(String search, InteractionHook event) {
        anilist.searchManga(search)
                .timeout(Duration.ofSeconds(5))
                .subscribe(media -> {

                    //extract information
                    String title = media.title().english() != null ? media.title().english() : media.title().romaji();
                    List<String> altTitle = ExtractAltTitle(media, title);

                    //build & send embed
                    EmbedBuilder embed = buildEmbed(media, title, altTitle);
                    event.sendMessageEmbeds(embed.build()).queue();

                    // add information into the DB
                    saveToMangaTitles(title, altTitle, media.id());

                }, throwable -> {
                    event.sendMessage(handleError(throwable,search)).queue();
                });
    }

    private List<String> ExtractAltTitle(AnilistService.MediaResponse media, String title) {
        List<String> altTitle = new ArrayList<>();
        if(media.synonyms() != null && !media.synonyms().isEmpty()){
            for(String s : media.synonyms()){
                if(s.matches("^[a-zA-Z0-9\\s'!?:.,-]+")){
                    altTitle.add(s);
                }
            }
        }
        if (!media.title().romaji().equals(title)) {
            altTitle.add(media.title().romaji());
        }
        return altTitle;
    }

    private EmbedBuilder buildEmbed(AnilistService.MediaResponse media, String title,List<String> altTitle) {
        String desc = media.description() != null
                ? media.description().replaceAll("<.+?>", "").replaceAll("\\s*\\(Source: [^)]+\\)", "")
                : "No description available.";
        System.out.println(media.coverImage().large());
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setThumbnail(media.coverImage().large())
                .setColor(Color.decode("#7851a9"))
                .addField("Status:", media.status().toLowerCase().strip(), true);

        if(media.chapters() != 0){
            embed.addField("Chapter:", "" + media.chapters(), true);
        }
        if(!altTitle.isEmpty()){
            embed.addField("Alternative Names:", String.join("\n",altTitle), false);
        }
        embed.addField("Summary:", desc, false);
        return embed;
    }

    private void saveToMangaTitles(String title, List<String> altTitle, int id) {
        mts.addTitle(id,title,true);
        for(String s: altTitle){
            mts.addTitle(id,s,false);
        }
    }

    private String handleError(Throwable throwable, String search) {
        String msg = "";
        if (throwable instanceof GraphQlTransportException transport &&
                transport.getCause() instanceof WebClientResponseException ex &&
                ex.getStatusCode().value() == 404) {
            msg = "\""+ search + "\" not found";
        } else if (throwable instanceof TimeoutException) {
            msg = "Search timed out after 5 seconds. Please try again.";
        }else
            msg = "An unexpected error occurred while searching.";
        return msg;
    }

}
