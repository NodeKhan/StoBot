package bot.stock.stobot.database.manga;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "manga_register")
public class MangaRegister {

    @EmbeddedId
    private MangaRegisterKey key;

    @Column(nullable = false)
    private int chapter;

    @Column(nullable = false)
    private long timestamp;

    private int interest;
    private boolean completed = false;


    public MangaRegister(int userId, int mangaId) {
        this.key = new MangaRegisterKey(userId, mangaId);
    }
    public MangaRegister(MangaRegisterKey key) {
        this.key = key;
    }

    public MangaRegister() {}
}

