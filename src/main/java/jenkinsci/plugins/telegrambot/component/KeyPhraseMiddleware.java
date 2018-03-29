package jenkinsci.plugins.telegrambot.component;

import hudson.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class KeyPhraseMiddleware implements Middleware {
    private static final Logger LOGGER = Logger.getLogger(KeyPhraseMiddleware.class.getName());
    private SearchEngine keyPhraseSE;

    public KeyPhraseMiddleware() {
        this(null);
    }

    public KeyPhraseMiddleware(String keyPhrase) {
        keyPhraseSE = new KeyPhraseSearchEngine(keyPhrase);
    }

    @Override
    public String expand(String fromMessage) {
        String message = fromMessage;
        List<String> searchingValues = keyPhraseSE.getSearchingValues(fromMessage);
        for (String resultPath : searchingValues) {
            try {
                String quotedFindKeyValue = keyPhraseSE.getQuotedFindKeyValue(resultPath);
                message = message.replaceAll(quotedFindKeyValue, Util.loadFile(new File(resultPath)));
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.info(String.format("File %s is %s", resultPath, e.getLocalizedMessage()));
            }
        }
        return message;
    }
}
