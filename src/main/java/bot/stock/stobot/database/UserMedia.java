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

    public enum ReadingStatus {
    READING, COMPLETED, DROPPED, PLAN_TO_READ;
        
        public String display() {
            return name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
        }
    }

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();

        if (this.readingStatus == ReadingStatus.DROPPED) return;

        if (this.chaptersRead > 0 && this.readingStatus == ReadingStatus.PLAN_TO_READ) 
            this.readingStatus = ReadingStatus.READING;

        if(media != null
            && media.getTotalChapters() != null
            && media.getTotalChapters() > 0
            && media.getTotalChapters() <= this.chaptersRead)
            this.readingStatus = ReadingStatus.COMPLETED;
    }

}
