package matrix;

import java.util.Arrays;
import java.util.Random;
import java.io.Serializable;

public class Matrix implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int[][] data;
    private final int rows, cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows][cols];
    }

    public Matrix(int[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            this.data[i] = Arrays.copyOf(data[i], cols);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getValue(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Index out of bounds for Matrix.");
        }
        return data[row][col];
    }

    public static Matrix randomMatrix(int rows, int cols, int bound) {
        Matrix matrix = new Matrix(rows, cols);
        Random rand = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.data[i][j] = rand.nextInt(bound);
            }
        }

        return matrix;
    }

    public static Matrix randomMatrix(int rows, int cols) {
        return randomMatrix(rows, cols, 100);
    }

    public boolean equals(Matrix other) {
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }
        for (int i = 0; i < rows; i++) {
            if (!Arrays.equals(this.data[i], other.data[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(Matrix matrixA, Matrix matrixB) {
        return matrixA.equals(matrixB);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int previewSize = 8;  // How many rows/columns to show at start and end
        
        if (rows <= 16 && cols <= 16) {
            // For small matrices, show everything
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    sb.append(String.format("%4d ", data[i][j]));
                }
                sb.append("\n");
            }
        } else {
            // For large matrices, show partial view
            // Show first previewSize rows
            for (int i = 0; i < previewSize && i < rows; i++) {
                // Show first previewSize columns
                for (int j = 0; j < previewSize && j < cols; j++) {
                    sb.append(String.format("%4d ", data[i][j]));
                }
                if (cols > previewSize) {
                    sb.append(" ... ");
                    // Show last previewSize columns
                    for (int j = cols - previewSize; j < cols; j++) {
                        sb.append(String.format("%4d ", data[i][j]));
                    }
                }
                sb.append("\n");
            }
            
            if (rows > previewSize) {
                sb.append("...\n");
                
                // Show last previewSize rows
                for (int i = rows - previewSize; i < rows; i++) {
                    // Show first previewSize columns
                    for (int j = 0; j < previewSize && j < cols; j++) {
                        sb.append(String.format("%4d ", data[i][j]));
                    }
                    if (cols > previewSize) {
                        sb.append(" ... ");
                        // Show last previewSize columns
                        for (int j = cols - previewSize; j < cols; j++) {
                            sb.append(String.format("%4d ", data[i][j]));
                        }
                    }
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
