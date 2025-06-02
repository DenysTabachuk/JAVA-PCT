package MPI;

import mpi.MPI;
import Matrix.Matrix;
import Matrix.FoxAlgorithm;

import static Matrix.MatrixUtils.*;

public class MatrixMultiplicationMPI {
    private static final int SIZE = 2000;
    private static final int MASTER = 0;  // Ідентифікатор головного процесу
    private static final int THREADS_PER_NODE = 2; // Потоки на один процес

    public static void main(String[] args) throws Exception {
        MPI.Init(args);

        int processId = MPI.COMM_WORLD.Rank(); // Поточний процес
        int totalProcesses = MPI.COMM_WORLD.Size(); // Кількість процесів у групі

        long start = System.currentTimeMillis();

        Matrix A = null;
        Matrix B = null;

        // Ініціалізація даних на головному вузлі
        if (processId == MASTER) {
            A = Matrix.randomMatrix(SIZE, SIZE);
            B = Matrix.randomMatrix(SIZE, SIZE);
        }

        // Підготовка до передачі всім вузлам
        int[] flatA = new int[SIZE * SIZE];
        int[] flatB = new int[SIZE * SIZE];

        if (processId == MASTER) {
            serializeMatrix(A, flatA);
            serializeMatrix(B, flatB);
        }

        // Розсилка даних
        MPI.COMM_WORLD.Bcast(flatA, 0, flatA.length, MPI.INT, MASTER);
        MPI.COMM_WORLD.Bcast(flatB, 0, flatB.length, MPI.INT, MASTER);

        // Відновлення матриць на кожному вузлі
        A = deserializeMatrix(flatA, SIZE, SIZE);
        B = deserializeMatrix(flatB, SIZE, SIZE);

        // Паралельне перемноження з використанням алгоритму Фокса
        Matrix totalResult = FoxAlgorithm.multiply(A, B, THREADS_PER_NODE);

        // Обчислення діапазону рядків для поточного процесу
        int rowsPerProcess = (int) Math.ceil((double) SIZE / totalProcesses);
        int startRow = processId * rowsPerProcess;
        int endRow = Math.min((processId + 1) * rowsPerProcess, SIZE);

        // Витягуємо свою частину результату
        int[][] partialResult = new int[endRow - startRow][SIZE];
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < SIZE; j++) {
                partialResult[i - startRow][j] = totalResult.getValue(i, j);
            }
        }

        Matrix localPart = new Matrix(partialResult);
        int[] flatLocal = new int[localPart.getRows() * localPart.getCols()];
        serializeMatrix(localPart, flatLocal);

        // Підготовка до збору даних
        int[] recvSizes = new int[totalProcesses];
        int[] offsets = new int[totalProcesses];
        for (int i = 0; i < totalProcesses; i++) {
            int rowStart = i * rowsPerProcess;
            int rowEnd = Math.min((i + 1) * rowsPerProcess, SIZE);
            recvSizes[i] = (rowEnd - rowStart) * SIZE;
            offsets[i] = rowStart * SIZE;
        }

        int[] fullFlatResult = null;
        if (processId == MASTER) {
            fullFlatResult = new int[SIZE * SIZE];
        }

        // Збір усіх частин на головному процесі
        MPI.COMM_WORLD.Gatherv(flatLocal, 0, flatLocal.length, MPI.INT,
                fullFlatResult, 0, recvSizes, offsets, MPI.INT, MASTER);

        long finish = System.currentTimeMillis();

        // Виведення фінального результату на головному процесі
        if (processId == MASTER) {
            Matrix resultMatrix = deserializeMatrix(fullFlatResult, SIZE, SIZE);
            System.out.println("Результат перемноження матриць (" + SIZE + "x" + SIZE + ")");
            System.out.println("Час виконання: " + (finish - start) + " мс");
        }

        MPI.Finalize();
    }
}
