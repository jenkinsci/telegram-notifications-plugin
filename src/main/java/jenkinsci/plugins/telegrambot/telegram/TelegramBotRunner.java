package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramBotRunner implements Observer {
    private static TelegramBotRunner instance;

    private static final Logger LOG = Logger.getLogger(TelegramBot.class.getName());
    private static final GlobalConfiguration CONFIG = GlobalConfiguration.getInstance();
    private static final TelegramBotsApi TELEGRAM_BOTS_API = new TelegramBotsApi();

    private TelegramBot bot;
    private BotSession botSession;

    static {
        ApiContextInitializer.init();
    }

    public synchronized static TelegramBotRunner getInstance() {
        if (instance == null) {
            instance = new TelegramBotRunner();
        }

        return instance;
    }

    public void runBot() {
        CONFIG.addObserver(this);
        rerunBot();
    }

    @Override
    public void update(Observable observable, Object o) {
        rerunBot();
    }

    public TelegramBot getBot() {
        return bot;
    }

    private void rerunBot() {
        if (bot == null
                || !bot.getBotToken().equals(CONFIG.getBotToken())
                || !bot.getBotUsername().equals(CONFIG.getBotName())) {
            bot = new TelegramBot(CONFIG.getBotToken(), CONFIG.getBotName());
            LOG.log(Level.INFO, "Bot was recreated");
        } else {
            LOG.log(Level.INFO, "There is no reason for bot recreating");
        }

        if (botSession != null && botSession.isRunning()) {
            botSession.stop();
            LOG.log(Level.INFO, "Bot session stopped");
        }

        try {
            botSession = TELEGRAM_BOTS_API.registerBot(bot);
            LOG.log(Level.INFO, "New bot session was registered");
        } catch (TelegramApiRequestException e) {
            LOG.log(Level.SEVERE, "Telegram API error", e);
        }
    }
}
