package bot.stock.stobot.database;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(
    name = "user_media",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_media",
        columnNames = {"discord_user_id", "media_id"}
    )
)
@Data
public class UserMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_user_id", nullable = false)
    private long discordUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Column(name = "chapters_read", nullable = false)
    private int chaptersRead = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus readingStatus = ReadingStatus.PLAN_TO_READ;

    @Column
    private Byte rating;                    // null = pas noté, 1-10

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.lastUpdated = LocalDateTime.now(); }

    public enum ReadingStatus {
    READING, COMPLETED, DROPPED, PLAN_TO_READ
    }

}
