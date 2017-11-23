package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.users.Subscribers;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.logging.BotLogger;

public class SubCommand extends AbstractBotCommand {

    private final String LOG_TAG = "/sub";

    public SubCommand() {
        super("sub", "command.sub");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Subscribers subscribers = Subscribers.getInstance();
        String ans;

        Long id = chat.getId();
        String name = chat.isUserChat() ? user.getUserName() : chat.getTitle();

        boolean isSubscribed = subscribers.isSubscribed(id);

        if (!isSubscribed) {
            subscribers.subscribe(name, id);
            ans = botStrings.get("message.sub.success");
        } else {
            ans = botStrings.get("message.sub.alreadysub");
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
