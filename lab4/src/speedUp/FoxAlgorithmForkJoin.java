package speedUp;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FoxAlgorithmForkJoin {

    public static Result multiply(Matrix matrixA, Matrix matrixB, int nThreads) {
        long startTime = System.nanoTime();

        int n = matrixA.getMatrixSize();
        int[][] resultMatrix = new int[n][n];
        int blockSize = n / nThreads;

        ForkJoinPool pool = new ForkJoinPool(nThreads);
        pool.invoke(new FoxMainTask(matrixA, matrixB, resultMatrix, blockSize, nThreads));

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        return new Result(new Matrix(resultMatrix), duration);
    }

    static class FoxMainTask extends RecursiveAction {
        private final Matrix A, B;
        private final int[][] result;
        private final int blockSize;
        private final int numThreads;

        public FoxMainTask(Matrix A, Matrix B, int[][] result, int blockSize, int numThreads) {
            this.A = A;
            this.B = B;
            this.result = result;
            this.blockSize = blockSize;
            this.numThreads = numThreads;
        }

        @Override
        protected void compute() {
            RecursiveAction[] tasks = new RecursiveAction[numThreads * numThreads];
            int idx = 0;
            for (int i = 0; i < numThreads; i++) {
                for (int j = 0; j < numThreads; j++) {
                    tasks[idx++] = new FoxBlockTask(A, B, result, i, j, blockSize, numThreads);
                }
            }
            invokeAll(tasks);
        }
    }

    static class FoxBlockTask extends RecursiveAction {
        private final Matrix A, B;
        private final int[][] result;
        private final int threadRow, threadCol;
        private final int blockSize;
        private final int numThreads;

        public FoxBlockTask(Matrix A, Matrix B, int[][] result,
                            int threadRow, int threadCol, int blockSize, int numThreads) {
            this.A = A;
            this.B = B;
            this.result = result;
            this.threadRow = threadRow;
            this.threadCol = threadCol;
            this.blockSize = blockSize;
            this.numThreads = numThreads;
        }

        @Override
        protected void compute() {
            int[][] aMatrix = A.getMatrix();
            int[][] bMatrix = B.getMatrix();

            for (int k = 0; k < numThreads; k++) {
                int rowStart = threadRow * blockSize;
                int colStart = threadCol * blockSize;

                int aRowStart = threadRow * blockSize;
                int aColStart = ((threadRow + k) % numThreads) * blockSize;

                int bRowStart = ((threadRow + k) % numThreads) * blockSize;
                int bColStart = threadCol * blockSize;

                multiplyBlock(
                        aMatrix, bMatrix, result,
                        rowStart, colStart,
                        aRowStart, aColStart,
                        bRowStart, bColStart,
                        blockSize
                );
            }
        }


        public static void multiplyBlock(
                int[][] A, int[][] B, int[][] C,
                int rowStart, int colStart,
                int aRowStart, int aColStart,
                int bRowStart, int bColStart,
                int blockSize) {

            for (int i = 0; i < blockSize; i++) {
                for (int j = 0; j < blockSize; j++) {
                    for (int k = 0; k < blockSize; k++) {
                        C[rowStart + i][colStart + j] +=
                                A[aRowStart + i][aColStart + k] *
                                        B[bRowStart + k][bColStart + j];
                    }
                }
            }
        }
    }
}
