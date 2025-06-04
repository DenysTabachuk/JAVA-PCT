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
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-5s | %-9s | %-10s | %-14s | %-10s | %-12s | %-15s | %-10s | %-12s | %-15s | %-10s | %-8s |\n",
                    "Size", "Proc", "Seq Time(ms)", "Block Time(ms)", "Block Speedup", "Block Eff",
                    "NonBlk Time(ms)", "NonBlk Speedup", "NonBlk Eff",
                    "Coll Time", "Coll Speedup", "Coll Eff", "Correct?");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
        }

        for (int size : matrixSizes) {
            int[][] matrixA = new int[size][size];
            int[][] matrixB = new int[size][size];

            if (taskId == MASTER) {
                matrixA = MatrixUtils.generateMatrix(size, size);
                matrixB = MatrixUtils.generateMatrix(size, size);
            }

            // Blocking multiply
            long blockingTotalTime = 0;
            int[][] resultMatrixBlocking = null;
            for (int i = 0; i < numRuns; i++) {
                long startTimeBlocking = System.currentTimeMillis();
                resultMatrixBlocking = new MatrixMultiplierBlocking().multiply(matrixA, matrixB, taskId, totalTasks, MASTER);
                MPI.COMM_WORLD.Barrier();
                long endTimeBlocking = System.currentTimeMillis();
                blockingTotalTime += (endTimeBlocking - startTimeBlocking);
            }
            long blockingTime = blockingTotalTime / numRuns;

            // Non-blocking multiply
            long nonBlockingTotalTime = 0;
            int[][] resultMatrixNonBlocking = null;
            for (int i = 0; i < numRuns; i++) {
                long startTimeNonBlocking = System.currentTimeMillis();
                resultMatrixNonBlocking = new MatrixMultiplierNonBlocking().multiply(matrixA, matrixB, taskId, totalTasks, MASTER);
                MPI.COMM_WORLD.Barrier();
                long endTimeNonBlocking = System.currentTimeMillis();
                nonBlockingTotalTime += (endTimeNonBlocking - startTimeNonBlocking);
            }
            long nonBlockingTime = nonBlockingTotalTime / numRuns;

            // Collective multiply
            long collectiveTotalTime = 0;
            int[][] resultMatrixCollective = null;
            for (int i = 0; i < numRuns; i++) {
                long startTimeCollective = System.currentTimeMillis();
                resultMatrixCollective = new MatrixMultiplierCollective(false).multiply(matrixA, matrixB, taskId, totalTasks, MASTER);
                MPI.COMM_WORLD.Barrier();
                long endTimeCollective = System.currentTimeMillis();
                collectiveTotalTime += (endTimeCollective - startTimeCollective);
            }
            long collectiveTime = collectiveTotalTime / numRuns;

            if (taskId == MASTER) {
                // Sequential multiply for correctness and baseline
                long sequentialTotalTime = 0;
                int[][] resultMatrixSeq = null;
                for (int i = 0; i < numRuns; i++) {
                    long startTime = System.currentTimeMillis();
                    resultMatrixSeq = MatrixUtils.multiply(matrixA, matrixB);
                    long endTime = System.currentTimeMillis();
                    sequentialTotalTime += (endTime - startTime);
                }
                long sequentialTime = sequentialTotalTime / numRuns;

                boolean blockingCorrect = MatrixUtils.areEqual(resultMatrixSeq, resultMatrixBlocking);
                boolean nonBlockingCorrect = MatrixUtils.areEqual(resultMatrixSeq, resultMatrixNonBlocking);
                boolean collectiveCorrect = MatrixUtils.areEqual(resultMatrixSeq, resultMatrixCollective);

                String allCorrect = (blockingCorrect && nonBlockingCorrect && collectiveCorrect) ? "+" : "-";

                double speedupBlocking = (double) sequentialTime / blockingTime;
                double efficiencyBlocking = speedupBlocking / logicalCoreCount;

                double speedupNonBlocking = (double) sequentialTime / nonBlockingTime;
                double efficiencyNonBlocking = speedupNonBlocking / logicalCoreCount;

                double speedupCollective = (double) sequentialTime / collectiveTime;
                double efficiencyCollective = speedupCollective / logicalCoreCount;

                System.out.printf("| %-5d | %-5d | %-12d | %-14d | %-14.2f | %-10.2f | %-15d | %-15.2f | %-10.2f | %-12d | %-15.2f | %-10.2f | %-8s |\n",
                        size, totalTasks,
                        sequentialTime,
                        blockingTime, speedupBlocking, efficiencyBlocking,
                        nonBlockingTime, speedupNonBlocking, efficiencyNonBlocking,
                        collectiveTime, speedupCollective, efficiencyCollective,
                        allCorrect);
            }
        }
        MPI.Finalize();
    }
}
