package jenkinsci.plugins.telegrambot;

import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import jenkinsci.plugins.telegrambot.users.Subscribers;
import jenkinsci.plugins.telegrambot.users.User;
import jenkinsci.plugins.telegrambot.users.UserApprover;
import jenkinsci.plugins.telegrambot.utils.StaplerRequestContainer;
import jenkinsci.plugins.telegrambot.utils.Utils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
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

public class TelegramBotBuilder extends Builder implements SimpleBuildStep {
    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * The message that will be expanded and sent to users
     */
    private final String message;

    @DataBoundConstructor
    public TelegramBotBuilder(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        TelegramBotDelegate delegate = new TelegramBotDelegate();
        delegate.setMessage(getMessage());
        delegate.perform(run, filePath, launcher, taskListener);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> implements Observer {
        private Boolean shouldLogToConsole;
        private String botToken;
        private String botName;
        private UserApprover.ApprovalType approvalType;
        private Set<User> users;

        /**
         * Called when Jenkins is starting and it's config is loading
         */
        public DescriptorImpl() {
            // Load global Jenkins config
            load();
            updateConfigSimpleParams();

            // Set the loaded recipients map
            Subscribers.getInstance().setUsers(users != null ? users : new HashSet<>());

            // Descriptor object is observer for the Subscribers object
            Subscribers.getInstance().addObserver(this);

            // Run the bot after Jenkins config have been loaded
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
            UserApprover userApprover = new UserApprover(users);
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
                Logger.getLogger(this.getClass()).error("Error while saving the recipients map");
            }
        }

        /**
         * Update global config params using Jenkins config
         */
        private void updateConfigSimpleParams() {
            GlobalConfiguration config = GlobalConfiguration.getInstance();

            config.setLogToConsole(shouldLogToConsole != null ? shouldLogToConsole : true);
            config.setBotToken(botToken != null ? botToken : "");
            config.setBotName(botName != null ? botName : "");
            config.setApprovalType(approvalType != null ? approvalType : UserApprover.ApprovalType.ALL);
        }

        public FormValidation doCheckMessage(@QueryParameter String value) throws IOException, ServletException {
            return Utils.checkNonEmpty(value, "Please set a message");
        }

        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return GlobalConfiguration.PLUGIN_DISPLAY_NAME;
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
}
