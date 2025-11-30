package bot.stock.stobot.services;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnilistService {
    private final HttpGraphQlClient gql;

    public AnilistService(HttpGraphQlClient anilistGraphQlClient) {
        this.gql = anilistGraphQlClient;
    }

    public Mono<MangaRecord> searchManga(String name) {
        String doc = """
            query($search: String){
              Media(search: $search, type: MANGA, isAdult: false){
                id
                title{romaji english}
                synonyms
                status
                chapters
                coverImage{large}
                description(asHtml:false)
              }
            }
        """;

        return gql.document(doc)
                .variable("search", name)
                .retrieve("Media")
                .toEntity(MediaResponse.class)
                .map(this::processResponse);
    }

    public record Title(String romaji, String english) {}
    public record CoverImage(String large) {}

    public record MediaResponse(int id, Title title, List<String> synonyms, String status, int chapters,
                                CoverImage coverImage, String description) {}
    public record MangaRecord(int id, String title, List<String> altTitles, String status, int chapters,
                              String coverUrl, String description) {}

    private MangaRecord processResponse(MediaResponse m) {
        if(m == null) return null;
        String title = m.title().english() != null ? m.title().english() : m.title().romaji();

        List<String> altTitle = new ArrayList<>();
        if(m.synonyms() != null && !m.synonyms().isEmpty()){
            for(String s : m.synonyms()){
                if(s.matches("^[a-zA-Z0-9\\s'!?:.,-]+")){
                    altTitle.add(s);
                }
            }
        }
        if (!m.title().romaji().equals(title)) {
            altTitle.add(m.title().romaji());
        }

        String desc = (m.description() == null ? "No description available." : m.description())
                .replaceAll("<.+?>", "")
                .replaceAll("\\s*\\(Source: [^)]+\\)", "");


        return new MangaRecord(
                m.id(),
                title,
                altTitle,
                m.status(),
                m.chapters(),
                m.coverImage().large(),
                desc
        );}

}
