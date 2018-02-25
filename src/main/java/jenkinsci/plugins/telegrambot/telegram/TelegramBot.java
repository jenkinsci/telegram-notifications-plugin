package jenkinsci.plugins.telegrambot.telegram;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.commands.*;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class TelegramBot extends TelegramLongPollingCommandBot {
    private final static Logger LOGGER = Logger.getLogger(TelegramBot.class.getName());

    private final String token;

    public TelegramBot(String token, String name) {
        super(name);
        this.token = token;

        Stream.of(
                new StartCommand(),
                new HelpCommand(),
                new SubCommand(),
                new UnsubCommand(),
                new StatusCommand()
        ).forEach(this::register);
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessageRequest = new SendMessage();

        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(message);
        sendMessageRequest.enableMarkdown(true);

        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, String.format("TelegramBot: Error while sending message: %s%n%s", chatId, message), e);
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        sendMessage(chatId, GlobalConfiguration.getInstance().getBotStrings().get("message.noncommand"));
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
