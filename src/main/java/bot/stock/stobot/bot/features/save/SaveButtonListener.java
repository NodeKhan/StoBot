package bot.stock.stobot.bot.features.save;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class SaveButtonListener extends ListenerAdapter {

    private final SaveService saveService;

    public SaveButtonListener(SaveService saveService) {
        this.saveService = saveService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (id.startsWith("save_confirm_"))
            saveService.handleConfirm(event, id);

        else if (id.startsWith("save_next_"))
            saveService.handleNext(event, id);

        else if (id.startsWith("save_cancel_"))
            saveService.handleCancel(event, id);
    }
}
