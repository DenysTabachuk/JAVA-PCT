package speedUp;

public class SpeedUpTest {
    public static void main(String[] args){
        final int[] MATRIX_SIZES = {128, 256, 512, 1024};
        final int[] NUM_THREADS = {1, 2, 4, 8, 16, 32};
        final int TABLE_WIDTH = 130;

        System.out.println("Matrix Multiplication Performance Tests\n");

        for (int size : MATRIX_SIZES) {
            System.out.println("-".repeat(TABLE_WIDTH));
            System.out.printf(
                    "%-10s%-10s%-15s%-18s%-8s%-10s%-18s%-8s%-10s\n",
                    "Matrix", "Threads", "Simple(s)",
                    "ForkJoin(s)", "✓", "Speedup",
                    "ThreadPool(s)", "✓", "Speedup"
            );
            System.out.println("-".repeat(TABLE_WIDTH));

            Matrix matrixA = new Matrix(size);
            Matrix matrixB = new Matrix(size);

            matrixA.fillWithRandomNumbers();
            matrixB.fillWithRandomNumbers();

            Result resultSimple = SimpleMatrixMultiplication.multiply(matrixA, matrixB);
            double simpleTimeSec = resultSimple.getCalculationTime() / 1_000_000_000.0;

            for (int numThreads : NUM_THREADS) {
                Result resultFoxForkJoin = FoxAlgorithmForkJoin.multiply(matrixA, matrixB, numThreads);
                Result resultFoxThreadPool = FoxAlgorithmThreadPool.multiply(matrixA, matrixB, numThreads);

                double forkJoinTimeSec = resultFoxForkJoin.getCalculationTime() / 1_000_000_000.0;
                double threadPoolTimeSec = resultFoxThreadPool.getCalculationTime() / 1_000_000_000.0;

                double speedupForkJoin = simpleTimeSec / forkJoinTimeSec;
                double speedupThreadPool = simpleTimeSec / threadPoolTimeSec;

                String correctForkJoin = resultFoxForkJoin.getResultMatrix().equals(resultSimple.getResultMatrix()) ? "+" : "-";
                String correctThreadPool = resultFoxThreadPool.getResultMatrix().equals(resultSimple.getResultMatrix()) ? "+" : "-";

                System.out.printf(
                        "%-10d%-10d%-15.3f%-18.3f%-8s%-10.2f%-18.3f%-8s%-10.2f\n",
                        size, numThreads,
                        simpleTimeSec,
                        forkJoinTimeSec, correctForkJoin, speedupForkJoin,
                        threadPoolTimeSec, correctThreadPool, speedupThreadPool
                );
            }

            System.out.println("-".repeat(TABLE_WIDTH));
            System.out.println("\n");
        }
    }
}
