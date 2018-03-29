package jenkinsci.plugins.telegrambot.component;

public class ExtensionMessageTransformer {
    private MiddlewareController middleware;

    public ExtensionMessageTransformer(MiddlewareController middleware) {
        this.middleware = middleware;

    }

    public String transform(String message) {
        return middleware.transform(message);
    }

}
