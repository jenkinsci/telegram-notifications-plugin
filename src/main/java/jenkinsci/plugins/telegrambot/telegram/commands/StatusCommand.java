package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.TelegramBot;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import jenkinsci.plugins.telegrambot.users.UserApprover;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;


public class StatusCommand extends BotCommand {

    private final String LOG_TAG = "/status";

    public StatusCommand() {
        super("status", TelegramBot.getProp().getProperty("command.status"));
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Subscribers subscribers = Subscribers.getInstance();
        String ans;

        Long id = chat.getId();

        boolean isSubscribed = subscribers.isSubscribed(id);

        if (isSubscribed) {
            boolean isApproved = subscribers.isApproved(id);

            if (GlobalConfiguration.getInstance().getApprovalType() == UserApprover.ApprovalType.ALL) {
                ans = TelegramBot.getProp().getProperty("message.status.approved");
            } else {
                ans = isApproved
                        ? TelegramBot.getProp().getProperty("message.status.approved")
                        : TelegramBot.getProp().getProperty("message.status.unapproved");
            }
        } else {
            ans = TelegramBot.getProp().getProperty("message.status.unsubscribed");
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
