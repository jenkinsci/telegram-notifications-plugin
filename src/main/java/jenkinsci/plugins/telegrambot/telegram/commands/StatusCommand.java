package jenkinsci.plugins.telegrambot.telegram.commands;

import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.TelegramBotGlobalConfiguration;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import jenkinsci.plugins.telegrambot.users.UserApprover;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

public class StatusCommand extends AbstractBotCommand {

    private static final TelegramBotGlobalConfiguration CONFIG = GlobalConfiguration.all().get(TelegramBotGlobalConfiguration.class);

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

            if (CONFIG.getApprovalType() == UserApprover.ApprovalType.ALL) {
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
