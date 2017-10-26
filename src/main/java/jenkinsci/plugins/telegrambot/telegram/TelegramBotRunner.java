package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;

import java.util.Observable;
import java.util.Observer;

public class TelegramBotRunner implements Observer {
    private TelegramBotThread thread;
    private static TelegramBotRunner instance = new TelegramBotRunner();

    public synchronized static TelegramBotRunner getInstance() {
        if (instance == null) {
            instance = new TelegramBotRunner();
        }

        return instance;
    }

    public void runBot() {
        GlobalConfiguration config = GlobalConfiguration.getInstance();
        config.addObserver(this);

        if (thread != null) {
            thread.interrupt();
        }

        thread = new TelegramBotThread(config.getBotToken(), config.getBotName());
        thread.start();
    }

    @Override
    public void update(Observable observable, Object o) {
        GlobalConfiguration config = GlobalConfiguration.getInstance();

        String cfgBotToken = config.getBotToken();
        String cfgBotName = config.getBotName();
        String botToken = thread.getBot().getBotToken();
        String botName = thread.getBot().getBotUsername();

        // Restart bot if necessary
        if (!cfgBotToken.equals(botToken) || !cfgBotName.equals(botName)) {
            thread.interrupt();
            thread = new TelegramBotThread(config.getBotToken(), config.getBotName());
            thread.start();
        }
    }

    public TelegramBotThread getThread() {
        return thread;
    }
}
