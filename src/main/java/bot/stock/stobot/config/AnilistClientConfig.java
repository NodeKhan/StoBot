package bot.stock.stobot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AnilistClientConfig {

    @Bean
    WebClient anilistWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://graphql.anilist.co")
                .defaultHeader("User-Agent", "YourApp/1.0")
                .build();
    }

    @Bean
    HttpGraphQlClient anilistGraphQlClient(WebClient anilistWebClient) {
        return HttpGraphQlClient.builder(anilistWebClient).build();
    }
}
