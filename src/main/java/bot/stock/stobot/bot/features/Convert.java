package bot.stock.stobot.bot.features;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import bot.stock.stobot.bot.core.CommandsProvider;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
@Component
public class Convert extends ListenerAdapter implements CommandsProvider.PublicSlashCommand {
    
    @Override
    public CommandData command() {
        return Commands.slash("convert", "Convert a currency to EUR")
                .addOption(OptionType.STRING, "amount", "Amount to convert", true)
                .addOption(OptionType.STRING, "currency_in", "Currency to convert from", true)
                .addOption(OptionType.STRING, "currency_out", "Currency to convert into (default: eur)", false);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("convert")) return;

        String amountStr = event.getOption("amount").getAsString();
        String currencyIN = event.getOption("currency_in").getAsString().toLowerCase();
        String currencyOUT = event.getOption("currency_out","eur", OptionMapping::getAsString).toLowerCase();
        
        if(currencyIN.equalsIgnoreCase(currencyOUT)){
            event.reply(String.format("%s %s = %s %s duuuh",amountStr,currencyIN,amountStr,currencyOUT)).queue();
            return;
        }

        event.deferReply().queue();

        try {
            double amount = Double.parseDouble(amountStr.replaceAll(" |,", ""));

            RestClient restClient = RestClient.create();
            Map<String, Object> response = restClient.get()
                    .uri("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/"+currencyOUT+".json")
                    .retrieve()
                    .body(Map.class);

            Map<String, Double> rates = (Map<String, Double>) response.get(currencyOUT);
            Double taux = rates.get(currencyIN);

            if (taux == null) {
                event.getHook().editOriginal("Devise inconnue : `" + currencyIN.toUpperCase() + "`").queue();
                return;
            }

            double result = amount / taux;
            String cutAmount = String.format("%.2f",amount).replaceAll("(\\d)(?=(\\d{3})+(?:\\.|$))", "$1,");
            String cutResult = String.format("%.2f",result).replaceAll("(\\d)(?=(\\d{3})+(?:\\.|$))", "$1,");
            log.info("Convert {} {} → {} EUR (taux: {})", cutAmount, currencyIN, cutResult, taux);

            event.getHook()
                    .editOriginal(String.format("%s %s = **%s %s**", cutAmount, currencyIN.toUpperCase(), cutResult,currencyOUT.toUpperCase()))
                    .queue();

        } catch (NumberFormatException e) {
            event.getHook().editOriginal("Montant invalide : `" + amountStr + "`").queue();
        } catch (Exception e) {
            log.error("Erreur conversion", e);
            event.getHook().editOriginal(" Erreur lors de la conversion.").queue();
        }
    }
}
