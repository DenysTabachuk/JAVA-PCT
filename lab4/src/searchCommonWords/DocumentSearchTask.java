package searchCommonWords;

import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;


class DocumentSearchTask extends RecursiveTask<Set<String>> {
    private final Document document;

    DocumentSearchTask(Document document) {
        super();
        this.document = document;
    }

    @Override
    protected Set<String> compute() {
          return  WordCounter.getUniqueWords(document);
    }
}