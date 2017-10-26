package jenkinsci.plugins.telegrambot.config;

import jenkinsci.plugins.telegrambot.users.UserApprover;
import org.apache.log4j.Logger;

import java.util.Observable;

/**
 * This class if user for the storing global plugin configuration.
 */
public class GlobalConfiguration extends Observable {
    private final Logger logger = Logger.getLogger(this.getClass());
    private static GlobalConfiguration instance = new GlobalConfiguration();

    /**
     * The plugin display name
     */
    public final static String PLUGIN_DISPLAY_NAME = "TelegramBot";

    private Boolean shouldLogToConsole;
    private String botToken;
    private String botName;
    private String usernames;
    private UserApprover.ApprovalType approvalType;

    public synchronized static GlobalConfiguration getInstance() {
        if (instance == null) {
            instance = new GlobalConfiguration();
        }

        return instance;
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
        this.botToken = botToken;

        // If token changed, Telegram Bot must be restarted
        setChanged();
        notifyObservers();
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;

        // If bot name changed, Telegram Bot must be restarted
        setChanged();
        notifyObservers();
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
