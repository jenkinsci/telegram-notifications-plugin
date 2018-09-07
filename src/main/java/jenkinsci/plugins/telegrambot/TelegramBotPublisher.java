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
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TelegramBotPublisher extends Notifier implements SimpleBuildStep {

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

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return TelegramBotGlobalConfiguration.PLUGIN_DISPLAY_NAME;
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
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
            TelegramBotRunner.getInstance().getBot()
                    .sendMessage(getMessage(), run, filePath, taskListener);
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
