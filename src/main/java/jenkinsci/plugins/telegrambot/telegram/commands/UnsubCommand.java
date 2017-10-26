package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.telegram.TelegramBot;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

public class UnsubCommand extends BotCommand {

    private final String LOG_TAG = "/unsub";

    public UnsubCommand() {
        super("unsub", TelegramBot.getProp().getProperty("command.unsub"));
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Subscribers subscribers = Subscribers.getInstance();
        String ans;

        Long id = chat.getId();

        boolean isSubscribed = subscribers.isSubscribed(id);

        if (isSubscribed) {
            subscribers.unsubscribe(id);
            ans = TelegramBot.getProp().getProperty("message.unsub.success");
        } else {
            ans = TelegramBot.getProp().getProperty("message.unsub.alreadyunsub");
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(ans);

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOG_TAG, e);
        }
    }
}
