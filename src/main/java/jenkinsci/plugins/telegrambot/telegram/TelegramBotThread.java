package jenkinsci.plugins.telegrambot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.BotSession;

public class TelegramBotThread extends Thread {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final TelegramBot bot;
    private BotSession session;
    private TelegramBotsApi telegramBotsApi;

    public TelegramBotThread(String botToken, String botName) {
        super(String.format("TelegramBot Thread; name=%s; token=%s", botName, botToken));

        bot = new TelegramBot(botToken, botName);
        telegramBotsApi = new TelegramBotsApi();
    }

    public TelegramBot getBot() {
        return bot;
    }

    @Override
    public void run() {
        try {
            session = telegramBotsApi.registerBot(bot);
            logger.info("BotSession started");

            while (true) {
                if (isInterrupted()) {
                    session.close();
                    logger.info("BotSession closed");
                    break;
                }
            }

        } catch (TelegramApiException e) {
            logger.error("Telegram API error", e);
            interrupt();
        }
    }
}
