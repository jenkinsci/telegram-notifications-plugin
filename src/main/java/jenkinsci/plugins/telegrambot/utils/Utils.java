package jenkinsci.plugins.telegrambot.utils;

import hudson.util.FormValidation;

public class Utils {

    public static FormValidation checkNonEmpty(String value, String message) {
        return value.length() == 0 ? FormValidation.error(message) : FormValidation.ok();
    }
}
