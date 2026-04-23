package bot.stock.stobot.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bot.stock.stobot.database.Media;

public interface MediaRepository extends JpaRepository<Media,Long>{
    @Query("""
        select distinct m from Media m
        left join fetch m.titles t
        where lower(m.mainTitle) like lower(concat('%', :name, '%'))
        or lower(t.title) like lower(concat('%', :name, '%'))
    """)
    List<Media> searchByTitle(@Param("name") String name);
    
    @Query("select m from Media m where lower(m.mainTitle) = lower(:title)")
    Optional<Media> findByMainTitleIgnoreCase(@Param("title") String title);

}
