package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.TelegramBot;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractBotCommand extends BotCommand {
    protected static final GlobalConfiguration CONFIG = GlobalConfiguration.getInstance();

    final Map<String, String> botStrings;

    public AbstractBotCommand(final String commandIdentifier, final String descriptionKey) {
        super(commandIdentifier, GlobalConfiguration.getInstance().getBotStrings().get(descriptionKey));
        botStrings = GlobalConfiguration.getInstance().getBotStrings();
    }
}
