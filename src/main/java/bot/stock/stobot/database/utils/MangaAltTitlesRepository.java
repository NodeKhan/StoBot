package bot.stock.stobot.database.utils;

import bot.stock.stobot.database.manga.MangaAltTitles;
import bot.stock.stobot.database.manga.MangaData;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MangaAltTitlesRepository extends JpaRepository<MangaAltTitles, String> {

    boolean existsByTitle(String title);
    List<MangaAltTitles> findByTitle(String title);

    @Query("SELECT m.title FROM MangaAltTitles m WHERE m.mangaId.mangaId = :id")
    List<String> getAllTitleByMangaId(@Param("id") Long mangaId);
}