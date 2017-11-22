package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;

import java.util.Observable;
import java.util.Observer;

public class TelegramBotRunner implements Observer {
    private static TelegramBotRunner instance;

    private TelegramBotThread botThread;
    private static final GlobalConfiguration CONFIG = GlobalConfiguration.getInstance();

    public synchronized static TelegramBotRunner getInstance() {
        if (instance == null) {
            instance = new TelegramBotRunner();
        }

        return instance;
    }

    public void runBot() {
        CONFIG.addObserver(this);

        if (botThread != null) {
            botThread.interrupt();
        }

        botThread = new TelegramBotThread(CONFIG.getBotToken(), CONFIG.getBotName());
        botThread.start();
    }

    @Override
    public void update(Observable observable, Object o) {
        String cfgBotToken = CONFIG.getBotToken();
        String cfgBotName = CONFIG.getBotName();
        String botToken = botThread.getBot().getBotToken();
        String botName = botThread.getBot().getBotUsername();

        // Restart bot if token or/and name have been changed
        if (!cfgBotToken.equals(botToken) || !cfgBotName.equals(botName)) {
            botThread.interrupt();
            botThread = new TelegramBotThread(CONFIG.getBotToken(), CONFIG.getBotName());
            botThread.start();
        }
    }

    public TelegramBotThread getBotThread() {
        return botThread;
    }
}
