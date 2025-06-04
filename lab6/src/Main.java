import mpi.*;

public class Main {
    static final int MASTER = 0;
    static int[] matrixSizes = {100, 200, 300, 500};
    static int numRuns = 5; // Кількість запусків для кожного методу
    static int logicalCoreCount = 12;

    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int taskId = MPI.COMM_WORLD.Rank();
        int totalTasks = MPI.COMM_WORLD.Size();

        if (taskId == MASTER) {
            // Вивід заголовку таблиці з результатами (тільки для головного процесу)
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-5s | %-12s | %-14s | %-14s | %-10s | %-15s | %-15s | %-10s | %-8s |\n",
                    "Size", "Proc", "Seq Time(ms)", "Block Time(ms)", "Block Speedup", "Block Eff",
                    "NonBlk Time(ms)", "NonBlk Speedup", "NonBlk Eff", "Correct?");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
        }

        for (int size : matrixSizes) {
            int[][] matrixA = new int[size][size];
            int[][] matrixB = new int[size][size];

            if (taskId == MASTER) {
                // Генерація випадкових матриць (тільки головним процесом)
                matrixA = MatrixUtils.generateMatrix(size, size);
                matrixB = MatrixUtils.generateMatrix(size, size);
            }

            // Блокуюче множення матриць (MPI Blocking)
            long blockingTotalTime = 0;
            int[][] resultMatrixBlocking = null;
            for (int i = 0; i < numRuns; i++) {
                long startTimeBlocking = System.currentTimeMillis();
                resultMatrixBlocking = new MatrixMultiplierBlocking().multiply(matrixA, matrixB, taskId, totalTasks, MASTER);
                MPI.COMM_WORLD.Barrier(); // Синхронізація процесів
                long endTimeBlocking = System.currentTimeMillis();
                blockingTotalTime += (endTimeBlocking - startTimeBlocking);
            }
            long blockingTime = blockingTotalTime / numRuns;

            // Неблокуюче множення матриць (MPI Non-Blocking)
            long nonBlockingTotalTime = 0;
            int[][] resultMatrixNonBlocking = null;
            for (int i = 0; i < numRuns; i++) {
                long startTimeNonBlocking = System.currentTimeMillis();
                resultMatrixNonBlocking = new MatrixMultiplierNonBlocking(false).multiply(matrixA, matrixB, taskId, totalTasks, MASTER);
                MPI.COMM_WORLD.Barrier(); // Синхронізація процесів
                long endTimeNonBlocking = System.currentTimeMillis();
                nonBlockingTotalTime += (endTimeNonBlocking - startTimeNonBlocking);
            }
            long nonBlockingTime = nonBlockingTotalTime / numRuns;

            if (taskId == MASTER) {
                // Послідовне множення для перевірки коректності та як базовий еталон
                long sequentialTotalTime = 0;
                int[][] resultMatrixSeq = null;
                for (int i = 0; i < numRuns; i++) {
                    long startTime = System.currentTimeMillis();
                    resultMatrixSeq = MatrixUtils.multiply(matrixA, matrixB);
                    long endTime = System.currentTimeMillis();
                    sequentialTotalTime += (endTime - startTime);
                }
                long sequentialTime = sequentialTotalTime / numRuns;

                // Перевірка коректності паралельних обчислень
                boolean blockingCorrect = MatrixUtils.areEqual(resultMatrixSeq, resultMatrixBlocking);
                boolean nonBlockingCorrect = MatrixUtils.areEqual(resultMatrixSeq, resultMatrixNonBlocking);
                String allCorrect = (blockingCorrect && nonBlockingCorrect) ? "+" : "-";

                // Обчислення прискорення (speedup) та ефективності (efficiency)
                double speedupBlocking = (double) sequentialTime / blockingTime;
                double efficiencyBlocking = speedupBlocking / logicalCoreCount;

                double speedupNonBlocking = (double) sequentialTime / nonBlockingTime;
                double efficiencyNonBlocking = speedupNonBlocking / logicalCoreCount;

                // Вивід результатів у вигляді таблиці
                System.out.printf("| %-5d | %-5d | %-12d | %-14d | %-14.2f | %-10.2f | %-15d | %-15.2f | %-10.2f | %-8s |\n",
                        size, totalTasks,
                        sequentialTime,
                        blockingTime, speedupBlocking, efficiencyBlocking,
                        nonBlockingTime, speedupNonBlocking, efficiencyNonBlocking,
                        allCorrect);
            }
        }

        // Завершення MPI-середовища
        MPI.Finalize();
    }
}
