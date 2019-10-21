package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.TelegramBotGlobalConfiguration;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;

import java.util.Map;

public abstract class AbstractBotCommand extends BotCommand {
    private static final TelegramBotGlobalConfiguration CONFIG = GlobalConfiguration.all().get(TelegramBotGlobalConfiguration.class);

    final Map<String, String> botStrings;

    public AbstractBotCommand(final String commandIdentifier, final String descriptionKey) {
        super(commandIdentifier, CONFIG.getBotStrings().get(descriptionKey));
        botStrings = CONFIG.getBotStrings();
    }
}
