package bot.stock.stobot.bot.features;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import bot.stock.stobot.bot.core.CommandsProvider;
import bot.stock.stobot.utils.Embed;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;

@Slf4j
@Component
public class Meme extends ListenerAdapter implements CommandsProvider.PublicSlashCommand{
    
    private final Embed eb;
    private final Map<String, Resource> memeMap = new HashMap<>();

    public Meme(Embed eb){
        this.eb = eb;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:meme/*");

            for (Resource resource : resources) {
                String name = resource.getFilename();
                if(name != null){
                    memeMap.put(name.substring(0,name.lastIndexOf(".")), resource);
                    log.info("Loaded meme {}",name);
                }
            } 
            log.info("Loaded {} memes: {}", memeMap.size(), memeMap.keySet());
            
        } catch (IOException e) {
            log.error("Failed to load memes: {}", e.getMessage());
        }
    }

    @Override
    public CommandData command() {
        return Commands.slash("meme", "publie a saved meme")
                .addOption(OptionType.STRING, "name", "the name of the meme", true,true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("meme")) return;
        String nameStr = event.getOption("name").getAsString();
        Resource resource = memeMap.get(nameStr);

        if(resource == null){
            event.reply("Meme `" + nameStr + "` introuvable. Disponibles : `"
                    + String.join("`, `", memeMap.keySet()) + "`")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply().queue();

        try{
            String filename = resource.getFilename();
            FileUpload file = FileUpload.fromData(resource.getInputStream(), filename);
            event.getHook()
                .sendFiles(Collections.singleton(file))
                .addEmbeds(eb.buildEmbedFromMeme(filename))
                .queue();
                
        }catch(IOException e){
            log.error("Erreur lors de l'envoi du meme '{}': {}", nameStr, e.getMessage());
            event.getHook().editOriginal("Erreur lors de l'envoi du meme.").queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("meme")) return;
        
        String focused = event.getFocusedOption().getValue().toLowerCase();
        List<Command.Choice> choices = memeMap.keySet().stream()
                .filter(name -> name.toLowerCase().startsWith(focused))
                .sorted()
                .limit(25)
                .map(name -> new Command.Choice(name, name))
                .collect(Collectors.toList());

        event.replyChoices(choices).queue();
    }

}
