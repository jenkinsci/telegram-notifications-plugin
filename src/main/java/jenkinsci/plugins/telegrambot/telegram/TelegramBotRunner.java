package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramBotRunner implements Observer {
    private static TelegramBotRunner instance;

    private static final Logger LOG = Logger.getLogger(TelegramBot.class.getName());
    private static final GlobalConfiguration CONFIG = GlobalConfiguration.getInstance();

    private final TelegramBotsApi api = new TelegramBotsApi();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        executor.submit(startBotTask);
    }

    @Override
    public void update(Observable observable, Object o) {
        rerunBot();
    }

    public TelegramBot getBot() {
        return bot;
    }

    private final Runnable startBotTask = () -> {
        bot = new TelegramBot(CONFIG.getBotToken(), CONFIG.getBotName());
        createBotSession();
    };

    private final Runnable rerunBotTask = () -> {
        if (bot == null
                || !bot.getBotToken().equals(CONFIG.getBotToken())
                || !bot.getBotUsername().equals(CONFIG.getBotName())) {
            bot = new TelegramBot(CONFIG.getBotToken(), CONFIG.getBotName());
            LOG.log(Level.INFO, "Bot was created");
        } else {
            LOG.log(Level.INFO, "There is no reason for bot recreating");
            return;
        }

        if (botSession != null && botSession.isRunning()) {
            botSession.stop();
            LOG.log(Level.INFO, "Bot session stopped");
        }

        createBotSession();
    };

    private void rerunBot() {
        executor.submit(rerunBotTask);
    }

    private void createBotSession() {
        try {
            botSession = api.registerBot(bot);
            LOG.log(Level.INFO, "New bot session was registered");
        } catch (TelegramApiRequestException e) {
            LOG.log(Level.SEVERE, "Telegram API error", e);
        }
    }
}
