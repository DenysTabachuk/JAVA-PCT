import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class AverageWordLength {
    private static final int THRESHOLD = 100;
    private static final int NUM_WORDS = 100000;

    public static void main(String[] args) {
        List<String> wordsPool = List.of("apple", "banana", "grape", "orange", "melon", "pear", "kiwi");

        List<String> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < NUM_WORDS; i++) {
            String randomWord = wordsPool.get(random.nextInt(wordsPool.size()));
            list.add(randomWord);
        }

        ForkJoinPool pool = new ForkJoinPool();
        double averageLength = pool.invoke(new WordLengthTask(list, 0, list.size()));

        System.out.println("Середня довжина слова: " + averageLength);
    }

    static class WordLengthTask extends RecursiveTask<Double> {
        private final List<String> list;
        private final int start;
        private final int end;

        WordLengthTask(List<String> list, int start, int end) {
            this.list = list;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Double compute() {
            if (end - start <= THRESHOLD) {
                double totalLength = 0;
                int count = 0;
                for (int i = start; i < end; i++) {
                    totalLength += list.get(i).length();
                    count++;
                }
                return totalLength / count;
            } else {
                int mid = (start + end) / 2;
                WordLengthTask leftTask = new WordLengthTask(list, start, mid);
                WordLengthTask rightTask = new WordLengthTask(list, mid, end);

                leftTask.fork();
                double rightResult = rightTask.compute();
                double leftResult = leftTask.join();

                return (leftResult + rightResult) / 2;
            }
        }
    }
}
