package wordLength;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistogramPlotter {

    public static void printHistogram(List<Integer> wordLengths) {

        Map<Integer, Integer> histogram = buildHistogram(wordLengths);

        System.out.println("Гістограма довжин слів:\n");
        int maxCount = histogram.values().stream().max(Integer::compareTo).orElse(1);
        int maxStars = 50; // максимум зірочок у одному рядку

        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            int length = entry.getKey();
            int count = entry.getValue();

            int stars = (int) Math.round((double) count / maxCount * maxStars);

            System.out.printf("%2d: ", length);
            for (int i = 0; i < stars; i++) {
                System.out.print("*");
            }
            System.out.printf(" (%d)\n", count);
        }
    }

    public static Map<Integer, Integer> buildHistogram(List<Integer> wordLengths) {
        Map<Integer, Integer> histogram = new HashMap<>();
        for (int length : wordLengths) {
            histogram.put(length, histogram.getOrDefault(length, 0) + 1);
        }
        return histogram;
    }
}
