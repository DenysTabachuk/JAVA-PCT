package speedUp;

public class SimpleMatrixMultiplication {

    public static Result multiply(Matrix matrixA, Matrix matrixB) {
        long startTime = System.nanoTime();

        if (matrixA.getMatrixSize() != matrixB.getMatrixSize()) {
            throw new IllegalArgumentException("Розміри матриць повинні співпадати для множення.");
        }

        int size = matrixA.getMatrixSize();

        Matrix resultMatrix = new Matrix(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += matrixA.getMatrix()[i][k] * matrixB.getMatrix()[k][j];
                }
                resultMatrix.getMatrix()[i][j] = sum;
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        return new Result(resultMatrix, duration);
    }
}
