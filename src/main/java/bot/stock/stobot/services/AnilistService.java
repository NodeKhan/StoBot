package bot.stock.stobot.services;

import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnilistService {

    private static final String SEARCH_QUERY = """
        query($search: String, $formats: [MediaFormat]) {
          Media(search: $search, type: MANGA, isAdult: false, format_in: $formats) {
            id
            title { romaji english }
            synonyms
            status
            chapters
            format
            coverImage { large }
            description(asHtml: false)
          }
        }
    """;

    private static final List<String> ALLOWED_FORMATS = List.of("MANGA", "ONE_SHOT");
    private static final String ALT_TITLE_PATTERN = "^[a-zA-Z0-9\\s'!?:.,-]+";
    private static final String HTML_TAG_PATTERN = "<.+?>";
    private static final String SOURCE_PATTERN = "\\s*\\(Source: [^)]+\\)";

    private final HttpGraphQlClient gql;

    public AnilistService(HttpGraphQlClient anilistGraphQlClient) {
        this.gql = anilistGraphQlClient;
    }

    public Mono<MangaRecord> searchManga(String name) {
        return gql.document(SEARCH_QUERY)
                .variable("search", name)
                .variable("formats", ALLOWED_FORMATS)
                .retrieve("Media")
                .toEntity(MediaResponse.class)
                .mapNotNull(this::toMangaRecord);
    }

    // ---- Records ----

    public record Title(String romaji, String english) {}

    public record CoverImage(String large) {}

    public record MediaResponse(
            int id,
            Title title,
            List<String> synonyms,
            String status,
            int chapters,
            String format,
            CoverImage coverImage,
            String description
    ) {}

    public record MangaRecord(
            int id,
            String title,
            List<String> altTitles,
            String status,
            int chapters,
            String coverUrl,
            String description
    ) {}

    // ---- Mapping ----

    private MangaRecord toMangaRecord(MediaResponse m) {
        if (m == null) return null;

        String title = resolveTitle(m.title());
        List<String> altTitles = resolveAltTitles(m.synonyms(), m.title(), title);
        String description = cleanDescription(m.description());

        return new MangaRecord(
                m.id(),
                title,
                altTitles,
                m.status(),
                m.chapters(),
                m.coverImage().large(),
                description
        );
    }

    private String resolveTitle(Title title) {
        return title.english() != null ? title.english() : title.romaji();
    }

    private List<String> resolveAltTitles(List<String> synonyms, Title title, String mainTitle) {
        List<String> altTitles = synonyms == null ? new java.util.ArrayList<>() : synonyms.stream()
                .filter(s -> s.matches(ALT_TITLE_PATTERN))
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        if (!Objects.equals(title.romaji(), mainTitle)) {
            altTitles.add(title.romaji());
        }

        return altTitles;
    }

    private String cleanDescription(String description) {
        return Objects.requireNonNullElse(description, "No description available.")
                .replaceAll(HTML_TAG_PATTERN, "")
                .replaceAll(SOURCE_PATTERN, "")
                .strip();
    }
}