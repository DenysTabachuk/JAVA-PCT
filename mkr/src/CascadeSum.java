import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class CascadeSum {

    private static final int THRESHOLD = 1000;  // Поріг, до якого буде виконуватися просте сумування

    public static void main(String[] args) {
        double[] numbers = new double[100000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Math.random();
        }


        ForkJoinPool pool = new ForkJoinPool(10); // 10 потоків
        double sum = pool.invoke(new SumTask(numbers, 0, numbers.length));

        System.out.println("Сума елементів: " + sum);
    }


    static class SumTask extends RecursiveTask<Double> {
        private final double[] array;
        private final int start;
        private final int end;

        SumTask(double[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Double compute() {
            // Якщо частина масиву менша за поріг, виконуємо просте сумування
            if (end - start <= THRESHOLD) {
                double localSum = 0;
                for (int i = start; i < end; i++) {
                    localSum += array[i];
                }
                return localSum;
            } else {
                int mid = (start + end) / 2;
                SumTask leftTask = new SumTask(array, start, mid);
                SumTask rightTask = new SumTask(array, mid, end);

                leftTask.fork();
                double rightResult = rightTask.compute();
                double leftResult = leftTask.join();

                return leftResult + rightResult;
            }
        }
    }
}
