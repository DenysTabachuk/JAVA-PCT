package searchCommonWords;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class WordCounter {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    public static Set<String> getUniqueWords(Document document){
        Set<String> uniqueWords = new HashSet<>();
        for (String line : document.getLines()){
            uniqueWords.addAll(List.of(wordsIn(line)));
        }
        return uniqueWords;
    }

    public Set<String>  getCommonWords(Folder folder) {
        return forkJoinPool.invoke(new FolderSearchTask(folder));
    }
}