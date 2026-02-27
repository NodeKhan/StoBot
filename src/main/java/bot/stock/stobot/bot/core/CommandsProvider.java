package bot.stock.stobot.bot.core;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public final class CommandsProvider {

    private CommandsProvider() {}

    public interface SlashCommand {
        CommandData command();
    }

    public interface AdminSlashCommand extends SlashCommand {}

    public interface PublicSlashCommand extends SlashCommand {}
}
