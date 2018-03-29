package jenkinsci.plugins.telegrambot.component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyPhraseSearchEngine implements SearchEngine {
    private static final String KEY_PHRASE_SEARCH_PATTERN = "\\$\\{%s,\\s*%s}";
    private static final String DEFAULT_KEY_PHRASE = "READ_FROM_FILE";
    private String keyPhrase;
    private String searchPattern;

    public KeyPhraseSearchEngine(String keyPhrase) {
        this.keyPhrase = keyPhrase == null ? getDefaultKeyPhrase() : getDefaultKeyPhraseIfEmpty(keyPhrase);
        this.searchPattern = getKeyPhraseSearchPatternWithParameters(this.keyPhrase, "(.*?)");
    }

    public KeyPhraseSearchEngine() {
        this(null);
    }


    @Override
    public List<String> getSearchingValues(@NotNull String message) {
        List<String> matchList = new ArrayList<>();
        Pattern pattern = Pattern.compile(getKeyPhraseSearchPattern(),
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            matchList.add(matcher.group(1));
        }
        return matchList;
    }

    /**
     * https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#quote%28java.lang.String%29
     */
    public String getQuotedFindKeyValue(String value) {
        return String.format("\\Q${%s,\\E\\s*\\Q%s}\\E", keyPhrase, value);


    }

    public String getKeyPhrase() {
        return keyPhrase;
    }

    public String getKeyPhraseSearchPatternWithValue(String value) {
        return getKeyPhraseSearchPatternWithParameters(keyPhrase, value);
    }

    public String getKeyPhraseSearchPattern() {
        return searchPattern;
    }

    private String getDefaultKeyPhrase() {
        return DEFAULT_KEY_PHRASE;
    }

    private String getDefaultKeyPhraseIfEmpty(String keyPhrase) {
        return keyPhrase.isEmpty() ? DEFAULT_KEY_PHRASE : keyPhrase;
    }

    private String getKeyPhraseSearchPatternWithParameters(String keyPhrase, String searchingValue) {
        return String.format(KEY_PHRASE_SEARCH_PATTERN, keyPhrase, searchingValue);
    }


}
