package bot.stock.stobot.services.database;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.UserMedia;
import bot.stock.stobot.database.UserMedia.ReadingStatus;
import bot.stock.stobot.database.repository.UserMediaRepository;
import bot.stock.stobot.database.repository.MediaRepository;


@Service
public class UserMediaService {

    @Autowired
    private UserMediaRepository userMediaRepository;

    @Autowired
    private MediaRepository mediaRepository;


    public UserMedia addToList(long userId, Media media, ReadingStatus status) {
        // Éviter les doublons
        return userMediaRepository
            .findByDiscordUserIdAndMedia(userId, media)
            .orElseGet(() -> {
                UserMedia um = new UserMedia();
                um.setDiscordUserId(userId);
                um.setMedia(media);
                um.setReadingStatus(status);
                return userMediaRepository.save(um);
            });
    }

    public UserMedia findUserMedia(long userId, String title){
        List<Media> results = mediaRepository.searchByTitle(title);
        
        if(results.isEmpty()) return null;
        return userMediaRepository.findByDiscordUserIdAndMedia(userId, results.get(0))
            .orElse(null);
    }

    public UserMedia updateChapterUserMedia(UserMedia userMedia,int chapter){
        userMedia.setChaptersRead(chapter);
        return userMediaRepository.save(userMedia);
    }

    public List<UserMedia> findAllUserMediaForUser(Long userId){
        return userMediaRepository.findAllByDiscordUserIdWithMedia(userId);
    }
}
