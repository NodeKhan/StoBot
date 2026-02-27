package bot.stock.stobot.bot.core;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;



@Component
public class BotInitializer{

    private final CommandContext ctx;
    private final CommandRegister register;
    
    //base call
    public BotInitializer(
        CommandContext ctx,
        CommandRegister register
    ){
        this.ctx = ctx;
        this.register = register;
    }

    @PostConstruct
    public void init(){
        register.register(ctx);
    }

    //context to not repeat injection and code.
    //is use to esquive repetitive code
    @Component
    protected static class CommandContext {
        final JDA jda;
        final List<EventListener> listeners;
        final Guild guild;
        final CommandRegister registrar;

        public CommandContext(
            JDA jda,
            List<EventListener> listeners,
            @Value("${discord.guild-id}") String GuildID,
            CommandRegister registrar
        ){
            this.jda = jda;
            this.listeners = listeners;
            this.registrar = registrar;

            if(GuildID == null){
                System.err.println("discord.dev-guild-id is required");
            }

            this.guild = jda.getGuildById(GuildID);
        }

    }

    //add commande
    interface CommandRegister {
    void register(CommandContext ctx);
    }

    //dev profil, so all command on the guild
    @Profile("dev")
    @Component
    public static class DevCommandRegister implements CommandRegister{
        
        final private List<CommandsProvider.SlashCommand> CommandsProvider;

        public DevCommandRegister(
            List<CommandsProvider.SlashCommand> commandProviders
        ){
            this.CommandsProvider = commandProviders;
        }

        @Override
        public void register(CommandContext ctx){
            List<CommandData> commands = new ArrayList<>();
            CommandsProvider.forEach(cp -> commands.add(cp.command()));

            if(ctx.guild == null){
                System.err.println("guild not found");
                return;
            }

            ctx.guild.updateCommands()
                .addCommands(commands)
                .queue(
                    ok -> System.out.println("Registered " + commands.size() + " commands for dev guild " + ctx.guild.getName()),
                    err -> System.err.println("Failed to register dev guild commands: " + err.getMessage())                
                );
        }
    }

    //prod profil, so admin on guild and other on public
    @Profile("prod")
    @Component
    public static class ProdCommandRegister implements CommandRegister{
        
        final private List<CommandsProvider.PublicSlashCommand> PublicSlashCommand;
        final private List<CommandsProvider.AdminSlashCommand> AdminSlashCommand;

        public ProdCommandRegister(
            List<CommandsProvider.PublicSlashCommand> PublicSlashCommand,
            List<CommandsProvider.AdminSlashCommand> AdminSlashCommand
        ){
            this.PublicSlashCommand = PublicSlashCommand;
            this.AdminSlashCommand = AdminSlashCommand;
        }

        @Override
        public void register(CommandContext ctx){
            List<CommandData> PublicCommands = new ArrayList<>();
            List<CommandData> AdminCommands = new ArrayList<>();

            PublicSlashCommand.forEach(cp -> PublicCommands.add(cp.command()));
            AdminSlashCommand.forEach(cp -> AdminCommands.add(cp.command()));
        
            if(ctx.guild == null){
                System.err.println("guild not found");
                return;
            }

            ctx.guild.updateCommands()
                .addCommands(AdminCommands)
                .queue(
                    ok -> System.out.println("Registered " + AdminCommands.size() + " commands for admin guild " + ctx.guild.getName()),
                    err -> System.err.println("Failed to register admin guild commands: " + err.getMessage())                
                );

            ctx.jda.updateCommands()
                    .addCommands(PublicCommands)
                    .queue(
                            ok -> System.out.println("Registered " + PublicCommands.size() + " global commands"),
                            err -> System.err.println("Failed to register global commands: " + err.getMessage())
                    );
        }
    }
}

