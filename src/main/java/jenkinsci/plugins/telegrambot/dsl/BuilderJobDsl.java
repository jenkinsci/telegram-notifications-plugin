package jenkinsci.plugins.telegrambot.dsl;

import jenkinsci.plugins.telegrambot.TelegramBotBuilder;
import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class BuilderJobDsl extends ContextExtensionPoint {

    @DslExtensionMethod(context = StepContext.class)
    public Object telegramBot(Runnable closure) {
        BuilderContext context = new BuilderContext();
        executeInContext(closure, context);

        return new TelegramBotBuilder(context.message);
    }
}
