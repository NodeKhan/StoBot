package bot.stock.stobot.database.manga;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class MangaRegisterKey implements Serializable {

    private long userId;
    private long mangaId;

    public MangaRegisterKey() {
    }

    public MangaRegisterKey(long userId, long mangaId) {
        this.userId = userId;
        this.mangaId = mangaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MangaRegisterKey)) return false;
        MangaRegisterKey key = (MangaRegisterKey) o;
        return Objects.equals(userId, key.userId) &&
                Objects.equals(mangaId, key.mangaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, mangaId);
    }
}
