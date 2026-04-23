package bot.stock.stobot.bot.features.manga;

import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.UserMedia.ReadingStatus;
import bot.stock.stobot.services.database.MediaService;
import bot.stock.stobot.services.database.UserMediaService;
import bot.stock.stobot.utils.Manga;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class MangaInteractionHandler extends ListenerAdapter {

    private final MediaService mediaService;
    private final UserMediaService userMediaService;
    private final Map<Long, Manga> pendingAnilist;
    private final Map<Long, MangaCommands.Updater> pendingUpdate;

    public MangaInteractionHandler(
        MediaService mediaService,
        UserMediaService userMediaService,
        MangaCommands mangaCommands
    ) {
        this.mediaService = mediaService;
        this.userMediaService = userMediaService;
        this.pendingAnilist = mangaCommands.getPendingAnilist();
        this.pendingUpdate = mangaCommands.getPendingUpdate();
    }

    // -------------------------------------------------------------------------
    // Boutons
    // -------------------------------------------------------------------------

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("manga:")) return;

        long userId = event.getUser().getIdLong();

        // Add manga to the personnal list
        if(id.startsWith("manga:add-local:")){
            long mediaId = Long.parseLong(id.split(":")[2]);
            mediaService.findById(mediaId).ifPresentOrElse(
            media -> {
                userMediaService.addToList(userId, media, ReadingStatus.PLAN_TO_READ);
                event.editMessage("**" + media.getMainTitle() + "** added to your list")
                    .setComponents().queue();
                log.info("[manga add-local] user={} mediaId={}", userId, mediaId);
            },
            () -> event.editMessage("Work not found.").setComponents().queue()
            );
            return;
        }

        switch (id) {

            // add the waiting aniliste entry
            case "manga:add-anilist" -> {
                Manga manga = pendingAnilist.remove(userId);
                if (manga == null) {
                    event.editMessage("Session expirée, refais `/manga add`.").setComponents().queue();
                    return;
                }

                Media media = mediaService.addOrUpdate(manga);
                userMediaService.addToList(userId, media, ReadingStatus.PLAN_TO_READ);
                event.editMessage("**" + media.getMainTitle() + "** added to your list").setComponents().queue();
                log.info("[manga add-anilist] user={} anilistId={}", userId, manga.anilistId());
            }

            // Ouvrir le modal d'ajout manuel
            case "manga:add-manual"  -> handleOpenManualModal(event);
            
            // Annuler
            case "manga:cancel" -> {
                pendingAnilist.remove(userId);
                pendingUpdate.remove(userId);
                event.editMessage("Cancel.").setComponents().queue();
            }

            case "manga:update" ->{
                MangaCommands.Updater updater = pendingUpdate.remove(userId);
                userMediaService.updateChapterUserMedia(updater.userMedia(), updater.chapter());
                log.info("[User media update] usermedia={} chapter={}", updater.userMedia(), updater.chapter());
                event.editMessage("**" + updater.userMedia().getMedia().getMainTitle() + "** updated").setComponents().queue();
            }

            default -> {}
        }
    }

    // -------------------------------------------------------------------------
    // Select menu — choix parmi plusieurs résultats locaux
    // -------------------------------------------------------------------------

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("manga:select-add")) return;

        String value = event.getSelectedOptions().get(0).getValue();
        long mediaId = Long.parseLong(value.split(":")[1]);
        long userId = event.getUser().getIdLong();

        mediaService.findById(mediaId).ifPresentOrElse(
            media -> {
                userMediaService.addToList(userId, media, ReadingStatus.PLAN_TO_READ);
                event.editMessage("**" + media.getMainTitle() + "** added to your list")
                    .setComponents().queue();
            },
            () -> event.editMessage("Work not found.").setComponents().queue()
        );
    }

    // -------------------------------------------------------------------------
    // Modal — ajout manuel
    // -------------------------------------------------------------------------

    private void handleOpenManualModal(ButtonInteractionEvent event) {
        pendingAnilist.remove(event.getUser().getIdLong());

        Modal modal = Modal.create("manga:manual-add", "Add manually")
            .addActionRow(
                TextInput.create("manga-title", "Main Title", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMaxLength(512)
                    .build()
            )
            .addActionRow(
                TextInput.create("manga-chapters", "Total chapters (leave blank if unknown)", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setMaxLength(6)
                    .build()
            )
            .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("manga:manual-add")) return;

        long userId = event.getUser().getIdLong();
        String title = event.getValue("manga-title").getAsString().strip();
        String chaptersRaw = event.getValue("manga-chapters").getAsString().strip();

        // Chapitres optionnels
        Integer totalChapters = null;
        if (!chaptersRaw.isEmpty()) {
            try {
                totalChapters = Integer.parseInt(chaptersRaw);
            } catch (NumberFormatException e) {
                event.reply("Nombre de chapitres invalide.").setEphemeral(true).queue();
                return;
            }
        }

        Media media = mediaService.createManual(title, totalChapters);
        userMediaService.addToList(userId, media, ReadingStatus.PLAN_TO_READ);
        event.reply("**" + title + "** ajouté manuellement à ta liste !").setEphemeral(true).queue();
        log.info("[manga manual-add] user={} title={}", userId, title);
    }
}