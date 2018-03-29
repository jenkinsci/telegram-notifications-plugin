package jenkinsci.plugins.telegrambot.component;

import java.util.List;

public interface SearchEngine {

    List<String> getSearchingValues(String fromMessage);
    String getKeyPhrase();
    String getKeyPhraseSearchPattern();
    String getKeyPhraseSearchPatternWithValue(String value);
    String getQuotedFindKeyValue(String value);
}
