package jenkinsci.plugins.telegrambot.component;

import hudson.EnvVars;

public class NativeJenkinsMiddleware implements Middleware {
    private EnvVars environment;

    public NativeJenkinsMiddleware(EnvVars environment) {
        this.environment = environment;
    }


    @Override
    public String expand(String fromMessage) {
        return environment.expand(fromMessage);
    }
}
