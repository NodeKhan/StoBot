package bot.stock.stobot.database.utils;

import bot.stock.stobot.database.manga.MangaTitles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaTitlesRepository extends JpaRepository<MangaTitles, String> {
}