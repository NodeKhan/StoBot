package bot.stock.stobot.config;

import bot.stock.stobot.bot.core.AniListRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AnilistClientConfig {

    @Bean
    WebClient anilistWebClient(WebClient.Builder builder, AniListRateLimiter limiter) {
        return builder
                .baseUrl("https://graphql.anilist.co")
                .defaultHeader("User-Agent", "StoBot")
                .filter((request, next) ->
                        limiter.awaitPermission().then(
                        next.exchange(request)
                        .doOnNext(response -> {
                            String limit = response.headers().asHttpHeaders()
                                    .getFirst("X-RateLimit-Limit");
                            String remaining = response.headers().asHttpHeaders()
                                    .getFirst("X-RateLimit-Remaining");
                            String reset = response.headers().asHttpHeaders()
                                    .getFirst("X-RateLimit-Reset");

                            System.out.println("Anilist rate current limit = " + limit);
                            System.out.println("Anilist rate remaining = " + remaining);
                            if (remaining != null && remaining.equals("0") && reset != null){
                                long timer = Long.parseLong(reset);
                                long waitSeconds = Math.max(0, timer - System.currentTimeMillis() / 1000L);
                                System.out.println("Blocking requests for " + waitSeconds + " seconds.");

                            }
                        })
                ))
                .build();
    }

    @Bean
    HttpGraphQlClient anilistGraphQlClient(WebClient anilistWebClient) {
        return HttpGraphQlClient.builder(anilistWebClient).build();
    }
}
