package bot.stock.stobot.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "MangaTitles")
public class MangaTitles {

    @Column(nullable = false)
    private int anilistId;

    @Id
    @Column(nullable = false)
    private String title;

    @Column(nullable = false,columnDefinition = "TINYINT(1)")
    private boolean primaryTitle;
}
