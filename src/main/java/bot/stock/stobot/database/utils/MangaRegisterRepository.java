package bot.stock.stobot.database.utils;

import bot.stock.stobot.database.manga.MangaRegister;
import bot.stock.stobot.database.manga.MangaRegisterKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaRegisterRepository extends JpaRepository<MangaRegister, MangaRegisterKey> {
    boolean existsByKey(MangaRegisterKey key);
}
