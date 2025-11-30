package bot.stock.stobot.bot.features.save;

import bot.stock.stobot.database.manga.MangaData;
import bot.stock.stobot.database.manga.MangaRegister;
import bot.stock.stobot.services.AnilistService;
import bot.stock.stobot.services.MangaAltTitlesService;
import bot.stock.stobot.services.MangaDataService;
import bot.stock.stobot.services.MangaRegisterService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SaveService {

    private final AnilistService anilist;
    private final MangaRegisterService mreg;
    private final MangaDataService mdata;

    private final Map<Long, SaveSession> sessions = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private record SaveSession(InteractionHook hook, List<MangaData> mangas, int chapter,
                               ScheduledFuture<?> timeoutTask) {
    }

    @Value("${discord.color}")
    private String discordColor;

    public SaveService(AnilistService anilist,
                       MangaRegisterService mreg, MangaDataService mdata) {
        this.anilist = anilist;
        this.mreg = mreg;
        this.mdata = mdata;
    }

    private void endSession(long userId) {
        SaveSession sess = sessions.remove(userId);
        if (sess != null && sess.timeoutTask != null)
            sess.timeoutTask.cancel(false);
    }

    public void process(String name, int chapter, long userid, InteractionHook hook) {

        name = name.trim();
        if (name.length() > 250) name = name.substring(0, 250);
        if (chapter < 0) chapter = 0;

        List<MangaData> mangas = mdata.findAllByTitle(name);
        boolean fullyEqual = false;
        for (MangaData manga : mangas) {
            fullyEqual = mdata.deepCompare(name,manga);
        }

        if (mangas.isEmpty() || !fullyEqual) {
            System.out.println("entre if");
            try {
                AnilistService.MangaRecord result = anilist.searchManga(name)
                        .block(Duration.ofSeconds(5));

                if (result != null) {
                    MangaData newManga = mdata.fromAnilist(result);
                    mangas.add(newManga); // convert to MangaData

                    if(!mdata.deepCompare(name,newManga)) {
                        mangas.add(mdata.tempoMangaData(name));
                    }
                }
            } catch (Exception e) {
                if(e.getMessage() != null && e.getMessage().contains("404 Not Found")) {
                        hook.editOriginal("\nManga not found on AniList").queue();
                }else {
                    hook.editOriginal(hook.retrieveOriginal().toString() + "\nAniList error: " + e.getMessage()).queue();
                }
                mangas.add(mdata.tempoMangaData(name));
            }
        }


        ScheduledFuture<?> timeout = scheduler.schedule(() -> {
            SaveSession sess = sessions.remove(userid);
            if (sess != null) {
                sess.hook.deleteOriginal().queue();
                sess.hook.sendMessage("**Timed out after 10 seconds.**").setEphemeral(true).queue();
            }
        }, 10, TimeUnit.SECONDS);

        sessions.put(userid, new SaveSession(hook, mangas, chapter, timeout));

        showCandidate(userid, 0);
    }

    private void showCandidate(long userId, int index) {

        SaveSession sess = sessions.get(userId);
        if (sess == null) return;

        MangaData manga = sess.mangas.get(index);
        MangaRegister newEntry = mreg.getMangaRegister(userId, manga.getMangaId());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.decode(discordColor));
        StringBuilder sb = new StringBuilder();
        sb.append(newEntry.getTimestamp() == 0 ? "New Entry: " : "Update Entry: ");
        sb.append(manga.getTitle());
        embed.setTitle(sb.toString());

        sb = new StringBuilder();
        if (newEntry.getTimestamp() != 0) {
            sb.append("**From Chapter:** ").append("`").append(newEntry.getChapter()).append("`\n");
        }
        sb.append("**To Chapter:** ").append("`").append(sess.chapter).append("`\n");

        embed.setDescription(sb.toString());
        embed.setFooter("Is this correct?");

        if(sess.mangas.size() == 1){
            sess.hook.editOriginalEmbeds(embed.build())
                    .setActionRow(
                            Button.success("save_confirm_" + userId + "_" + manga.getMangaId()+"_"+sess.chapter, "Confirm"),
                            Button.danger("save_cancel_" + userId, "Cancel")
                    ).queue();
        }else{
            if(index == 0){
                sess.hook.editOriginalEmbeds(embed.build())
                        .setActionRow(
                                Button.success("save_confirm_" + userId + "_" + manga.getMangaId()+"_"+sess.chapter, "Confirm"),
                                Button.primary("save_next_" + userId + "_" + (index+1), "Next"),
                                Button.danger("save_cancel_" + userId, "Cancel")
                        ).queue();
            } else if (sess.mangas.size() == index+1) {
                sess.hook.editOriginalEmbeds(embed.build())
                        .setActionRow(
                                Button.success("save_confirm_" + userId + "_" + manga.getMangaId()+"_"+sess.chapter, "Confirm"),
                                Button.primary("save_next_" + userId + "_" + (index-1), "Previous"),
                                Button.danger("save_cancel_" + userId, "Cancel")
                        ).queue();
            }else{
                sess.hook.editOriginalEmbeds(embed.build())
                        .setActionRow(
                                Button.success("save_confirm_" + userId + "_" + manga.getMangaId()+"_"+sess.chapter, "Confirm"),
                                Button.primary("save_next_" + userId + "_" + (index-1), "Previous"),
                                Button.primary("save_next_" + userId + "_" + (index+1), "Next"),
                                Button.danger("save_cancel_" + userId, "Cancel")
                        ).queue();
            }


        }

    }

    // === Button handlers ===

    public void handleConfirm(ButtonInteractionEvent event, String id) {

        String[] split = id.split("_");
        long userId = Long.parseLong(split[2]);
        long mangaId = Long.parseLong(split[3]);
        int chapter = Integer.parseInt(split[4]);

        if (sessions.get(userId) == null) {
            event.editMessage("❌ Session expired.").queue();
            return;
        }

        if(mangaId == 0){
            List<MangaData> mangas = sessions.get(userId).mangas();
            mangaId = mdata.newMangaData(mangas.get(mangas.size()-1).getTitle()).getMangaId();
        }

        mreg.changeChapter(userId, mangaId, chapter);

        event.deferEdit().queue(); // acknowledge ONE time
        sessions.get(userId).hook.editOriginal("✅ Saved!").setComponents().queue();
        endSession(userId);
    }

    public void handleNext(ButtonInteractionEvent event, String id) {

        String[] split = id.split("_");
        long userId = Long.parseLong(split[2]);
        int next = Integer.parseInt(split[3]);

        SaveSession sess = sessions.get(userId);
        if (sess == null) {
            event.editMessage("Session expired.").setComponents().queue();
            return;
        }

        if (next >= sess.mangas.size()) {

            event.editMessage("❌ No more suggestions.").setComponents().queue();
            event.deferEdit().queue();
            return;

        }
        event.deferEdit().queue();
        showCandidate(userId, next);
    }

    public void handleCancel(ButtonInteractionEvent event, String id) {

        String[] split = id.split("_");
        long userId = Long.parseLong(split[2]);

        event.deferEdit().queue();
        sessions.get(userId).hook.editOriginal("❌ Cancelled.").setComponents().queue();
        endSession(userId);
    }
}
