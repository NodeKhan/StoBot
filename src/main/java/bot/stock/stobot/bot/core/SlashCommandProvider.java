package bot.stock.stobot.bot.core;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommandProvider {
    CommandData command();
}

