package bot.stock.stobot.interfaces;

import bot.stock.stobot.model.MangaTitles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaTitlesRepository extends JpaRepository<MangaTitles, String> {
}