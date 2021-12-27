package jenkinsci.plugins.telegrambot.telegram;

import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.TelegramBotGlobalConfiguration;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TelegramBotRunner {
    private static TelegramBotRunner instance;

    private static final Logger LOG = Logger.getLogger(TelegramBot.class.getName());

    private final TelegramBotsApi api;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TelegramBot bot;
    private BotSession botSession;

    private String botToken;
    private String botName;

    public TelegramBotRunner() throws TelegramApiException {
        api = new TelegramBotsApi(DefaultBotSession.class);
    }

    public synchronized static TelegramBotRunner getInstance() {
        if (instance == null) {
            try {
                instance = new TelegramBotRunner();
            } catch (TelegramApiException e) {
                LOG.log(Level.SEVERE, "Telegram API error", e);
            }
        }
        return instance;
    }

    public void runBot(String name, String token) {
        botName = name;
        botToken = token;
        executor.submit(startBotTask);
    }

    public TelegramBot getBot() {
        return bot;
    }

    private final Runnable startBotTask = () -> {
        if (bot == null
                || !bot.getBotToken().equals(botToken)
                || !bot.getBotUsername().equals(botName)) {
            bot = new TelegramBot(botToken, botName);
            LOG.log(Level.INFO, "Bot was created");
        } else {
            LOG.log(Level.INFO, "There is no reason for bot recreating");
            return;
        }
        createBotSession();
    };

    private void createBotSession() {
        if (botSession != null && botSession.isRunning()) {
            LOG.info("Stopping previous bot session");
            botSession.stop();
        }

        try {
            botSession = api.registerBot(bot);
            LOG.log(Level.INFO, "New bot session was registered");
        } catch (TelegramApiException e) {
            LOG.log(Level.SEVERE, "Telegram API error", e);
        }
    }
}
