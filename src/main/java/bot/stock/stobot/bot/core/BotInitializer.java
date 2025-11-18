package bot.stock.stobot.bot.core;

import bot.stock.stobot.bot.features.kill.KillListener;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotInitializer {

    private final JDA jda;
    private final List<EventListener> listeners;
    private final List<SlashCommandProvider> commandProviders;
    private final String guildId;
    private final KillListener killListener;

    public BotInitializer(
            JDA jda,
            List<EventListener> listeners,
            List<SlashCommandProvider> commandProviders,
            @Value("${discord.guild-id}") String guildId, KillListener killListener
    ) {
        this.jda = jda;
        this.listeners = listeners;
        this.commandProviders = commandProviders;
        this.guildId = guildId;
        this.killListener = killListener;
    }

    @PostConstruct
    public void init() {
        //TODO : change for dev/product mode
        for (EventListener l : listeners) {
            jda.addEventListener(l);
        }
        List<CommandData> commands = new ArrayList<>();
        for (SlashCommandProvider cp : commandProviders) {
            commands.add(cp.command());
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            guild.updateCommands()
                    .addCommands(commands)
                    .queue(
                            ok -> System.out.println("Registered " + commands.size() + " commands for guild " + guild.getName()),
                            err -> System.err.println("Failed to register commands: " + err.getMessage())
                    );
            guild.updateCommands().addCommands(killListener.command()).queue(
                    ok -> System.out.println("kill switch added"),
                    err -> System.err.println("Failed to register kill switch: " + err.getMessage())
            );
        } else {
            System.err.println("Guild not found: " + guildId);
        }
        jda.updateCommands()
                .addCommands(commands)
                .queue(
                        ok -> System.out.println("Registered " + commands.size() + " commands for world"),
                        err -> System.err.println("Failed to register commands: " + err.getMessage())
                );
    }
}
