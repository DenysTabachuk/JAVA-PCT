import mpi.MPI;
import mpi.Request;

import static mpi.MPI.COMM_WORLD;

public class MatrixMultiplierNonBlocking {

    public static final int TAG_FROM_MASTER = 1;
    public static final int TAG_FROM_WORKER = 2;

    private boolean verbose;

    public MatrixMultiplierNonBlocking() {
        this.verbose = false;
    }

    public MatrixMultiplierNonBlocking(boolean verbose) {
        this.verbose = verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB, int taskId, int totalTasks, int MASTER) throws Exception {
        int numRowsA = matrixA.length;
        int numColsA = matrixA[0].length;
        int numColsB = matrixB[0].length;
        int workerCount = totalTasks - 1;
        int[][] resultMatrix = new int[numRowsA][numColsB];

        int[] numRowsToSend = {0};
        int[] rowOffset = {0};

        if (taskId == MASTER) {
            if (verbose) System.out.println("Master started (non-blocking).");

            int avgRowsPerWorker = numRowsA / workerCount;
            int extraRows = numRowsA % workerCount;

            // Розсилаємо частини матриці A та всю матрицю B
            for (int dest = 1; dest <= workerCount; dest++) {
                numRowsToSend[0] = (dest <= extraRows) ? avgRowsPerWorker + 1 : avgRowsPerWorker;

                if (verbose) {
                    System.out.println("Sending " + numRowsToSend[0] + " rows to task " + dest +
                            ", offset = " + rowOffset[0]);
                }

                // Відправляємо дані у неблокуючому режимі
                COMM_WORLD.Isend(matrixB, 0, numColsA, MPI.OBJECT, dest, TAG_FROM_MASTER); // вся матриця B
                COMM_WORLD.Isend(rowOffset, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);         // offset
                COMM_WORLD.Isend(numRowsToSend, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);     // rows
                COMM_WORLD.Isend(matrixA, rowOffset[0], numRowsToSend[0], MPI.OBJECT, dest, TAG_FROM_MASTER); // частина A

                rowOffset[0] += numRowsToSend[0];
            }

            // Приймаємо результати від робітників
            rowOffset[0] = 0; // скидаємо offset для прийому
            for (int source = 1; source <= workerCount; source++) {
                COMM_WORLD.Irecv(rowOffset, 0, 1, MPI.INT, source, TAG_FROM_WORKER).Wait();
                COMM_WORLD.Irecv(numRowsToSend, 0, 1, MPI.INT, source, TAG_FROM_WORKER).Wait();
                COMM_WORLD.Irecv(resultMatrix, rowOffset[0], numRowsToSend[0], MPI.OBJECT, source, TAG_FROM_WORKER).Wait();

                if (verbose) {
                    System.out.println("Master received partial result from worker " + source);
                }

                rowOffset[0] += numRowsToSend[0];
            }

        } else {
            int[][] fullB = new int[numColsA][numColsB];
            int[] offsetBuf = {0};
            int[] rowsBuf = {0};

            // Отримання повної B
            Request recvBRequest = COMM_WORLD.Irecv(fullB, 0, numColsA, MPI.OBJECT, MASTER, TAG_FROM_MASTER);

            // Отримання offset і кількості рядків
            COMM_WORLD.Irecv(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER).Wait();
            COMM_WORLD.Irecv(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER).Wait();

            int numRowsReceived = rowsBuf[0];
            int[][] receivedA = new int[numRowsReceived][numColsA];

            // Отримання частини A
            Request recvARequest = COMM_WORLD.Irecv(receivedA, 0, numRowsReceived, MPI.OBJECT, MASTER, TAG_FROM_MASTER);


            // Очікуємо завершення неблокуючого отримання матриці B та підматриці A,
            // перш ніж переходити до обчислень множення
            recvBRequest.Wait();
            recvARequest.Wait();

            // Обчислення часткової результуючої матриці
            int[][] partialResult = new int[numRowsReceived][numColsB];

            for (int i = 0; i < numRowsReceived; i++) {
                for (int j = 0; j < numColsB; j++) {
                    int sum = 0;
                    for (int k = 0; k < numColsA; k++) {
                        sum += receivedA[i][k] * fullB[k][j];
                    }
                    partialResult[i][j] = sum;
                }
            }

            // Вивід часткового результату
            if (verbose) {
                StringBuilder sb = new StringBuilder();
                sb.append("Worker ").append(taskId).append(" calculated partial result:\n");
                for (int[] row : partialResult) {
                    for (int val : row) {
                        sb.append(val).append(" ");
                    }
                    sb.append("\n");
                }
                synchronized (System.out) {
                    System.out.print(sb.toString());
                }
            }

            // Надсилаємо результат назад
            COMM_WORLD.Isend(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            COMM_WORLD.Isend(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            COMM_WORLD.Isend(partialResult, 0, numRowsReceived, MPI.OBJECT, MASTER, TAG_FROM_WORKER);
        }

        return resultMatrix;
    }
}
