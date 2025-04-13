package keyWordSearch;

import java.util.*;
import java.util.concurrent.*;

public class WordCounter {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    public static boolean documentHasAtLeastOneKeyWord(Document document, List<String> keyWords) {
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (keyWords.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean documentHasAllKeyWords(Document document, List<String> keyWords) {
        Set<String> foundWords = new HashSet<>();

        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (keyWords.contains(word)) {
                    foundWords.add(word);
                }
            }
        }

        return foundWords.containsAll(keyWords);
    }

    public List<String> findDocsByKeyWords(Folder folder, List<String> keyWords){
        return forkJoinPool.invoke(new FolderSearchTask(folder, keyWords, false));
    }

    public List<String> findDocsByKeyWordsStrictMode(Folder folder, List<String> keyWords){
        return forkJoinPool.invoke(new FolderSearchTask(folder, keyWords, true));
    }
}