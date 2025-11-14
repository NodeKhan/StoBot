package bot.stock.stobot.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AnilistService {
    private final HttpGraphQlClient gql;

    public AnilistService(HttpGraphQlClient anilistGraphQlClient) {
        this.gql = anilistGraphQlClient;
    }

    public Mono<MediaResponse> searchManga(String name) {
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
                .toEntity(MediaResponse.class);
    }

    // DTOs
    public record MediaResponse(int id, Title title, List<String> synonyms, String status, int chapters,
                                CoverImage coverImage, String description) {}
    public record Title(String romaji, String english) {}
    public record CoverImage(String large) {}
}
