package Matrix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FoxAlgorithm {

    public static Matrix multiply(Matrix A, Matrix B, int numThreads) {
        int size = A.getRows();

        // Перевірка, що матриці квадратні та однакового розміру
        if (size != A.getCols() || size != B.getRows() || size != B.getCols()) {
            throw new IllegalArgumentException("Алгоритм Фокса вимагає квадратні матриці однакового розміру.");
        }

        int[][] result = new int[size][size];

        // Обчислення розміру блоку: ділимо матрицю на підматриці відповідно до кількості потоків
        int blockSize = (int) Math.ceil((double) size / Math.sqrt(numThreads));

        // Створення пулу потоків для паралельного виконання завдань
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Запускаємо множення блоків паралельно: для кожного блоку в матрицях A і B
        for (int i = 0; i < size; i += blockSize) {
            for (int j = 0; j < size; j += blockSize) {
                for (int k = 0; k < size; k += blockSize) {
                    executor.execute(new BlockMultiplication(A, B, result, i, j, k, blockSize));
                }
            }
        }

        executor.shutdown();  // Закриваємо пул
        try {
            // Очікуємо завершення всіх потоків
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Обробка множення блоків була перервана: " + e.getMessage());
            e.printStackTrace();
        }

        return new Matrix(result);
    }

    // Внутрішній клас для множення блоку матриць
    private static class BlockMultiplication implements Runnable {
        private final Matrix A;
        private final Matrix B;
        private final int[][] result;
        private final int rowBlock;
        private final int colBlock;
        private final int kBlock;
        private final int blockSize;

        public BlockMultiplication(Matrix A, Matrix B, int[][] result, int rowBlock, int colBlock, int kBlock, int blockSize) {
            this.A = A;
            this.B = B;
            this.result = result;
            this.rowBlock = rowBlock;
            this.colBlock = colBlock;
            this.kBlock = kBlock;
            this.blockSize = blockSize;
        }

        @Override
        public void run() {
            // Обчислюємо блок добутку для відповідного підмасиву матриць
            for (int i = rowBlock; i < Math.min(rowBlock + blockSize, A.getRows()); i++) {
                for (int j = colBlock; j < Math.min(colBlock + blockSize, B.getCols()); j++) {
                    int sum = 0;

                    // Сума добутків елементів блоку
                    for (int k = kBlock; k < Math.min(kBlock + blockSize, A.getCols()); k++) {
                        sum += A.getValue(i, k) * B.getValue(k, j);
                    }

                    // Синхронізований запис у спільний результат, щоб уникнути конфліктів потоків
                    synchronized (result) {
                        result[i][j] += sum;
                    }
                }
            }
        }
    }
}
