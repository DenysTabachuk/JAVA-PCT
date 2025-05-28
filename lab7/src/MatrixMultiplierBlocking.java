import mpi.*;
import static mpi.MPI.COMM_WORLD;

public class MatrixMultiplierBlocking {
    private boolean verbose;

    public static final int TAG_FROM_MASTER = 1;
    public static final int TAG_FROM_WORKER = 2;

    // Конструктор за замовчуванням - verbose вимкнено
    public MatrixMultiplierBlocking() {
        this.verbose = false;
    }

    // Конструктор з параметром verbose
    public MatrixMultiplierBlocking(boolean verbose) {
        this.verbose = verbose;
    }

    // Сеттер для зміни verbose "на льоту"
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
                System.out.println("Master started.");
            }
            int avgRowsPerWorker = ROWS_A / workerCount;
            int extraRows = ROWS_A % workerCount;
            int currentOffset = 0;
            for (int dest = 1; dest <= workerCount; dest++) {
                int rowsToSend = (dest <= extraRows) ? avgRowsPerWorker + 1 : avgRowsPerWorker;
                COMM_WORLD.Send(new int[]{currentOffset}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);
                COMM_WORLD.Send(new int[]{rowsToSend}, 0, 1, MPI.INT, dest, TAG_FROM_MASTER);
                COMM_WORLD.Send(flattenMatrix(matrixA, currentOffset, rowsToSend, COLS_A), 0, rowsToSend * COLS_A, MPI.INT, dest, TAG_FROM_MASTER);
                COMM_WORLD.Send(flattenMatrix(matrixB, 0, COLS_A, COLS_B), 0, COLS_A * COLS_B, MPI.INT, dest, TAG_FROM_MASTER);
                currentOffset += rowsToSend;
            }
            for (int source = 1; source <= workerCount; source++) {
                int[] offsetBuf = new int[1];
                int[] rowsBuf = new int[1];
                COMM_WORLD.Recv(offsetBuf, 0, 1, MPI.INT, source, TAG_FROM_WORKER);
                COMM_WORLD.Recv(rowsBuf, 0, 1, MPI.INT, source, TAG_FROM_WORKER);
                int[] cPart = new int[rowsBuf[0] * COLS_B];
                COMM_WORLD.Recv(cPart, 0, rowsBuf[0] * COLS_B, MPI.INT, source, TAG_FROM_WORKER);

                if (verbose) {
                    synchronized (System.out) {
                        System.out.println("Master received partial result from worker " + source + ": ");
                        for (int i = 0; i < rowsBuf[0]; i++) {
                            for (int j = 0; j < COLS_B; j++) {
                                System.out.print(cPart[i * COLS_B + j] + " ");
                            }
                            System.out.println();
                        }
                    }
                }
                expandMatrix(resultMatrix, cPart, offsetBuf[0], rowsBuf[0], COLS_B);
            }
        } else {
            int[] offsetBuf = new int[1];
            int[] rowsBuf = new int[1];
            COMM_WORLD.Recv(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);
            COMM_WORLD.Recv(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);
            int rows = rowsBuf[0];
            int[] aFlat = new int[rows * COLS_A];
            int[] bFlat = new int[COLS_A * COLS_B];
            int[] cFlat = new int[rows * COLS_B];
            COMM_WORLD.Recv(aFlat, 0, rows * COLS_A, MPI.INT, MASTER, TAG_FROM_MASTER);
            COMM_WORLD.Recv(bFlat, 0, COLS_A * COLS_B, MPI.INT, MASTER, TAG_FROM_MASTER);
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
            COMM_WORLD.Send(offsetBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            COMM_WORLD.Send(rowsBuf, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            COMM_WORLD.Send(cFlat, 0, rows * COLS_B, MPI.INT, MASTER, TAG_FROM_WORKER);
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
