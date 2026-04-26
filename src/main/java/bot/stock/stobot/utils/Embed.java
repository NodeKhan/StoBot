package bot.stock.stobot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

import org.springframework.stereotype.Component;

import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.UserMedia;

@Component
public class Embed {
    private final Color embedColor = new Color(0x038C73);

    public EmbedBuilder setupBuilder(){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(embedColor);
        return embed;
    }

    public MessageEmbed buildEmbedFromManga(Manga manga){
        EmbedBuilder embed = setupBuilder();
        embed.setTitle(manga.title());
        embed.setThumbnail(manga.coverUrl());

        if(!manga.altTitles().isEmpty()){
            embed.setDescription(String.join("\n",manga.altTitles()));
        }

        embed.addField("Status:", manga.status().toString(),true);
        if(manga.chapter() != 0){
            embed.addField("Chapter:", ""+manga.chapter(),true);
        }
        
        embed.addField("Summary:", manga.description(),false);

        return embed.build();
    }

    public MessageEmbed buildEmbedFromMeme(String meme){
        EmbedBuilder embed = setupBuilder();
        embed.setTitle(meme.replace(".png", "").replace("_"," "));
        embed.setImage("attachment://" + meme);
        return embed.build();

    }

    public MessageEmbed buildEmbedFromMedia(Media media) {
        EmbedBuilder embed = setupBuilder();
        embed.setTitle(media.getMainTitle());
        embed.setThumbnail(media.getCoverUrl());

        if(!media.getTitles().isEmpty()){
            
            embed.setDescription(String.join("\n",media.getTitlesToStrings()));
        }

        embed.addField("Status:", media.getStatus().toString(),true);
        if(media.getTotalChapters() != 0){
            embed.addField("Chapter:", ""+media.getTotalChapters(),true);
        }
        return embed.build();
    }

    public MessageEmbed buildEmbedFromUserMedia(UserMedia userMedia) {
        EmbedBuilder embed = setupBuilder();
        Media media = userMedia.getMedia();

        embed.setTitle(media.getMainTitle());
        embed.setThumbnail(media.getCoverUrl());

        if(!media.getTitles().isEmpty()){
            embed.setDescription(String.join("\n",media.getTitlesToStrings()));
        }

        embed.addField("Status:", media.getStatus().toString(),true);
        if(media.getTotalChapters() != 0){
            embed.addField("Chapter:", ""+media.getTotalChapters(),true);
        }
        embed.addField("Current chapter:",""+userMedia.getChaptersRead(),true);
        return embed.build();
    }

}
