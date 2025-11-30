package bot.stock.stobot.database.manga;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "manga_data")
public class MangaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mangaId;

    @Column(nullable = false, unique = true)
    private String title;


    @OneToMany(mappedBy = "mangaId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<MangaAltTitles> altTitles = new ArrayList<>();

    public void addAltTitle(String title) {
        MangaAltTitles alt = new MangaAltTitles();
        alt.setTitle(title);
        alt.setMangaId(this);
        this.altTitles.add(alt);
    }


}
