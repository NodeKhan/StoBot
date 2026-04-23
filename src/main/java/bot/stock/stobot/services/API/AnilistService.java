package bot.stock.stobot.services.API;

import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import bot.stock.stobot.utils.Manga;
import bot.stock.stobot.utils.MediaStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
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

    public Mono<Manga> searchManga(String name) {
        return gql.document(SEARCH_QUERY)
                .variable("search", name)
                .variable("formats", ALLOWED_FORMATS)
                .retrieve("Media")
                .toEntity(MediaResponse.class)
                .mapNotNull(this::toManga);
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

    // ---- Mapping ----

    private Manga toManga(MediaResponse m) {
        if (m == null) return null;

        String title = resolveTitle(m.title());
        List<String> altTitles = resolveAltTitles(m.synonyms(), m.title(), title);
        String description = cleanDescription(m.description());
        MediaStatus status = resolvStatus(m.status());

        return new Manga(
            title,
            altTitles,
            status,
            m.format(),
            m.chapters(),
            m.coverImage().large(),
            description,
            m.id()
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

    private MediaStatus resolvStatus(String status){
        return switch (status) {
            case "RELEASING"    -> MediaStatus.ONGOING;
            case "FINISHED"     -> MediaStatus.COMPLETED;
            case "HIATUS"       -> MediaStatus.HIATUS;
            case "CANCELLED"    -> MediaStatus.CANCELLED;
            default             -> MediaStatus.UNKNOWN;
        };
    }

    public String handleError(Throwable error, String search) {
        if (isNotFound(error)) {
            return "\"%s\" not found on AniList.".formatted(search);
        }
        if (error instanceof TimeoutException) {
            log.warn("Timeout for '{}'", search);
            return "Search timed out after 5 seconds. Please try again.";
        }
        log.error("Unexpected error during search for '{}'", search, error);
        return "An unexpected error occurred while searching.";
    }

    private boolean isNotFound(Throwable error) {
        return error instanceof GraphQlTransportException t
                && t.getCause() instanceof WebClientResponseException ex
                && ex.getStatusCode().value() == 404;
    }
}