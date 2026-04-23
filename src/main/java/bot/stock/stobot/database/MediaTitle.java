package bot.stock.stobot.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
    name = "media_titles",
    indexes = @Index(name = "idx_media_id", columnList = "media_id")
)
@Data
public class MediaTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Column(nullable = false, length = 512)
    private String title;
}