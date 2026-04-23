package bot.stock.stobot.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bot.stock.stobot.database.MediaTitle;

public interface MediaTitleRepository extends JpaRepository<MediaTitle,Long>{
    
}
