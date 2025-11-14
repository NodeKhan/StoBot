package bot.stock.stobot.services;

import bot.stock.stobot.interfaces.MangaTitlesRepository;
import bot.stock.stobot.model.MangaTitles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MangaTitlesService {

    @Autowired
    private MangaTitlesRepository mangaTitlesRepository;

    public MangaTitles addTitle(int anilistId, String title, boolean primaryTitle) {
        MangaTitles entry = new MangaTitles();
        entry.setTitle(title);
        entry.setPrimaryTitle(primaryTitle);
        entry.setAnilistId(anilistId);
        return mangaTitlesRepository.save(entry);
    }
}
