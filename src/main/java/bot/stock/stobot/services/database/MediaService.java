package bot.stock.stobot.services.database;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.repository.MediaRepository;
import bot.stock.stobot.utils.Manga;
import bot.stock.stobot.utils.MediaStatus;

@Service

public class MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    public Media addNewMedia(Manga manga){
        Media media = new Media();
        media.setAnilistId(manga.anilistId());
        media.setCoverUrl(manga.coverUrl());
        media.setMainTitle(manga.title());
        media.setStatus(manga.status());
        media.setTotalChapters(manga.chapter());
        media.addTitles(manga.altTitles());
        return mediaRepository.save(media);
    }

    public Media addOrUpdate(Manga manga){
        return mediaRepository.findByMainTitleIgnoreCase(manga.title())
            .map(existing -> updateMedia(existing, manga))
            .orElseGet(() -> addNewMedia(manga));
    }

    private Media updateMedia(Media media, Manga manga){
        media.setStatus(manga.status());
        media.setCoverUrl(manga.coverUrl());
        media.setTotalChapters(manga.chapter());
        return mediaRepository.save(media);
    }

    public List<Media> searchByTitle(String title){
        return mediaRepository.searchByTitle(title);
    }
    public Optional<Media> findById(Long id) {
    return mediaRepository.findById(id);
    }

    public Media createManual(String title, Integer totalChapters) {
        Media media = new Media();
        media.setMainTitle(title);
        media.setTotalChapters(totalChapters);
        media.setStatus(MediaStatus.UNKNOWN);
        return mediaRepository.save(media);
    }
}