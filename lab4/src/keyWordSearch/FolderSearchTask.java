package keyWordSearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

class FolderSearchTask extends RecursiveTask<List<String>> {
    private final Folder folder;
    private final List<String> keyWords;
    private final boolean strictMode;

    public FolderSearchTask(Folder folder, List<String> keyWords, Boolean strictMode) {
        super();
        this.folder = folder;
        this.keyWords = keyWords;
        this.strictMode = strictMode;
    }

    @Override
    protected List<String> compute() {
        List<String> fileNames = new ArrayList<>();

        List<RecursiveTask<List<String>>> forks = new LinkedList<>();

        for (Folder subFolder : folder.getSubFolders()){
            FolderSearchTask task = new FolderSearchTask(subFolder, keyWords, strictMode);
            forks.add(task);
            task.fork();
        }

        for (Document document : folder.getDocuments()) {
            DocumentSearchTask task = new DocumentSearchTask(document, keyWords, strictMode);
            forks.add(task);
            task.fork();
        }

        for (RecursiveTask<List<String>> task : forks) {
            List<String> subResult = task.join();
            if (subResult != null){
                fileNames.addAll(subResult);
            }
        }

        return fileNames;
    }
}
