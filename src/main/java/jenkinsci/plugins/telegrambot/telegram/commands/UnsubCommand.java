package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.users.Subscribers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UnsubCommand extends AbstractBotCommand {

    private static final String LOG_TAG = "/unsub";
    private static final Logger LOGGER = LoggerFactory.getLogger(UnsubCommand.class);

    public UnsubCommand() {
        super("unsub", "command.unsub");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Subscribers subscribers = Subscribers.getInstance();
        String ans;

        Long id = chat.getId();

        boolean isSubscribed = subscribers.isSubscribed(id);

        if (isSubscribed) {
            subscribers.unsubscribe(id);
            ans = botStrings.get("message.unsub.success");
        } else {
            ans = botStrings.get("message.unsub.alreadyunsub");
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(ans);

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            LOGGER.error(LOG_TAG, e);
        }
    }
}
