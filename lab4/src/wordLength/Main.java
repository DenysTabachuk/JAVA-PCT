package wordLength;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final String FOLDER_PATH = "lab4\\src\\wordLength\\documents";
        final int NUM_REPETITIONS = 5;

        int[] fileCounts = {20, 100, 500, 1000, 5000};
        int[] wordCounts = {500, 2500, 5000};

        System.out.printf(
                "%-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s %-15s\n",
                "Files", "Words/File", "Avg (Seq)", "StdDev (Seq)", "Time (Seq)",
                "Avg (Par)", "StdDev (Par)", "Time (Par)", "SpeedUp", "Efficiency"
        );

        for (int totalFiles : fileCounts) {
            for (int wordsPerFile : wordCounts) {
                // Генерація документів
                FolderDocumentGenerator.generateFolderStructure(FOLDER_PATH, totalFiles, 5, wordsPerFile);

                WordCounter wordCounter = new WordCounter();
                Folder folder = Folder.fromDirectory(new File(FOLDER_PATH));

                long totalParallelTime = 0;
                long totalSequentialTime = 0;

                StatsResult statsResultParallel = null;
                StatsResult statsResultSequential = null;

                for (int i = 0; i < NUM_REPETITIONS; i++) {
                    long startParallel = System.currentTimeMillis();
                    List<Integer> wordLengthsParallel = wordCounter.countWordLengthsParallel(folder);
                    statsResultParallel = StatAnalysisTask.computeStatisticsParallel(wordLengthsParallel);

                    long endParallel = System.currentTimeMillis();
                    totalParallelTime += (endParallel - startParallel);

                    long startSequential = System.currentTimeMillis();
                    List<Integer> wordLengthsSequential = wordCounter.countWordLengthSingleThread(folder);
                    statsResultSequential = StatAnalysisTask.computeStatisticsSequential(wordLengthsSequential);
                    long endSequential = System.currentTimeMillis();
                    totalSequentialTime += (endSequential - startSequential);
                }

                long avgParallelTime = totalParallelTime / NUM_REPETITIONS;
                long avgSequentialTime = totalSequentialTime / NUM_REPETITIONS;

                double speedUp = (double) avgSequentialTime / avgParallelTime;
                int numThreads = Runtime.getRuntime().availableProcessors();
                double efficiency = speedUp / numThreads * 100;

                System.out.printf(
                        "%-15d %-15d %-15.2f %-15.2f %-15d %-15.2f %-15.2f %-15d %-15.2f %-15.2f\n",
                        totalFiles, wordsPerFile,
                        statsResultSequential.getAverage(), statsResultSequential.getStandardDeviation(), avgSequentialTime,
                        statsResultParallel.getAverage(), statsResultParallel.getStandardDeviation(), avgParallelTime,
                        speedUp, efficiency
                );

                // Будуємо і виводимо гістограму лише для випадку, коли
                // totalFiles є максимальним (останній елемент fileCounts) і wordsPerFile є максимальним (останній елемент wordCounts)
                if (totalFiles == fileCounts[fileCounts.length - 1] &&
                        wordsPerFile == wordCounts[wordCounts.length - 1]) {
                    List<Integer> wordLengths = wordCounter.countWordLengthSingleThread(folder);
                    System.out.println("\nГістограма для Words/File = " + wordsPerFile + ":");
                    HistogramPlotter.printHistogram(wordLengths);
                    System.out.println();
                }
            }
            System.out.println();
        }
    }
}
