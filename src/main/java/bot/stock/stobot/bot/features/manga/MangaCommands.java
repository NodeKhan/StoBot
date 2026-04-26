package bot.stock.stobot.bot.features.manga;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import bot.stock.stobot.bot.core.CommandsProvider;
import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.UserMedia;
import bot.stock.stobot.services.API.AnilistService;
import bot.stock.stobot.services.database.MediaService;
import bot.stock.stobot.services.database.UserMediaService;
import bot.stock.stobot.utils.Embed;
import bot.stock.stobot.utils.Manga;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@Slf4j
@Component
public class MangaCommands extends ListenerAdapter implements CommandsProvider.PublicSlashCommand {

    private final AnilistService anilist;
    private final Embed embed;
    private final MediaService mediaService;
    private final Map<Long, Manga> pendingAnilist = new ConcurrentHashMap<>();
    private final Map<Long, Updater> pendingUpdate = new ConcurrentHashMap<>();
    private final UserMediaService userMediaService; 

    public MangaCommands(AnilistService anilist,Embed embed, MediaService mediaService, UserMediaService userMediaService){
        this.anilist = anilist;
        this.embed = embed;
        this.mediaService = mediaService;
        this.userMediaService = userMediaService;
    }


    @Override
    public CommandData command() {
        return Commands.slash("manga", "Manage your manga/webtoon/manhua list")
            .addSubcommands(
                new SubcommandData("add","Add a work to your liste")
                    .addOption(OptionType.STRING, "title", "Title of the work", true),
                
                new SubcommandData("update", "Update your number of chapters read")
                    .addOption(OptionType.STRING, "title", "Title of the work", true)
                    .addOption(OptionType.INTEGER, "chapter", "Number of the last chapter read", true),
                
                new SubcommandData("info", "Show your personnal progress for a work")
                    .addOption(OptionType.STRING, "title", "Title of the work", true),
                
                new SubcommandData("list", "Show your reading list"),

                new SubcommandData("suggest", "Suggest a work for your reading list"),

                new SubcommandData("rate", "Rate a work for you futur suggestion"),

                new SubcommandData("drop", "Drop a title of you reading list (it will be conservated but never suggested)")
            );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("manga")) return;
        
        String sub = event.getSubcommandName();
        if(sub == null) return;

        switch (sub) {
            case "add"      -> handleAdd(event);
            case "update"   -> handleUpdate(event);
            case "info"     -> handleInfo(event);
            case "list"     -> handleList(event);
            case "suggest"  -> handleSuggest(event);
            case "rate"     -> handleRate(event);
            case "drop"     -> handleDrop(event);
            default     -> event.reply("sub-command unknown.").setEphemeral(true).queue();
        }

    }
    
    private void handleAdd(SlashCommandInteractionEvent event){
        String title = event.getOption("title", OptionMapping::getAsString);
        long userId = event.getUser().getIdLong();
        log.info("[manga add] user={} title={}", userId, title);
        event.deferReply().setEphemeral(true).queue();

        List<Media> localResults = mediaService.searchByTitle(title);
        if(localResults.size() == 1){
            Media media = localResults.get(0);

            event.getHook().editOriginalEmbeds(embed.buildEmbedFromMedia(media))
                .setActionRow(
                    Button.success("manga:add-local:" + media.getId(), "Add"),
                    Button.danger("manga:cancel", "Cancel")
                ).queue();
        }

        else if(localResults.size() > 1){
            List<SelectOption> options = localResults.stream()
                .limit(25)
                .map(m -> SelectOption.of(m.getMainTitle(), "local:" + m.getId()))
                .toList();

            event.getHook().editOriginal("Several works match, which one do you want to add?")
            .setActionRow(
                StringSelectMenu.create("manga:select-add")
                    .addOptions(options)
                    .build()
            ).queue();
        }
        
        else{
            anilist.searchManga(title)
            .timeout(Duration.ofSeconds(5))
            .subscribe(
                manga -> {
                    event.getHook().editOriginalEmbeds(embed.buildEmbedFromManga(manga))
                        .setActionRow(
                            Button.success("manga:add-anilist", "Add"),
                            Button.secondary("manga:add-manual", "Other"),
                            Button.danger("manga:cancel", "Cancel")
                        ).queue();
                    pendingAnilist.put(userId, manga);
                },

                error -> {
                    String msg = anilist.handleError(error, title);
                    event.getHook().editOriginal(msg)
                        .setActionRow(
                            Button.secondary("manga:add-manual", "Other"),
                            Button.danger("manga:cancel", "Cancel")
                        ).queue();
                }
            );
        }
    }
    
    private void handleUpdate(SlashCommandInteractionEvent event){
        String title = event.getOption("title", OptionMapping::getAsString);
        long userId = event.getUser().getIdLong();
        int chapter = event.getOption("chapter",0, OptionMapping::getAsInt);
        log.info("[manga update] user={} titre={} chapitre={}", userId, title, chapter);
        event.deferReply().setEphemeral(true).queue();

        UserMedia userMedia = userMediaService.findUserMedia(userId, title);
        if(userMedia == null){
            event.getHook().editOriginal("Title not registered, do /manga add title first").queue();
            return;
        }

        pendingUpdate.put(userId,new Updater(userMedia, chapter));
        event.getHook().editOriginalEmbeds(embed.buildEmbedFromUserMedia(userMedia))
            .setActionRow(                            
                Button.success("manga:update", "Update"),
                Button.danger("manga:cancel", "Cancel"))    
            .queue();
    }
    
    private void handleInfo(SlashCommandInteractionEvent event){
        String title = event.getOption("title", OptionMapping::getAsString);
        long userId = event.getUser().getIdLong();
        event.deferReply().setEphemeral(true).queue();

        UserMedia userMedia = userMediaService.findUserMedia(userId, title);
        if(userMedia == null){
            event.getHook().editOriginal("Title not registered, do /manga add title first").queue();
            return;
        }
        event.getHook().editOriginalEmbeds(embed.buildEmbedFromUserMedia(userMedia)).queue();
    }
    
    private void handleList(SlashCommandInteractionEvent event){
        long userId = event.getUser().getIdLong();
        event.deferReply().setEphemeral(true).queue();
        List<UserMedia> userMedias = userMediaService.findAllUserMediaForUser(userId);
        if(userMedias.isEmpty()){
            event.getHook().editOriginal("No registered manga").queue();
            return;
        }

        event.getHook().editOriginal(buildTable(userMedias)).queue();

    }

    private String buildTable(List<UserMedia> userMedias) {
        StringBuilder sb = new StringBuilder();
        sb.append("```diff\n");
        sb.append(String.format("%-2s %-25s %-15s %s%n", "", "Titre", "Status", "Chap"));
        sb.append("  ").append("─".repeat(47)).append("\n");

        for (UserMedia um : userMedias) {
            String prefix = switch (um.getReadingStatus()) {
                case READING      -> " ";
                case DROPPED      -> "-";
                case COMPLETED    -> "+";
                case PLAN_TO_READ -> "#";
            };

            sb.append(String.format("%-2s %-25s %-15s %d%n",
                prefix,
                truncate(um.getMedia().getMainTitle(), 25),
                um.getReadingStatus().display(),
                um.getChaptersRead()
            ));
        }

        sb.append("```");
        return sb.toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
    
    private void handleSuggest(SlashCommandInteractionEvent event){
        // TODO
        event.deferReply().setEphemeral(true).queue();
        event.getHook().editOriginal("not implemented yet").queue();
    }
    
    private void handleRate(SlashCommandInteractionEvent event){
        // TODO
        event.deferReply().setEphemeral(true).queue();
        event.getHook().editOriginal("not implemented yet").queue();
    }
    
    private void handleDrop(SlashCommandInteractionEvent event){
        // TODO
        event.deferReply().setEphemeral(true).queue();
        event.getHook().editOriginal("not implemented yet").queue();
    }


    public Map<Long, Manga> getPendingAnilist() {
        return pendingAnilist;
    }

    public Map<Long,Updater> getPendingUpdate(){
        return pendingUpdate;
    }

    public record Updater(
        UserMedia userMedia,
        int chapter
    ){}

}
