import mpi.MPI;

public class BlockingMatrixMultiplication {
    int[][] multiply(int[][] matrixA, int[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;
        int colsB = matrixB[0].length;

        int taskId = MPI.COMM_WORLD.Rank();     // ID поточного процесу
        int workerCount = totalTasks - 1;       // Кількість робочих процесів

        if (taskId == Main.MASTER) {
            System.out.println("Запуск множення матриць з " + totalTasks + " процесами");


            int avgRowsPerWorker = rowsA / workerCount;
            int extraRows = rowsA % workerCount;
            int currentOffset = 0;

            // Розподіл задач між WORKER-процесами
            for (int dest = 1; dest <= workerCount; dest++) {
                int rowsToSend = (dest <= extraRows) ? avgRowsPerWorker + 1 : avgRowsPerWorker;

                System.out.println("Надсилаємо " + rowsToSend + " рядків до процесу " + dest + ", offset=" + currentOffset);

                // Надсилаємо offset, rows, частину A та повністю B
                MPI.COMM_WORLD.Send(new int[]{currentOffset}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);
                MPI.COMM_WORLD.Send(new int[]{rowsToSend}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);

                MPI.COMM_WORLD.Send(flattenMatrix(matrixA, currentOffset, rowsToSend, colsA), 0, rowsToSend * colsA, MPI.DOUBLE, dest, TAG_FROM_MASTER);
                MPI.COMM_WORLD.Send(flattenMatrix(matrixB, 0, colsA, COLS_B), 0, colsA * COLS_B, MPI.DOUBLE, dest, TAG_FROM_MASTER);

                currentOffset += rowsToSend;
            }

            // Отримання результатів від WORKER-ів
            for (int source = 1; source <= workerCount; source++) {
                int[] offsetBuf = new int[1];
                int[] rowsBuf = new int[1];

                MPI.COMM_WORLD.Recv(offsetBuf, 0, 1, MPI.INT, source, TAG_FROM_WORKER);
                MPI.COMM_WORLD.Recv(rowsBuf, 0, 1, MPI.INT, source, TAG_FROM_WORKER);

                double[] cPart = new double[rowsBuf[0] * COLS_B];
                MPI.COMM_WORLD.Recv(cPart, 0, rowsBuf[0] * COLS_B, MPI.DOUBLE, source, TAG_FROM_WORKER);

                expandMatrix(resultMatrix, cPart, offsetBuf[0], rowsBuf[0], COLS_B);
                System.out.println("Отримано результати від процесу " + source);
            }
        return null;
    }
}
