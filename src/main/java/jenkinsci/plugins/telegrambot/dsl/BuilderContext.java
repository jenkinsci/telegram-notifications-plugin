package jenkinsci.plugins.telegrambot.dsl;

import javaposse.jobdsl.dsl.Context;

public class BuilderContext implements Context {
    String message;

    public void message(String message) {
        this.message = message;
    }
}
