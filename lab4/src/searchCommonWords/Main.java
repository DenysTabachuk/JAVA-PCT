package searchCommonWords;

import java.io.File;
import java.io.IOException;

// 3. Розробіть та реалізуйте алгоритм пошуку спільних слів в текстових документах з використанням ForkJoinFramework. 20 балів.
// Тобто на виході маємо повернути ті слова, які є у всіх файлах

public class Main {
    public static void main(String[] args) throws IOException {
        final String FOLDER_PATH = "lab4\\src\\searchCommonWords\\documents";
//        FolderDocumentGenerator.generateFolderStructure(FOLDER_PATH, 1024, 4);

        WordCounter wordCounter = new WordCounter();
        Folder folder = Folder.fromDirectory(new File(FOLDER_PATH));

        System.out.println("Спільні слова для всіх документів:" + wordCounter.getCommonWords(folder) );
    }
}
