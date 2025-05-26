public class MatrixUtils {

    public static void printMatrix(int[][] matrix) {
        final int ROWS_A = matrix.length;
        final int COLS_B = matrix[0].length;


        for (int i = 0; i < ROWS_A; i++) {
            for (int j = 0; j < COLS_B; j++) {
                System.out.print(" " + matrix[i][j]);
            }
            System.out.println();
        }
    }

    public static int[][] multiply(int[][] A, int[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;

        int[][] result = new int[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return result;
    }

    public static int[][] generateMatrix(int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    public static boolean areEqual(int[][] result1, int[][] result2) {
        if (result1.length != result2.length || result1[0].length != result2[0].length) {
            return false;
        }
        for (int i = 0; i < result1.length; i++) {
            for (int j = 0; j < result1[0].length; j++) {
                if (result1[i][j] != result2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
