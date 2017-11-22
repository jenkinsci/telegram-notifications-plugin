package jenkinsci.plugins.telegrambot;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import jenkinsci.plugins.telegrambot.config.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TelegramBotPublisher extends Publisher implements SimpleBuildStep {

    /**
     * The message that will be expanded and sent to users
     */
    private final String message;

    private final boolean whenSuccess;
    private final boolean whenUnstable;
    private final boolean whenFailed;
    private final boolean whenAborted;

    @DataBoundConstructor
    public TelegramBotPublisher(
            String message,
            boolean whenSuccess,
            boolean whenUnstable,
            boolean whenFailed,
            boolean whenAborted) {

        this.message = message;
        this.whenSuccess = whenSuccess;
        this.whenUnstable = whenUnstable;
        this.whenFailed = whenFailed;
        this.whenAborted = whenAborted;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public void perform(
            @Nonnull Run<?, ?> run,
            @Nonnull FilePath filePath,
            @Nonnull Launcher launcher,
            @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        Result result = run.getResult();

        boolean success  = result == Result.SUCCESS  && whenSuccess;
        boolean unstable = result == Result.UNSTABLE && whenUnstable;
        boolean failed   = result == Result.FAILURE  && whenFailed;
        boolean aborted  = result == Result.ABORTED  && whenAborted;

        boolean neededToSend = success || unstable || failed || aborted;

        if (neededToSend) {
            new TelegramBotDelegate(getMessage())
                    .perform(run, filePath, launcher, taskListener);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            return true;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return GlobalConfiguration.PLUGIN_DISPLAY_NAME;
        }
    }

    public String getMessage() {
        return message;
    }

    public boolean isWhenSuccess() {
        return whenSuccess;
    }

    public boolean isWhenUnstable() {
        return whenUnstable;
    }

    public boolean isWhenFailed() {
        return whenFailed;
    }

    public boolean isWhenAborted() {
        return whenAborted;
    }
}
