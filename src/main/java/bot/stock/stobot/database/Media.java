package bot.stock.stobot.database;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import bot.stock.stobot.utils.MediaStatus;
import jakarta.persistence.*;
import lombok.Data; 

@Entity
@Data
@Table(name = "media")
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Main_title", nullable = false, length = 512)
    private String mainTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MediaStatus status = MediaStatus.UNKNOWN;

    @Column(name = "total_chapters")
    private Integer totalChapters;

    @Column(name = "cover_url", length = 1024)
    private String coverUrl;

    @Column(name = "anilist_id", unique = true)
    private Integer anilistId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MediaTitle> titles = new ArrayList<>();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public void addTitles(List<String> titles){
        for(String t : titles){
            MediaTitle mt = new MediaTitle();
            mt.setMedia(this);
            mt.setTitle(t);
            this.titles.add(mt);
        }
    }
    public List<String> getTitlesToStrings(){
        List<String> titles = new ArrayList<>();
        for (MediaTitle t : this.titles) {
            titles.add(t.getTitle());
        }
        return titles;
    }
}


