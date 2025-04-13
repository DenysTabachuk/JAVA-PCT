package keyWordSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;


class DocumentSearchTask extends RecursiveTask<List<String>> {
    private final Document document;
    private final List<String> searchWords;
    private final Boolean strictMode;

    DocumentSearchTask(Document document, List<String> searchWords, Boolean strictMode) {
        super();
        this.document = document;
        this.searchWords = searchWords;
        this.strictMode = strictMode;
    }

    @Override
    protected List<String> compute() {
        boolean matches;

        if (strictMode) {
            matches = WordCounter.documentHasAllKeyWords(document, searchWords);
        } else {
            matches = WordCounter.documentHasAtLeastOneKeyWord(document, searchWords);
        }

        if (matches) {
            List<String> matchedDoc = new ArrayList<>();
            matchedDoc.add(document.getName());
            return matchedDoc;
        } else {
            return null;
        }
    }
}