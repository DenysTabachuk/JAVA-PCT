import mpi.MPI;

public class MatrixMultiplierCollective {

    private boolean verbose;

    public MatrixMultiplierCollective() {
        this.verbose = false;
    }

    public MatrixMultiplierCollective(boolean verbose) {
        this.verbose = verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB, int taskId, int totalTasks, int MASTER) {
        // Виводимо вхідні матриці на головному процесі
        if (taskId == MASTER && verbose) {
            System.out.println("Matrix A:");
            MatrixUtils.printMatrix(matrixA);
            System.out.println("Matrix B:");
            MatrixUtils.printMatrix(matrixB);
        }

        int ROWS_A = 0, COLS_A = 0, COLS_B = 0;

        // Головний процес ініціалізує розміри матриць
        if (taskId == MASTER) {
            ROWS_A = matrixA.length;
            COLS_A = matrixA[0].length;
            COLS_B = matrixB[0].length;
            if (verbose) {
                System.out.printf("[Task %d] Matrix A is %dx%d, Matrix B is %dx%d\n", taskId, ROWS_A, COLS_A, COLS_A, COLS_B);
            }
        }

        // Передаємо розміри всім процесам (Bcast)
        int[] dims = new int[3];
        if (taskId == MASTER) {
            dims[0] = ROWS_A;
            dims[1] = COLS_A;
            dims[2] = COLS_B;
        }
        MPI.COMM_WORLD.Bcast(dims, 0, 3, MPI.INT, MASTER);

        if (taskId != MASTER) {
            ROWS_A = dims[0];
            COLS_A = dims[1];
            COLS_B = dims[2];
        }

        // Перетворюємо матрицю B у плоский масив для розсилки
        int[] flatB = new int[COLS_A * COLS_B];
        if (taskId == MASTER) {
            for (int i = 0; i < COLS_A; i++) {
                for (int j = 0; j < COLS_B; j++) {
                    flatB[i * COLS_B + j] = matrixB[i][j];
                }
            }
        }
        // Розсилаємо B усім процесам
        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.INT, MASTER);

        // Розрахунок кількості рядків, які отримає кожен процес
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

        // Плоске представлення матриці A (тільки в master)
        int[] flatA = null;
        if (taskId == MASTER) {
            flatA = new int[ROWS_A * COLS_A];
            for (int i = 0; i < ROWS_A; i++) {
                for (int j = 0; j < COLS_A; j++) {
                    flatA[i * COLS_A + j] = matrixA[i][j];
                }
            }
        }

        // Кожен процес отримує свою частину рядків A
        int recvCount = sendCounts[taskId];
        int rowsLocal = recvCount / COLS_A;
        int[] localA = new int[recvCount];
        MPI.COMM_WORLD.Scatterv(flatA, 0, sendCounts, displs, MPI.INT, localA, 0, recvCount, MPI.INT, MASTER);

        if (verbose) {
            System.out.printf("[Task %d] Received %d rows of A\n", taskId, rowsLocal);
        }

        // Відновлюємо матрицю B з плоского вигляду
        int[][] B = new int[COLS_A][COLS_B];
        for (int i = 0; i < COLS_A; i++) {
            for (int j = 0; j < COLS_B; j++) {
                B[i][j] = flatB[i * COLS_B + j];
            }
        }

        // Відновлюємо підматрицю A з плоского вигляду
        int[][] localA2D = new int[rowsLocal][COLS_A];
        for (int i = 0; i < rowsLocal; i++) {
            for (int j = 0; j < COLS_A; j++) {
                localA2D[i][j] = localA[i * COLS_A + j];
            }
        }

        // Виконуємо множення локальної частини A на B
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

        // Перетворюємо результат у плоский масив для збору
        int[] flatLocalResult = new int[rowsLocal * COLS_B];
        for (int i = 0; i < rowsLocal; i++) {
            for (int j = 0; j < COLS_B; j++) {
                flatLocalResult[i * COLS_B + j] = localResult[i][j];
            }
        }

        // Підготовка до збору всіх результатів на головному процесі
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

        if (taskId == MASTER) {
            recvResult = new int[ROWS_A * COLS_B];
        }

        // Збираємо всі частини результату на master-процесі
        MPI.COMM_WORLD.Gatherv(flatLocalResult, 0, flatLocalResult.length, MPI.INT,
                recvResult, 0, recvCounts, resultDispls, MPI.INT, MASTER);

        // Формуємо фінальну 2D-матрицю результату (тільки в master)
        if (taskId == MASTER) {
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
