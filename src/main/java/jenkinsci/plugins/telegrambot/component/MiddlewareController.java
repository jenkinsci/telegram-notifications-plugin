package jenkinsci.plugins.telegrambot.component;

public interface MiddlewareController {
    MiddlewareController linkWith(Middleware next);

    String transform(String message);
}
