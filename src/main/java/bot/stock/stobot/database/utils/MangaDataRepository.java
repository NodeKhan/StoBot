package bot.stock.stobot.database.utils;

import bot.stock.stobot.database.manga.MangaAltTitles;
import bot.stock.stobot.database.manga.MangaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MangaDataRepository extends JpaRepository<MangaData, Integer> {
    @Query("""
    select md from MangaData md left join md.altTitles at
    where lower(md.title) like  lower(concat( '%',:name,'%'))
    or lower(at.title) like lower(concat( '%', :name,'%'))
""")
    List<MangaData> deepSearchByTitle(String name);

    boolean existsByTitle(String title);
    MangaData findMangaDataByTitle(String title);
}