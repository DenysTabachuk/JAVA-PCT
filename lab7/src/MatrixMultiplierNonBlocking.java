import mpi.*;
import static mpi.MPI.*;

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
        int ROWS_A = matrixA.length;
        int COLS_A = matrixA[0].length;
        int COLS_B = matrixB[0].length;
        int workerCount = totalTasks - 1;
        int[][] resultMatrix = new int[ROWS_A][COLS_B];

        if (taskId == MASTER) {
            if (verbose) {
                System.out.println("Master started (non-blocking).");
            }
            int avgRowsPerWorker = ROWS_A / workerCount;
            int extraRows = ROWS_A % workerCount;
            int currentOffset = 0;

            Request[] sendRequests = new Request[workerCount * 4];
            int requestIndex = 0;

            // Неблокуюче розподілення задач серед робітників
            for (int dest = 1; dest <= workerCount; dest++) {
                int rowsToSend = (dest <= extraRows) ? avgRowsPerWorker + 1 : avgRowsPerWorker;
                sendRequests[requestIndex++] = COMM_WORLD.Isend(new int[]{currentOffset}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);
                sendRequests[requestIndex++] = COMM_WORLD.Isend(new int[]{rowsToSend}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);
                sendRequests[requestIndex++] = COMM_WORLD.Isend(flattenMatrix(matrixA, currentOffset, rowsToSend, COLS_A), 0, rowsToSend * COLS_A, MPI.INT, dest, TAG_FROM_MASTER);
                sendRequests[requestIndex++] = COMM_WORLD.Isend(flattenMatrix(matrixB, 0, COLS_A, COLS_B), 0, COLS_A * COLS_B, MPI.INT, dest, TAG_FROM_MASTER);
                currentOffset += rowsToSend;
            }
            // Чекати завершення всіх надсилань
            Request.Waitall(sendRequests);

            // Неблокуюче отримання результатів від робітників
            Request[] recvRequests = new Request[workerCount * 3]; // offset, rows, partialResults
            int[][] offsets = new int[workerCount][1];
            int[][] rows = new int[workerCount][1];
            int[][] partialResults = new int[workerCount][]; // Часткові результати

            for (int source = 1; source <= workerCount; source++) {
                int idx = (source - 1) * 3;
                partialResults[source - 1] = new int[(avgRowsPerWorker + 1) * COLS_B]; // Максимальний розмір
                recvRequests[idx] = COMM_WORLD.Irecv(offsets[source - 1], 0, 1, MPI.INT, source, TAG_FROM_WORKER);
                recvRequests[idx + 1] = COMM_WORLD.Irecv(rows[source - 1], 0, 1, MPI.INT, source, TAG_FROM_WORKER);
                recvRequests[idx + 2] = COMM_WORLD.Irecv(partialResults[source - 1], 0, (avgRowsPerWorker + 1) * COLS_B, MPI.INT, source, TAG_FROM_WORKER);
            }
            // Чекати завершення всіх отримань
            Request.Waitall(recvRequests);

            // Побудова кінцевої матриці результатів
            for (int source = 1; source <= workerCount; source++) {
                int offset = offsets[source - 1][0];
                int rowsToReceive = rows[source - 1][0];
                expandMatrix(resultMatrix, partialResults[source - 1], offset, rowsToReceive, COLS_B);
            }

            if (verbose) {
                System.out.println("Master finished assembling result.");
            }

        } else {
            // Неблокуюче отримання даних від майстра
            int[] offsetBuf = new int[1];
            int[] rowsBuf = new int[1];
            Request[] recvRequests = new Request[4]; // offset, rows, aFlat, bFlat

            recvRequests[0] = COMM_WORLD.Irecv(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);
            recvRequests[1] = COMM_WORLD.Irecv(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);

            // Очікування завершення перших двох отримань (offset і rows)
            Request.Waitall(new Request[]{recvRequests[0], recvRequests[1]});

            int rows = rowsBuf[0];
            int[] aFlat = new int[rows * COLS_A];
            int[] bFlat = new int[COLS_A * COLS_B];

            recvRequests[2] = COMM_WORLD.Irecv(aFlat, 0, rows * COLS_A, MPI.INT, MASTER, TAG_FROM_MASTER);
            recvRequests[3] = COMM_WORLD.Irecv(bFlat, 0, COLS_A * COLS_B, MPI.INT, MASTER, TAG_FROM_MASTER);

            // Очікування завершення отримань матриці
            Request.Waitall(new Request[]{recvRequests[2], recvRequests[3]});

            int[] cFlat = new int[rows * COLS_B];

            // Обчислення
            for (int k = 0; k < COLS_B; k++) {
                for (int i = 0; i < rows; i++) {
                    int sum = 0;
                    for (int j = 0; j < COLS_A; j++) {
                        sum += aFlat[i * COLS_A + j] * bFlat[j * COLS_B + k];
                    }
                    cFlat[i * COLS_B + k] = sum;
                }
            }

            if (verbose) {
                StringBuilder sb = new StringBuilder();
                sb.append("Worker ").append(taskId).append(" calculated partial result:\n");
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < COLS_B; j++) {
                        sb.append(cFlat[i * COLS_B + j]).append(" ");
                    }
                    sb.append("\n");
                }
                synchronized (System.out) {
                    System.out.print(sb.toString());
                }
            }

            // Неблокуюче відправлення результату
            Request[] sendRequests = new Request[3]; // offset, rows, cFlat
            sendRequests[0] = COMM_WORLD.Isend(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            sendRequests[1] = COMM_WORLD.Isend(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            sendRequests[2] = COMM_WORLD.Isend(cFlat, 0, rows * COLS_B, MPI.INT, MASTER, TAG_FROM_WORKER);
            Request.Waitall(sendRequests);
        }

        return resultMatrix;
    }

    private static int[] flattenMatrix(int[][] matrix, int rowOffset, int rows, int cols) {
        int[] flat = new int[rows * cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix[rowOffset + i], 0, flat, i * cols, cols);
        }
        return flat;
    }

    private static void expandMatrix(int[][] resultMatrix, int[] cPart, int offset, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            System.arraycopy(cPart, i * cols, resultMatrix[offset + i], 0, cols);
        }
    }
}
