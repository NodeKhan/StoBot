package bot.stock.stobot.database.manga;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "manga_alt_titles")
public class MangaAltTitles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "manga_id") // <-- this creates the foreign key column
    private MangaData mangaId;
}
