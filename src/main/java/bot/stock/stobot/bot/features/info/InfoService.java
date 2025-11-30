package bot.stock.stobot.bot.features.info;

import bot.stock.stobot.database.manga.MangaAltTitles;
import bot.stock.stobot.database.manga.MangaData;
import bot.stock.stobot.services.AnilistService;
import bot.stock.stobot.services.MangaAltTitlesService;
import bot.stock.stobot.services.MangaDataService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Value;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class InfoService {
    private final AnilistService anilist;
    private final MangaAltTitlesService mts;
    private final MangaDataService mangaDataService;

    @Value("${discord.color}")
    private String discordColor;

    public InfoService(AnilistService anilist, MangaAltTitlesService mts, MangaDataService mangaDataService) {
        this.anilist = anilist;
        this.mts = mts;
        this.mangaDataService = mangaDataService;
    }

    public void process(String search, InteractionHook event) {
        anilist.searchManga(search)
                .timeout(Duration.ofSeconds(5))
                .subscribe(media -> {

                    //build & send embed
                    EmbedBuilder embed = buildEmbed(media);
                    event.sendMessageEmbeds(embed.build()).queue();

                    // add information into the DB
                    saveToMangaTitles(media);

                }, throwable -> {
                    event.sendMessage(handleError(throwable,search)).queue();
                });
    }

    private EmbedBuilder buildEmbed(AnilistService.MangaRecord media) {


        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(media.title())
                .setThumbnail(media.coverUrl())
                .setColor(Color.decode(discordColor))
                .addField("Status:", media.status().toLowerCase().strip(), true);

        if(media.chapters() != 0){
            embed.addField("Chapter:", "" + media.chapters(), true);
        }
        if(!media.altTitles().isEmpty()){
            embed.addField("Alternative Names:", String.join("\n",media.altTitles()), false);
        }
        embed.addField("Summary:", media.description(), false);
        return embed;
    }

    private void saveToMangaTitles(AnilistService.MangaRecord media) {

        MangaData md = ! mangaDataService.existsByTitle(media.title()) ?
                mangaDataService.newMangaData(media.title()) :
                mangaDataService.findMangaDataByTitle(media.title());

        List<String> altTitles = mts.getAllTitleByMangaId(md);

        for(String s: media.altTitles()){
            if(! altTitles.contains(s)){
                mts.addMangaAltTitles(md, s);
            }
        }
    }

    private String handleError(Throwable throwable, String search) {
        String msg = "";
        if (throwable instanceof GraphQlTransportException transport &&
                transport.getCause() instanceof WebClientResponseException ex &&
                ex.getStatusCode().value() == 404) {
            msg = "\""+ search + "\" not found on anilist";
        } else if (throwable instanceof TimeoutException) {
            msg = "Search timed out after 5 seconds. Please try again.";
        }else
            msg = "An unexpected error occurred while searching.";
        return msg;
    }

}
