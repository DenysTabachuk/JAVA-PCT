import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StrippedAlgorithm {

    public static Result multiply(Matrix matrixA, Matrix matrixB, int numThreads) {
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        Matrix resultMatrix = new Matrix(matrixA.getMatrixSize());

        for (int i = 0; i < matrixA.getMatrixSize(); i++) {
            int finalI = i;
            futures.add(executor.submit(new StripedAlgorithmThread(matrixA.getRow(finalI), finalI, matrixB, resultMatrix)));
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

        return new Result(resultMatrix, duration);
    }

    public static class StripedAlgorithmThread implements Runnable {
        private int[] row;
        private int rowIndex;
        private Matrix matrixB;
        private Matrix resultMatrix;

        public StripedAlgorithmThread(int[] row, int rowIndex, Matrix matrixB, Matrix resultMatrix) {
            this.row = row;
            this.rowIndex = rowIndex;
            this.resultMatrix = resultMatrix;
            this.matrixB = matrixB;
        }

        @Override
        public void run() {
            for (int i = 0; i < matrixB.getMatrixSize(); i++) {
                int result = 0;
                int[] column = matrixB.getColumn(i);

                for (int j = 0; j < row.length; j++) {
                    result += row[j] * column[j];
                }

                resultMatrix.setElement(rowIndex, i, result);
            }
        }
    }
}
