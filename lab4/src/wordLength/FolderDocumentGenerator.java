package wordLength;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FolderDocumentGenerator {
    private static final Random random = new Random();

    private static final String[] WORDS = {
            "літо", "сонце", "Java", "код", "дослід", "машина", "слово", "аналітика", "файл", "структура" , "слово"
    };

    public static void generateFolderStructure(String rootPath, int totalFiles, int maxDepth, int wordsPerFile) throws IOException {
        File root = new File(rootPath);

        if (root.exists()) {
            deleteFolder(root);
        }

        root.mkdirs();
        generateRecursive(root, totalFiles, maxDepth, 0, wordsPerFile);
    }

    private static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        folder.delete();
    }

    private static void generateRecursive(File folder, int filesRemaining, int maxDepth, int currentDepth, int wordsPerFile) throws IOException {
        int filesInThisFolder = Math.min(filesRemaining, 5 + random.nextInt(5)); // 5-10 файлів у папці

        for (int i = 0; i < filesInThisFolder; i++) {
            File file = new File(folder, "doc" + System.nanoTime() + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(generateRandomText(wordsPerFile));
            }
        }

        int remaining = filesRemaining - filesInThisFolder;
        if (remaining <= 0 || currentDepth >= maxDepth) return;

        int subFolders = 1 + random.nextInt(3);
        for (int i = 0; i < subFolders; i++) {
            File subFolder = new File(folder, "subfolder_" + i);
            subFolder.mkdir();

            int filesForSubfolder = remaining / subFolders;
            generateRecursive(subFolder, filesForSubfolder, maxDepth, currentDepth + 1, wordsPerFile);
        }
    }

    private static String generateRandomText(int wordsPerFile) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < wordsPerFile; i++) {
            String word = WORDS[random.nextInt(WORDS.length)];
            text.append(word).append(" ");
            if (i % 10 == 0) text.append("\n"); // Додаємо переведення рядка через кожні 10 слів
        }
        return text.toString();
    }
}
