package bot.stock.stobot.database.manga;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "manga_titles")
public class MangaTitles {

    @Column(nullable = false)
    private int anilistId;

    @Id
    @Column(nullable = false)
    private String title;

    @Column(nullable = false,columnDefinition = "TINYINT(1)")
    private boolean primaryTitle;
}
