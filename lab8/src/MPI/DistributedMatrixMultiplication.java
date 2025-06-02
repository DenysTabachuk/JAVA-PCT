package MPI;

import mpi.MPI;
import matrix.Matrix;
import matrix.FoxAlgorithm;

public class DistributedMatrixMultiplication {
    private static final int MATRIX_SIZE = 1000;
    private static final int ROOT = 0; // Головний процес
    private static final int THREAD_NUM_PER_PROCESS = 2; // Кількість потоків на кожен процес

    public static void main(String[] args) throws Exception {
        // Ініціалізація MPI
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank(); // ID поточного процесу
        int size = MPI.COMM_WORLD.Size(); // Загальна кількість процесів

        long startTime = System.currentTimeMillis();

        Matrix matrixA = null;
        Matrix matrixB = null;

        // Головний процес генерує матриці
        if (rank == ROOT) {
            matrixA = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE, 10);
            matrixB = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE, 10);
        }

        // Розсилка матриць A і B усім процесам
        int[] aData = new int[MATRIX_SIZE * MATRIX_SIZE];
        int[] bData = new int[MATRIX_SIZE * MATRIX_SIZE];
        if (rank == ROOT) {
            flattenMatrix(matrixA, aData);
            flattenMatrix(matrixB, bData);
        }
        MPI.COMM_WORLD.Bcast(aData, 0, aData.length, MPI.INT, ROOT);
        MPI.COMM_WORLD.Bcast(bData, 0, bData.length, MPI.INT, ROOT);

        // Відновлення матриць на кожному процесі
        matrixA = unflattenMatrix(aData, MATRIX_SIZE, MATRIX_SIZE);
        matrixB = unflattenMatrix(bData, MATRIX_SIZE, MATRIX_SIZE);

        // Обчислення повної результуючої матриці з використанням ParallelFox
        Matrix fullResult = FoxAlgorithm.multiply(matrixA, matrixB, THREAD_NUM_PER_PROCESS);

        // Визначення частини результату для цього процесу
        int blockSize = (int) Math.ceil((double) MATRIX_SIZE / size); // Розділяємо по рядках
        int startRow = rank * blockSize;
        int endRow = Math.min((rank + 1) * blockSize, MATRIX_SIZE);

        // Вирізаємо потрібну частину результату
        int[][] localResultData = new int[endRow - startRow][MATRIX_SIZE];
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                localResultData[i - startRow][j] = fullResult.getValue(i, j);
            }
        }
        Matrix localResult = new Matrix(localResultData);

        // Збір результатів від усіх процесів
        int[] flatLocalResult = new int[localResult.getRows() * localResult.getCols()];
        flattenMatrix(localResult, flatLocalResult);

        // Визначаємо розміри для збору результатів
        int[] recvCounts = new int[size];
        int[] displacements = new int[size];
        for (int i = 0; i < size; i++) {
            int rowStart = i * blockSize;
            int rowEnd = Math.min((i + 1) * blockSize, MATRIX_SIZE);
            recvCounts[i] = (rowEnd - rowStart) * MATRIX_SIZE;
            displacements[i] = rowStart * MATRIX_SIZE;
        }

        int[] globalResult = null;
        if (rank == ROOT) {
            globalResult = new int[MATRIX_SIZE * MATRIX_SIZE];
        }

        // Збираємо всі частини в одну результуючу матрицю
        MPI.COMM_WORLD.Gatherv(flatLocalResult, 0, flatLocalResult.length, MPI.INT,
                globalResult, 0, recvCounts, displacements, MPI.INT, ROOT);

        long endTime = System.currentTimeMillis();

        // Виведення результатів на головному процесі
        if (rank == ROOT) {
            Matrix result = unflattenMatrix(globalResult, MATRIX_SIZE, MATRIX_SIZE);
            System.out.println("Distributed System with ParallelFox - Result matrix size: " + result.getRows() + "x" + result.getCols());
            System.out.println("Execution time: " + (endTime - startTime) + " ms");
        }

        // Завершення MPI
        MPI.Finalize();
    }

    // Перетворення матриці в одновимірний масив
    private static void flattenMatrix(Matrix matrix, int[] flat) {
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                flat[i * matrix.getCols() + j] = matrix.getValue(i, j);
            }
        }
    }

    // Відновлення матриці з одновимірного масиву
    private static Matrix unflattenMatrix(int[] flat, int rows, int cols) {
        int[][] data = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = flat[i * cols + j];
            }
        }
        return new Matrix(data);
    }
}
