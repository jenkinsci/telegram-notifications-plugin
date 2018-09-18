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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TelegramBotMessager extends Notifier implements SimpleBuildStep {

    /**
     * The message that will be expanded and sent to users
     */
    private final String message;


    @DataBoundConstructor
    public TelegramBotMessager(
            String message) {

        this.message = message;
    }

    @Extension @Symbol("telegramSend")
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

            TelegramBotRunner.getInstance().getBot()
                    .sendMessage(getMessage(), run, filePath, taskListener);

    }

    public String getMessage() {
        return message;
    }


}
