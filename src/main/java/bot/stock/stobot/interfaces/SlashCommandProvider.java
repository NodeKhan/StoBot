package bot.stock.stobot.interfaces;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommandProvider {
    CommandData command();
}

