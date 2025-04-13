package speedUp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FoxAlgorithmThreadPool {

    public static Result multiply(Matrix matrixA, Matrix matrixB, int nThreads) {
        long startTime = System.nanoTime();

        int n = matrixA.getMatrixSize();

        int[][] resultMatrix = new int[n][n];

        int numThreads = Math.min(nThreads, matrixA.getMatrixSize());
        int blockSize = n / numThreads;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int threadRow = 0; threadRow < numThreads; threadRow++) {
            for (int threadCol = 0; threadCol < numThreads; threadCol++) {
                final int finalThreadRow = threadRow;
                final int finalThreadCol = threadCol;

                futures.add(executor.submit(() -> multiplyBlockInThread(matrixA, matrixB, resultMatrix, finalThreadRow, finalThreadCol, blockSize, numThreads)));
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        return new Result(new Matrix(resultMatrix), duration);
    }

    private static void multiplyBlockInThread(Matrix matrixA, Matrix matrixB, int[][] resultMatrix, int threadRow, int threadCol, int blockSize, int numThreads) {
        for (int k = 0; k < numThreads; k++) {
            int rowStart = threadRow * blockSize;
            int colStart = threadCol * blockSize;
            int kRowStart = k * blockSize;
            int kColStart = k * blockSize;

            multiplyBlock(matrixA.getMatrix(), matrixB.getMatrix(), resultMatrix,
                    rowStart, colStart,
                    kRowStart, kColStart,
                    blockSize);
        }
    }

    private static void multiplyBlock(
            int[][] blockA, int[][] blockB, int[][] resultBlock,
            int rowStart, int colStart,
            int kRowStart, int kColStart,
            int blockSize
    ) {
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                int sum = 0;
                for (int k = 0; k < blockSize; k++) {
                    sum += blockA[rowStart + i][kRowStart + k] *
                            blockB[kColStart + k][colStart + j];
                }
                resultBlock[rowStart + i][colStart + j] += sum;
            }
        }
    }
}
