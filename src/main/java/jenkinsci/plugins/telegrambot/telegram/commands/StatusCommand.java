package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import jenkinsci.plugins.telegrambot.users.UserApprover;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

public class StatusCommand extends AbstractBotCommand {

    private static final String LOG_TAG = "/status";

    public StatusCommand() {
        super("status", "command.status");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Subscribers subscribers = Subscribers.getInstance();
        String toSend;

        Long id = chat.getId();

        boolean isSubscribed = subscribers.isSubscribed(id);

        if (isSubscribed) {
            boolean isApproved = subscribers.isApproved(id);

            if (GlobalConfiguration.getInstance().getApprovalType() == UserApprover.ApprovalType.ALL) {
                toSend = botStrings.get("message.status.approved");
            } else {
                toSend = isApproved
                        ? botStrings.get("message.status.approved")
                        : botStrings.get("message.status.unapproved");
            }
        } else {
            toSend = botStrings.get("message.status.unsubscribed");
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(toSend);

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOG_TAG, e);
        }
    }
}
