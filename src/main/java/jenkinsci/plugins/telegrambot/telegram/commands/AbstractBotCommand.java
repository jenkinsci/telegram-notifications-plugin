package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import org.telegram.telegrambots.bots.commands.BotCommand;

import java.util.Map;

public abstract class AbstractBotCommand extends BotCommand {

    protected final Map<String, String> botStrings;

    public AbstractBotCommand(String commandIdentifier, String descriptionKey) {
        super(commandIdentifier, GlobalConfiguration.getInstance().getBotStrings().get(descriptionKey));
        botStrings = GlobalConfiguration.getInstance().getBotStrings();
    }
}
