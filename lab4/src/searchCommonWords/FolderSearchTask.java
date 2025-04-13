package searchCommonWords;

import java.util.*;
import java.util.concurrent.RecursiveTask;

class FolderSearchTask extends RecursiveTask<Set<String>> {
    private final Folder folder;

    public FolderSearchTask(Folder folder) {
        super();
        this.folder = folder;
    }

    @Override
    protected Set<String> compute() {
        Set<String> commonWords = null;

        List<RecursiveTask<Set<String>>> forks = new LinkedList<>();

        for (Folder subFolder : folder.getSubFolders()){
            FolderSearchTask task = new FolderSearchTask(subFolder);
            forks.add(task);
            task.fork();
        }

        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(document);
            forks.add(task);
            task.fork();
        }

        for (RecursiveTask<Set<String>> task : forks) {
            Set<String> subResult = task.join();
            // Ініціалізуємо commonWords першим результатом, інакше спільних слів не буде
            if (commonWords == null) {
                commonWords = subResult;
            } else {
                // Знаходимо перетин (спільні слова)
                commonWords.retainAll(subResult);
            }
        }

        return commonWords;
    }
}
