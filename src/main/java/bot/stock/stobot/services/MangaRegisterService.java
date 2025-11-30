package bot.stock.stobot.services;

import bot.stock.stobot.database.manga.MangaRegister;
import bot.stock.stobot.database.manga.MangaRegisterKey;
import bot.stock.stobot.database.utils.MangaRegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MangaRegisterService {

    @Autowired
    private MangaRegisterRepository mangaRegisterRepository;

    public MangaRegister newEntry(long userId, long mangaId, int chapter, int interest) {
        MangaRegister mangaRegister = getMangaRegister(userId,mangaId);
        mangaRegister.setChapter(chapter);
        mangaRegister.setInterest(interest);
        mangaRegister.setTimestamp(System.currentTimeMillis());
        return mangaRegisterRepository.save(mangaRegister);
    }

    public MangaRegister changeInterest (long userId, long mangaId, int interest) {
        MangaRegister mangaRegister = getMangaRegister(userId,mangaId);
        mangaRegister.setInterest(interest);
        return mangaRegisterRepository.save(mangaRegister);
    }

    public MangaRegister changeChapter(long userId, long mangaId, int chapter) {
        MangaRegister mangaRegister = getMangaRegister(userId,mangaId);
        mangaRegister.setChapter(chapter);
        mangaRegister.setTimestamp(System.currentTimeMillis());
        return mangaRegisterRepository.save(mangaRegister);
    }

    public MangaRegister getMangaRegister(long userId, long mangaId) {
        MangaRegisterKey key = new MangaRegisterKey(userId,mangaId);
        Optional<MangaRegister> mangaRegister =  mangaRegisterRepository.findById(key);
        return mangaRegister.orElse(new MangaRegister(key));
    }

    public boolean existsMangaRegister(long userId, long mangaId) {
        return mangaRegisterRepository.existsByKey(new MangaRegisterKey(userId,mangaId));
    }

}
