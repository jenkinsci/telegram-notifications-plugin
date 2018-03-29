package jenkinsci.plugins.telegrambot.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultMiddlewareController implements MiddlewareController {
    private String message;
    private List<Middleware> middlewareSubscribers = new ArrayList<>();

    public MiddlewareController linkWith(Middleware next) {
        middlewareSubscribers.add(next);
        return this;
    }


    @Override
    public String transform(String message) {
        this.message = message;
        Iterator<Middleware> middlewareIterator = middlewareSubscribers.iterator();
        while (middlewareIterator.hasNext()) {
            this.message = middlewareIterator.next().expand(this.message);
        }
        return this.message;
    }


}
