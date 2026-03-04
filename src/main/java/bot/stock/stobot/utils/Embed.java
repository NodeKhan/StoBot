package bot.stock.stobot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

import org.springframework.stereotype.Component;

@Component
public class Embed {
    private final Color embedColor = new Color(0x038C73);

    public MessageEmbed buildEmbedFromManga(Manga manga){
        EmbedBuilder embed = new EmbedBuilder();
        
        embed.setTitle(manga.title());
        embed.setThumbnail(manga.coverUrl());
        embed.setColor(embedColor);

        if(!manga.altTitles().isEmpty()){
            embed.setDescription(String.join("\n",manga.altTitles()));
        }

        embed.addField("Status:", manga.status(),true);
        if(manga.chapter() != 0){
            embed.addField("Chapter:", ""+manga.chapter(),true);
        }
        
        embed.addField("Summary:", manga.description(),false);

        return embed.build();
    }

}
