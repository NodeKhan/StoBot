package bot.stock.stobot.services;

import bot.stock.stobot.database.manga.MangaAltTitles;
import bot.stock.stobot.database.manga.MangaData;
import bot.stock.stobot.database.utils.MangaDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MangaDataService {

    @Autowired
    private MangaDataRepository mangaDataRepository;

    public List<MangaData> findAllByTitle(String title) {
        return mangaDataRepository.deepSearchByTitle(title);
    }

    public MangaData newMangaData(String title){
        return mangaDataRepository.save(tempoMangaData(title));
    }

    public MangaData tempoMangaData(String title){
        MangaData mangaData = new MangaData();
        mangaData.setTitle(title);
        return mangaData;
    }

    public MangaData newMangaData(String title, List<String> altTitles){
        MangaData mangaData = new MangaData();
        mangaData.setTitle(title);
        altTitles.forEach(mangaData::addAltTitle);
        return mangaDataRepository.save(mangaData);
    }

    public MangaData fromAnilist(AnilistService.MangaRecord result) {
        return existsByTitle(result.title()) ? findMangaDataByTitle(result.title()) : newMangaData(result.title(),result.altTitles());
    }

    public boolean existsByTitle(String title) {
        return mangaDataRepository.existsByTitle(title);
    }

    public MangaData findMangaDataByTitle(String title) {
        return mangaDataRepository.findMangaDataByTitle(title);
    }

    public boolean deepCompare(String title, MangaData manga) {
        if (manga.getTitle().equalsIgnoreCase(title)) {
            return true;
        }
        for(MangaAltTitles alt: manga.getAltTitles()){
            if(alt.getTitle().equalsIgnoreCase(title)){
                return true;
            }
        }
        return false;
    }

}
