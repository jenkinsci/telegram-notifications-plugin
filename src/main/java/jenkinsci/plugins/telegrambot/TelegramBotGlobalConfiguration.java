package jenkinsci.plugins.telegrambot;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
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
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class if user for the storing global plugin configuration.
 */
@Extension
public class TelegramBotGlobalConfiguration extends GlobalConfiguration implements Observer {
    private Boolean shouldLogToConsole;
    private String botToken;
    private String botName;
    private UserApprover.ApprovalType approvalType;
    private Set<User> users;

    /**
     * Called when Jenkins is starting and it's config is loading
     */
    @DataBoundConstructor
    public TelegramBotGlobalConfiguration() {
        // Load global Jenkins config
        load();
        updateConfigSimpleParams();

        // Save the loaded recipients map
        Subscribers.getInstance().setUsers(users != null ? users : new HashSet<>());

        // Descriptor object is observer for the Subscribers object
        Subscribers.getInstance().addObserver(this);

        // Run the bot after Jenkins config has been loaded
        TelegramBotRunner.getInstance().runBot();
    }

    /**
     * Called when Jenkins config is saving via web-interface
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

        // Save for the future using
        StaplerRequestContainer.req = req;

        // Getting simple params from formData
        this.shouldLogToConsole = formData.getBoolean("shouldLogToConsole");
        this.botToken = formData.getString("botToken");
        this.botName = formData.getString("botName");

        // Approve users
        UserApprover userApprover = new UserApprover(users != null ? users : new HashSet<>());
        approvalType = userApprover.approve(formData);
        users = userApprover.getUsers();

        // Store params to the global configuration
        updateConfigSimpleParams();

        // Store users
        Subscribers.getInstance().setUsers(users != null ? new HashSet<>(users) : new HashSet<>());

        // Save the configuration
        save();
        return super.configure(req, formData);
    }

    /**
     * Called when recipients object have been changed
     */
    @Override
    public void update(Observable observable, Object o) {
        try {
            users = Subscribers.getInstance().getUsers();
            save();
            super.configure(StaplerRequestContainer.req, new JSONObject());
        } catch (FormException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while saving the recipients map", e);
        }
    }

    /**
     * Update global config params using Jenkins config
     */
    private void updateConfigSimpleParams() {
        jenkinsci.plugins.telegrambot.config.GlobalConfiguration config = jenkinsci.plugins.telegrambot.config.GlobalConfiguration.getInstance();

        config.setLogToConsole(shouldLogToConsole != null ? shouldLogToConsole : true);
        config.setBotToken(botToken != null ? botToken : "");
        config.setBotName(botName != null ? botName : "");
        config.setApprovalType(approvalType != null ? approvalType : UserApprover.ApprovalType.ALL);
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
        return jenkinsci.plugins.telegrambot.config.GlobalConfiguration.PLUGIN_DISPLAY_NAME;
    }

    public Boolean shouldLogToConsole() {
        return shouldLogToConsole;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }

    public Set<User> getUsers() {
        return users;
    }

    public UserApprover.ApprovalType getApprovalType() {
        return approvalType;
    }
}
