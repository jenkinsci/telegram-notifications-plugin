package jenkinsci.plugins.telegrambot.telegram;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.BotSession;

public class TelegramBotThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(TelegramBotThread.class);

    private final TelegramBot bot;

    private static final TelegramBotsApi TELEGRAM_BOTS_API = new TelegramBotsApi();

    public TelegramBotThread(String botToken, String botName) {
        super(String.format("TelegramBot Thread; name=%s; token=%s", botName, botToken));

        bot = new TelegramBot(botToken, botName);
    }

    public TelegramBot getBot() {
        return bot;
    }

    @Override
    public void run() {
        try {
            // Start bot session
            BotSession session = TELEGRAM_BOTS_API.registerBot(bot);
            LOGGER.info("BotSession was started");

            while (true) {
                if (isInterrupted()) {
                    // If thread was interrupted bot session should be closed
                    session.close();
                    LOGGER.info("BotSession was closed");
                    break;
                }
            }

        } catch (TelegramApiException e) {
            LOGGER.error("Telegram API error", e);
            interrupt();
        }
    }
}
