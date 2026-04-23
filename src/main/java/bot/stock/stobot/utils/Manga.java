package bot.stock.stobot.utils;
import java.util.List;

public record Manga(

    String title,
    List<String> altTitles,

    MediaStatus status,
    String format,
    int chapter,
    
    String coverUrl,
    String description,

    int anilistId
){}