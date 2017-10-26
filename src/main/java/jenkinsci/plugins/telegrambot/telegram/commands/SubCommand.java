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

public class SubCommand extends BotCommand {

    private final String LOG_TAG = "/sub";

    public SubCommand() {
        super("sub", TelegramBot.getProp().getProperty("command.sub"));
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
            ans = TelegramBot.getProp().getProperty("message.sub.success");
        } else {
            ans = TelegramBot.getProp().getProperty("message.sub.alreadysub");
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(ans);

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e1) {
            BotLogger.error(LOG_TAG, e1);
        }
    }
}
