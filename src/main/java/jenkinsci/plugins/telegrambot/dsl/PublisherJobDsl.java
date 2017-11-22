package jenkinsci.plugins.telegrambot.dsl;

import jenkinsci.plugins.telegrambot.TelegramBotPublisher;
import hudson.Extension;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class PublisherJobDsl extends ContextExtensionPoint {

    @DslExtensionMethod(context = javaposse.jobdsl.dsl.helpers.publisher.PublisherContext.class)
    public Object telegramBot(Runnable closure) {
        PublisherContext context = new PublisherContext();
        executeInContext(closure, context);

        return new TelegramBotPublisher(
                context.message,
                context.whenSuccess,
                context.whenUnstable,
                context.whenFailed,
                context.whenAborted);
    }
}
