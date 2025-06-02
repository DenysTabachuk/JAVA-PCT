package matrix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FoxAlgorithm {
        public static Matrix multiply(Matrix A, Matrix B, int numThreads) {
        int size = A.getRows();

        if (size != A.getCols() || size != B.getRows() || size != B.getCols()) {
            throw new IllegalArgumentException("Fox algorithm requires square matrices of the same size.");
        }

        int[][] result = new int[size][size];

        int blockSize = (int) Math.ceil((double) size / Math.sqrt(numThreads));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < size; i += blockSize) {
            for (int j = 0; j < size; j += blockSize) {
                for (int k = 0; k < size; k += blockSize) {
                    executor.execute(new BlockMultiplication(A, B, result, i, j, k, blockSize));
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Matrix(result);
    }

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
            for (int i = rowBlock; i < Math.min(rowBlock + blockSize, A.getRows()); i++) {
                for (int j = colBlock; j < Math.min(colBlock + blockSize, B.getCols()); j++) {
                    int sum = 0;

                    for (int k = kBlock; k < Math.min(kBlock + blockSize, A.getCols()); k++) {
                        sum += A.getValue(i, k) * B.getValue(k, j);
                    }

                    synchronized (result) {
                        result[i][j] += sum;
                    }
                }
            }
        }
    }
}

