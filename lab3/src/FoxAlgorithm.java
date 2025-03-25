import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class FoxAlgorithm {

    public static Result multiply(Matrix matrixA, Matrix matrixB, int numThreads) {
        long startTime = System.nanoTime();

        int sizeA = matrixA.getMatrixSize();
        int sizeB = matrixB.getMatrixSize();

        if (sizeA != sizeB) {
            throw new IllegalArgumentException("Матриці повинні бути квадратними або мати однаковий розмір.");
        }

        if (matrixA.getMatrixSize() != matrixB.getMatrixSize()) {
            throw new IllegalArgumentException("Розміри матриць повинні співпадати для множення.");
        }



        int n = sizeA;

        if (numThreads > n) {
            System.out.println("Попередження: Кількість потоків перевищує кількість рядків. Змінено на " + n);
            numThreads = n;
        }

        int blockSize = n / numThreads;
        int numBlocks = n / blockSize;

        Matrix[][] subMatrixA = splitMatrix(matrixA, blockSize);
        Matrix[][] subMatrixB = splitMatrix(matrixB, blockSize);
        Matrix[][] subMatrixC = new Matrix[numBlocks][numBlocks];

        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                subMatrixC[i][j] = new Matrix(blockSize);
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (int step = 0; step < numBlocks; step++) {
            for (int i = 0; i < numBlocks; i++) {
                for (int j = 0; j < numBlocks; j++) {
                    int k = (i + step) % numBlocks;
                    Matrix Ablock = subMatrixA[i][k];
                    Matrix Bblock = subMatrixB[k][j];
                    Matrix Cblock = subMatrixC[i][j];

                    futures.add(executor.submit(() -> multiplyBlocks(Ablock, Bblock, Cblock)));
                }
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        Matrix resultMatrix = new Matrix(n);
        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                Matrix block = subMatrixC[i][j];
                for (int x = 0; x < blockSize; x++) {
                    for (int y = 0; y < blockSize; y++) {
                        resultMatrix.getMatrix()[i * blockSize + x][j * blockSize + y] = block.getMatrix()[x][y];
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        return new Result(resultMatrix, duration);
    }

    private static Matrix[][] splitMatrix(Matrix matrix, int blockSize) {
        int size = matrix.getMatrixSize();
        int numBlocks = size / blockSize;
        Matrix[][] subMatrices = new Matrix[numBlocks][numBlocks];

        for (int i = 0; i < numBlocks; i++) {
            for (int j = 0; j < numBlocks; j++) {
                int[][] block = new int[blockSize][blockSize];
                for (int x = 0; x < blockSize; x++) {
                    for (int y = 0; y < blockSize; y++) {
                        block[x][y] = matrix.getMatrix()[i * blockSize + x][j * blockSize + y];
                    }
                }
                subMatrices[i][j] = new Matrix(blockSize);
                subMatrices[i][j].setMatrix(block);
            }
        }
        return subMatrices;
    }

    private static void multiplyBlocks(Matrix A, Matrix B, Matrix C) {
        int size = A.getMatrixSize();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    C.getMatrix()[i][j] += A.getMatrix()[i][k] * B.getMatrix()[k][j];
                }
            }
        }
    }
}
