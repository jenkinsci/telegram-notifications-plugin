package jenkinsci.plugins.telegrambot.telegram.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand extends AbstractBotCommand {

    private static final String LOG_TAG = "/start";
    private static final Logger LOGGER = LoggerFactory.getLogger(StartCommand.class);

    public StartCommand() {
        super("start", "command.start");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(botStrings.get("message.help"));

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            LOGGER.error(LOG_TAG, e);
        }
    }
}
