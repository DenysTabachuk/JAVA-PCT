package wordLength;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WordCounter {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    static List<Integer> wordLengths(Document document){
        List<Integer> lengths = new ArrayList<>();

        for (String line: document.getLines()){
            for (String word : wordsIn(line)){
                lengths.add(word.length());
            }
        }

        return lengths;
    }

    List<Integer> countWordLengthsParallel(Folder folder) {
        return forkJoinPool.invoke(new FolderSearchTask(folder));
    }

    List<Integer> countWordLengthSingleThread(Folder folder) {
        List<Integer> lengths = new ArrayList<>();
        for (Folder subFolder : folder.getSubFolders()) {
            List<Integer> subResult = countWordLengthSingleThread(subFolder);
            lengths.addAll(subResult);
        }
        for (Document document : folder.getDocuments()) {
            List<Integer> subResult = wordLengths(document);
            lengths.addAll(subResult);
        }
        return lengths;
    }
}