package wordLength;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

class FolderSearchTask extends RecursiveTask<List<Integer>> {
    private final Folder folder;

    FolderSearchTask(Folder folder) {
        super();
        this.folder = folder;
    }

    @Override
    protected List<Integer> compute() {
        List<Integer> lengths = new ArrayList<>();

        List<RecursiveTask<List<Integer>>> forks = new LinkedList<>();
        for (Folder subFolder : folder.getSubFolders()) {
            FolderSearchTask task = new FolderSearchTask(subFolder);
            forks.add(task);
            task.fork();
        }
        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(document);
            forks.add(task);
            task.fork();
        }
        for (RecursiveTask<List<Integer>> task : forks) {
            List<Integer> subResult = task.join();
            lengths.addAll(subResult);
        }
        return lengths;
    }
}