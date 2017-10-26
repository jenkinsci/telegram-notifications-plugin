package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.telegram.commands.*;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;

import java.io.IOException;
import java.util.Properties;

public class TelegramBot extends TelegramLongPollingCommandBot {
    private final static Logger logger = Logger.getLogger(TelegramBot.class);
    private final static Properties prop = new Properties();

    private final String token;
    private final String name;

    static {
        try {
            prop.load(TelegramBot.class.getClassLoader().getResourceAsStream("bot.properties"));
        } catch (IOException e) {
            logger.error("Bot properties file not found");
        }
    }

    public TelegramBot(String token, String name) {
        this.token = token;
        this.name = name;

        register(new StartCommand());
        register(new HelpCommand());
        register(new SubCommand());
        register(new UnsubCommand());
        register(new StatusCommand());
    }

    public static Properties getProp() {
        return prop;
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessageRequest = new SendMessage();

        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(message);
        sendMessageRequest.enableMarkdown(true);

        try {
            sendMessage(sendMessageRequest);
        } catch (TelegramApiException e) {
            logger.error(String.format("TelegramBot: Error while sending message: %s\n%s", chatId, message));
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        sendMessage(chatId, prop.getProperty("message.noncommand"));
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
