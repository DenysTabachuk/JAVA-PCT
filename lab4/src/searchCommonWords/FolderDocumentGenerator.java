package searchCommonWords;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FolderDocumentGenerator {
    private static final Random random = new Random();

    private static final String[] COMMON_WORDS = {
            "Java", "код", "файл"
    };

    private static final String[] OTHER_WORDS = {
            "літо", "сонце", "дослід", "машина", "слово", "аналітика", "структура", "весна", "зима", "дані", "мова", "об'єкт"
    };

    public static void generateFolderStructure(String rootPath, int totalFiles, int maxDepth) throws IOException {
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }

        generateRecursive(root, totalFiles, maxDepth, 0);
    }

    private static void generateRecursive(File folder, int filesRemaining, int maxDepth, int currentDepth) throws IOException {
        int filesInThisFolder = Math.min(filesRemaining, 2 + random.nextInt(3)); // 2-4 файли в папці

        for (int i = 0; i < filesInThisFolder; i++) {
            File file = new File(folder, "doc" + System.nanoTime() + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(generateControlledText());
            }
        }

        int remaining = filesRemaining - filesInThisFolder;
        if (remaining <= 0 || currentDepth >= maxDepth) return;

        int subFolders = 1 + random.nextInt(2); // 1-2 підпапки
        for (int i = 0; i < subFolders; i++) {
            File subFolder = new File(folder, "subfolder_" + i + "_" + System.nanoTime());
            subFolder.mkdir();

            int filesForSubfolder = remaining / subFolders;
            generateRecursive(subFolder, filesForSubfolder, maxDepth, currentDepth + 1);
        }
    }

    private static String generateControlledText() {
        int totalWords = 500;
        StringBuilder text = new StringBuilder();

        // Обов'язково додаємо спільні слова кілька разів
        for (int i = 0; i < 20; i++) {
            String word = COMMON_WORDS[random.nextInt(COMMON_WORDS.length)];
            text.append(word).append(" ");
            if (i % 10 == 0) text.append("\n");
        }

        // Створюємо випадкову підмножину OTHER_WORDS
        Set<String> subset = getRandomSubset(OTHER_WORDS, 4 + random.nextInt(OTHER_WORDS.length - 4));

        // Додаємо їх у решту тексту
        for (int i = 0; i < totalWords - 20; i++) {
            String[] pool = subset.toArray(new String[0]);
            String word = pool[random.nextInt(pool.length)];
            text.append(word).append(" ");
            if (i % 10 == 0) text.append("\n");
        }

        return text.toString();
    }

    private static Set<String> getRandomSubset(String[] array, int subsetSize) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        Collections.shuffle(list);
        return new HashSet<>(list.subList(0, subsetSize));
    }
}
