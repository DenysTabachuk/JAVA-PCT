package wordLength;

import java.util.List;
import java.util.concurrent.RecursiveTask;

class DocumentSearchTask extends RecursiveTask<List<Integer>> {
    private final Document document;

    DocumentSearchTask(Document document) {
        super();
        this.document = document;
    }

    @Override
    protected List<Integer> compute() {
          return WordCounter.wordLengths(document);
    }
}