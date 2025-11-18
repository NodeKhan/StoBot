package bot.stock.stobot.bot.core;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AniListRateLimiter {
    private final AtomicLong blockUntilEpochSeconds = new AtomicLong(0) ;

    public Mono<Void> awaitPermission(){
        long delay = blockUntilEpochSeconds.get() * 1000L - System.currentTimeMillis();
        if(delay <= 0){
            return Mono.empty();
        }
        return Mono.delay(Duration.ofMillis(delay)).then();

    }

    public void updateBlockUntil(long resetTimeStamp){
        blockUntilEpochSeconds.updateAndGet(v -> Math.max(v, resetTimeStamp));
    }
}
