import mpi.MPI;

public class MatrixMultiplierCollective {

    private boolean verbose;

    // Конструктор за замовчуванням (verbose=false)
    public MatrixMultiplierCollective() {
        this.verbose = false;
    }

    // Конструктор з параметром verbose
    public MatrixMultiplierCollective(boolean verbose) {
        this.verbose = verbose;
    }

    // Сеттер verbose
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB, int taskId, int totalTasks, int master) {
        if (taskId == master && verbose) {
            System.out.println("Matrix A:");
            MatrixUtils.printMatrix(matrixA);
            System.out.println("Matrix B:");
            MatrixUtils.printMatrix(matrixB);
        }

        int ROWS_A = 0, COLS_A = 0, COLS_B = 0;

        if (taskId == master) {
            ROWS_A = matrixA.length;
            COLS_A = matrixA[0].length;
            COLS_B = matrixB[0].length;
            if (verbose) {
                System.out.printf("[Task %d] Matrix A is %dx%d, Matrix B is %dx%d\n", taskId, ROWS_A, COLS_A, COLS_A, COLS_B);
            }
        }

        int[] dims = new int[3];
        if (taskId == master) {
            dims[0] = ROWS_A;
            dims[1] = COLS_A;
            dims[2] = COLS_B;
        }
        MPI.COMM_WORLD.Bcast(dims, 0, 3, MPI.INT, master);

        if (taskId != master) {
            ROWS_A = dims[0];
            COLS_A = dims[1];
            COLS_B = dims[2];
        }

        int[] flatB = new int[COLS_A * COLS_B];
        if (taskId == master) {
            for (int i = 0; i < COLS_A; i++) {
                for (int j = 0; j < COLS_B; j++) {
                    flatB[i * COLS_B + j] = matrixB[i][j];
                }
            }
        }
        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.INT, master);

        int rowsPerProc = ROWS_A / totalTasks;
        int remainder = ROWS_A % totalTasks;

        int[] sendCounts = new int[totalTasks];
        int[] displs = new int[totalTasks];
        int offset = 0;
        for (int i = 0; i < totalTasks; i++) {
            sendCounts[i] = (rowsPerProc + (i < remainder ? 1 : 0)) * COLS_A;
            displs[i] = offset;
            offset += sendCounts[i];
        }

        int[] flatA = null;
        if (taskId == master) {
            flatA = new int[ROWS_A * COLS_A];
            for (int i = 0; i < ROWS_A; i++) {
                for (int j = 0; j < COLS_A; j++) {
                    flatA[i * COLS_A + j] = matrixA[i][j];
                }
            }
        }

        int recvCount = sendCounts[taskId];
        int rowsLocal = recvCount / COLS_A;
        int[] localA = new int[recvCount];
        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, displs, MPI.INT, localA, 0, recvCount, MPI.INT, master);
        if (verbose) {
            System.out.printf("[Task %d] Received %d rows of A\n", taskId, rowsLocal);
        }

        int[][] B = new int[COLS_A][COLS_B];
        for (int i = 0; i < COLS_A; i++) {
            for (int j = 0; j < COLS_B; j++) {
                B[i][j] = flatB[i * COLS_B + j];
            }
        }

        int[][] localA2D = new int[rowsLocal][COLS_A];
        for (int i = 0; i < rowsLocal; i++) {
            for (int j = 0; j < COLS_A; j++) {
                localA2D[i][j] = localA[i * COLS_A + j];
            }
        }

        int[][] localResult = new int[rowsLocal][COLS_B];
        for (int i = 0; i < rowsLocal; i++) {
            for (int j = 0; j < COLS_B; j++) {
                for (int k = 0; k < COLS_A; k++) {
                    localResult[i][j] += localA2D[i][k] * B[k][j];
                }
            }
        }

        if (verbose) {
            System.out.printf("[Task %d] Finished local multiplication\n", taskId);
        }

        int[] flatLocalResult = new int[rowsLocal * COLS_B];
        for (int i = 0; i < rowsLocal; i++) {
            for (int j = 0; j < COLS_B; j++) {
                flatLocalResult[i * COLS_B + j] = localResult[i][j];
            }
        }

        int[] recvResult = null;
        int[] recvCounts = new int[totalTasks];
        int[] resultDispls = new int[totalTasks];

        offset = 0;
        for (int i = 0; i < totalTasks; i++) {
            int localRows = sendCounts[i] / COLS_A;
            recvCounts[i] = localRows * COLS_B;
            resultDispls[i] = offset;
            offset += recvCounts[i];
        }

        if (taskId == master) {
            recvResult = new int[ROWS_A * COLS_B];
        }

        MPI.COMM_WORLD.Gatherv(flatLocalResult, 0, flatLocalResult.length, MPI.INT,
                recvResult, 0, recvCounts, resultDispls, MPI.INT, master);

        if (taskId == master) {
            int[][] result = new int[ROWS_A][COLS_B];
            for (int i = 0; i < ROWS_A; i++) {
                for (int j = 0; j < COLS_B; j++) {
                    result[i][j] = recvResult[i * COLS_B + j];
                }
            }
            if (verbose) {
                System.out.println("[Master] Final result assembled.");
                MatrixUtils.printMatrix(result);
            }
            return result;
        } else {
            return null;
        }
    }
}
