package bot.stock.stobot.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bot.stock.stobot.database.Media;
import bot.stock.stobot.database.UserMedia;

public interface UserMediaRepository extends JpaRepository<UserMedia,Long>{
    Optional<UserMedia> findByDiscordUserIdAndMedia(long discordUserId, Media media);
    @Query("""
    select um from UserMedia um
    join fetch um.media m
    left join fetch m.titles
    where um.discordUserId = :userId
    """)
    List<UserMedia> findAllByDiscordUserIdWithMedia(@Param("userId") long userId);
}
