package Matrix;

public class MatrixUtils {
    // Перетворення матриці в одновимірний масив
    public static void serializeMatrix(Matrix matrix, int[] target) {
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                target[i * matrix.getCols() + j] = matrix.getValue(i, j);
            }
        }
    }

    // Відновлення матриці з одновимірного представлення
    public static Matrix deserializeMatrix(int[] flat, int rows, int cols) {
        int[][] restored = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                restored[i][j] = flat[i * cols + j];
            }
        }
        return new Matrix(restored);
    }
}
