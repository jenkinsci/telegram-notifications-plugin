package jenkinsci.plugins.telegrambot;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import jenkinsci.plugins.telegrambot.users.User;
import jenkinsci.plugins.telegrambot.users.UserApprover;
import jenkinsci.plugins.telegrambot.utils.StaplerRequestContainer;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class if user for the storing global plugin configuration.
 */
@Extension
public class TelegramBotGlobalConfiguration extends GlobalConfiguration {

    final static String PLUGIN_DISPLAY_NAME = "TelegramBot";
    private final Map<String, String> botStrings;

    private Boolean shouldLogToConsole = Boolean.TRUE;
    private String botToken;
    private String botName;
    private UserApprover.ApprovalType approvalType;
    private Set<User> users;

    /**
     * Called when Jenkins is starting and it's config is loading
     */
    @DataBoundConstructor
    public TelegramBotGlobalConfiguration() {
        try {
            Properties properties = new Properties();
            properties.load(TelegramBotGlobalConfiguration.class.getClassLoader().getResourceAsStream("bot.properties"));
            botStrings = Collections.unmodifiableMap(properties.stringPropertyNames().stream()
                    .collect(Collectors.toMap(Function.identity(), properties::getProperty)));
        } catch (IOException e) {
            throw new RuntimeException("Bot properties file not found", e);
        }
        // Load global Jenkins config
        load();

        // Save the loaded recipients map
        Subscribers.getInstance().setUsers(users != null ? users : new HashSet<>());
        Subscribers.getInstance().addObserver(this::onSubscribersUpdate);

        // Run the bot after Jenkins config has been loaded
        TelegramBotRunner.getInstance().runBot(botName, botToken);
    }

    /**
     * Called when Jenkins config is saving via web-interface
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

        // Save for the future using
        StaplerRequestContainer.req = req;

        // Getting simple params from formData
        setShouldLogToConsole(formData.getBoolean("shouldLogToConsole"));
        setBotToken(formData.getString("botToken"));
        setBotName(formData.getString("botName"));

        // Approve users
        UserApprover userApprover = new UserApprover(users != null ? users : new HashSet<>());
        approvalType = userApprover.approve(formData);
        users = userApprover.getUsers();

        // Store users
        Subscribers.getInstance().setUsers(users != null ? new HashSet<>(users) : new HashSet<>());

        TelegramBotRunner.getInstance().runBot(botName, botToken);

        // Save the configuration
        save();
        return super.configure(req, formData);
    }

    private void onSubscribersUpdate(Observable o, Object arg) {
        users = Subscribers.getInstance().getUsers();
        save();
    }

    public FormValidation doCheckMessage(@QueryParameter String value) throws IOException, ServletException {
        return value.length() == 0 ? FormValidation.error("Please set a message") : FormValidation.ok();
    }

    public boolean isApplicable(Class<? extends AbstractProject> clazz) {
        return true;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return PLUGIN_DISPLAY_NAME;
    }

    public Map<String, String> getBotStrings() {
        return botStrings;
    }

    public Boolean isShouldLogToConsole() {
        return shouldLogToConsole;
    }

    public void setShouldLogToConsole(Boolean shouldLogToConsole) {
        this.shouldLogToConsole = shouldLogToConsole;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = Secret.fromString(botToken).getPlainText();
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public Set<User> getUsers() {
        return users;
    }

    public UserApprover.ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(UserApprover.ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

}
