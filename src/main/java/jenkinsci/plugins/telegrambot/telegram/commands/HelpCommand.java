package jenkinsci.plugins.telegrambot.telegram.commands;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.logging.BotLogger;

public class HelpCommand extends AbstractBotCommand {

    private final String LOG_TAG = "/help";

    public HelpCommand() {
        super("help", "command.help");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(botStrings.get("message.help"));

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOG_TAG, e);
        }
    }
}
