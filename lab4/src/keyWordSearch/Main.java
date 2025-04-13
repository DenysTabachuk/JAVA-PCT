package keyWordSearch;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final String FOLDER_PATH = "lab4\\src\\keyWordSearch\\documents";
        final List<String> keyWords = List.of("Java", "дані");

//        FolderDocumentGenerator.generateFolderStructure(FOLDER_PATH, 1024, 4);
        System.out.println("Пошук за словами: " + keyWords);

        WordCounter wordCounter = new WordCounter();
        Folder folder = Folder.fromDirectory(new File(FOLDER_PATH));

        List<String> fileNames = wordCounter.findDocsByKeyWords(folder, keyWords);
        System.out.println("\nФайлів, що містять в собі принаймні 1 ключове слово " + fileNames.size() + " :");
        printFileNames(fileNames);

        fileNames = wordCounter.findDocsByKeyWordsStrictMode(folder, keyWords);
        System.out.println("\n\n\nФайлів, що містять в собі всі ключові слова " + fileNames.size() + " :");
        printFileNames(fileNames);
    }

    private static void printFileNames(List<String> fileNames) {
        int count = 0;
        for (String fileName : fileNames) {
            System.out.print(fileName + "\t");
            count++;

            if (count % 5 == 0) {
                System.out.println();
            }
        }
        if (count % 5 != 0) {
            System.out.println();
        }
    }
}
