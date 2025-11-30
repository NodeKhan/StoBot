package bot.stock.stobot.services;

import bot.stock.stobot.database.manga.MangaData;
import bot.stock.stobot.database.utils.MangaAltTitlesRepository;
import bot.stock.stobot.database.manga.MangaAltTitles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MangaAltTitlesService {

    @Autowired
    private MangaAltTitlesRepository mangaAltTitlesRepository;

    public MangaAltTitles addMangaAltTitles(MangaData mangaData, String title) {
        MangaAltTitles mangaAltTitles = new MangaAltTitles();
        mangaAltTitles.setMangaId(mangaData);
        mangaAltTitles.setTitle(title);
        return mangaAltTitlesRepository.save(mangaAltTitles);
    }

    public Boolean existTitle(String title){
        return mangaAltTitlesRepository.existsById(title);
    }

    public  List<MangaAltTitles> findByTitle(String title){
        return mangaAltTitlesRepository.findByTitle(title);
    }

    public boolean existsByTitle(String title){
        return mangaAltTitlesRepository.existsByTitle(title);
    }

    public List<String> getAllTitleByMangaId(MangaData mangaId) {
        return mangaAltTitlesRepository.getAllTitleByMangaId(mangaId.getMangaId());
    }

}
