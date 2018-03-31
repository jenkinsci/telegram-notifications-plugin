package jenkinsci.plugins.telegrambot.config;

import jenkinsci.plugins.telegrambot.users.UserApprover;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class if user for the storing global plugin configuration.
 */
public class GlobalConfiguration extends Observable {
    private static GlobalConfiguration instance;

    public final static String PLUGIN_DISPLAY_NAME = "TelegramBot";

    private Boolean shouldLogToConsole;
    private String botToken;
    private String botName;
    private String usernames;
    private UserApprover.ApprovalType approvalType;

    private final Map<String, String> botStrings;

    private GlobalConfiguration() {
        try {
            Properties properties = new Properties();
            properties.load(GlobalConfiguration.class.getClassLoader().getResourceAsStream("bot.properties"));
            botStrings = Collections.unmodifiableMap(properties.stringPropertyNames().stream()
                    .collect(Collectors.toMap(Function.identity(), properties::getProperty)));
        } catch (IOException e) {
            throw new RuntimeException("Bot properties file not found", e);
        }
    }

    public synchronized static GlobalConfiguration getInstance() {
        if (instance == null) {
            instance = new GlobalConfiguration();
        }

        return instance;
    }

    public Map<String, String> getBotStrings() {
        return botStrings;
    }

    public Boolean shouldLogToConsole() {
        return shouldLogToConsole;
    }

    public void setLogToConsole(Boolean shouldLogToConsole) {
        this.shouldLogToConsole = shouldLogToConsole;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        if (!Objects.equals(this.botToken, botToken)) {
            this.botToken = botToken;

            // If token changed, Telegram Bot must be restarted
            setChanged();
            notifyObservers();
        }
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        if (!Objects.equals(this.botName, botName)) {
            this.botName = botName;

            // If bot name changed, Telegram Bot must be restarted
            setChanged();
            notifyObservers();
        }
    }

    public String getUsernames() {
        return usernames;
    }

    public void setUsernames(String usernames) {
        this.usernames = usernames;
    }

    public UserApprover.ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(UserApprover.ApprovalType approvalType) {
        this.approvalType = approvalType;
    }
}
