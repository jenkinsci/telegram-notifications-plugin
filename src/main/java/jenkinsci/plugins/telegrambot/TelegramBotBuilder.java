package jenkinsci.plugins.telegrambot;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import jenkinsci.plugins.telegrambot.telegram.TelegramBotRunner;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class TelegramBotBuilder extends Builder implements SimpleBuildStep {

    /**
     * The message that will be expanded and sent to users
     */
    private final String message;

    @DataBoundConstructor
    public TelegramBotBuilder(String message) {
        this.message = message;
    }

    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {
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

    public String getMessage() {
        return message;
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

}
