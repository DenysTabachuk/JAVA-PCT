public class MatrixMultiplicationTests {
    public static void main(String[] args) {
        final int[] MATRIX_SIZES = {32, 64, 128, 256, 512};
        final int[] NUM_THREADS = {1, 2, 4, 8,  16, 32};

        System.out.println("Matrix Multiplication Performance Tests\n");

        System.out.printf("%-15s%-15s%-25s%-25s%-25s%-25s%-25s%-25s%-15s\n",
                "Matrix Size", "Threads",
                "Simple Time (s)", "Fox Time (s)", "Speedup (Fox/Sim)",
                "Stripped Time (s)", "Speedup (Stripped/Sim)", "Fox Correct", "Stripped Correct");

        for (int size : MATRIX_SIZES) {
            Matrix matrixA = new Matrix(size);
            Matrix matrixB = new Matrix(size);

            matrixA.fillWithRandomNumbers();
            matrixB.fillWithRandomNumbers();

            for (int numThreads : NUM_THREADS) {
                Result resultFox = FoxAlgorithm.multiply(matrixA, matrixB, numThreads);
                Result resultStripped = StrippedAlgorithm.multiply(matrixA, matrixB, numThreads);
                Result resultSimple = SimpleMatrixMultiplication.multiply(matrixA, matrixB);

                double speedupFox = (double) resultSimple.getCalculationTime() / resultFox.getCalculationTime();
                double speedupStripped = (double) resultSimple.getCalculationTime() / resultStripped.getCalculationTime();

                String foxCorrect = resultFox.getResultMatrix().equals(resultSimple.getResultMatrix()) ? "+" : "-";
                String strippedCorrect = resultStripped.getResultMatrix().equals(resultSimple.getResultMatrix()) ? "+" : "-";

                System.out.printf("%-15d%-15d%-25.3f%-25.3f%-25.3f%-25.3f%-25.3f%-25s%-25s\n",
                        size, numThreads,
                        resultSimple.getCalculationTime() / 1_000_000_000.0,
                        resultFox.getCalculationTime() / 1_000_000_000.0,
                        speedupFox,
                        resultStripped.getCalculationTime() / 1_000_000_000.0,
                        speedupStripped,
                        foxCorrect,
                        strippedCorrect);
            }

            System.out.println();
        }
    }
}
