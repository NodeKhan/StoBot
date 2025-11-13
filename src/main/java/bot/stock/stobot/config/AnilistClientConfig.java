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
                .defaultHeader("User-Agent", "StoBot")
                .filter((request, next) -> next.exchange(request)
                        .doOnNext(response -> {
                            System.out.println("X-RateLimit-Limit = " + response.headers().asHttpHeaders().getFirst("RateLimit-Limit"));
                            System.out.println("X-RateLimit-Remaining = " + response.headers().asHttpHeaders().getFirst("RateLimit-Remaining"));
                            System.out.println("X-RateLimit-Reset = " + response.headers().asHttpHeaders().getFirst("RateLimit-Reset"));
                        })
                )
                .build();
    }

    @Bean
    HttpGraphQlClient anilistGraphQlClient(WebClient anilistWebClient) {
        return HttpGraphQlClient.builder(anilistWebClient).build();
    }
}
