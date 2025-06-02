package Matrix;

import java.util.Arrays;
import java.util.Random;
import java.io.Serializable;

public class Matrix implements Serializable {
    private final int[][] data;
    private final int rows, cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows][cols];
    }

    // Конструктор для квадратної матриці з розміром size x size.
    public Matrix(int size) {
        this(size, size);
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
            throw new IndexOutOfBoundsException("Індекс виходить за межі матриці.");
        }
        return data[row][col];
    }

    public static Matrix randomMatrix(int rows, int cols) {
        Matrix matrix = new Matrix(rows, cols);
        Random rand = new Random();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.data[i][j] = rand.nextInt(100);
            }
        }

        return matrix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int previewSize = 8;  // Скільки рядків/стовпців показувати на початку і в кінці

        if (rows <= 16 && cols <= 16) {
            // Для маленьких матриць показуємо всі елементи
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    sb.append(String.format("%4d ", data[i][j]));
                }
                sb.append("\n");
            }
        } else {
            // Для великих матриць показуємо частковий перегляд

            // Показуємо перші previewSize рядків
            for (int i = 0; i < previewSize && i < rows; i++) {
                // Показуємо перші previewSize стовпців
                for (int j = 0; j < previewSize && j < cols; j++) {
                    sb.append(String.format("%4d ", data[i][j]));
                }
                if (cols > previewSize) {
                    sb.append(" ... ");
                    // Показуємо останні previewSize стовпців
                    for (int j = cols - previewSize; j < cols; j++) {
                        sb.append(String.format("%4d ", data[i][j]));
                    }
                }
                sb.append("\n");
            }

            if (rows > previewSize) {
                sb.append("...\n");

                // Показуємо останні previewSize рядків
                for (int i = rows - previewSize; i < rows; i++) {
                    // Показуємо перші previewSize стовпців
                    for (int j = 0; j < previewSize && j < cols; j++) {
                        sb.append(String.format("%4d ", data[i][j]));
                    }
                    if (cols > previewSize) {
                        sb.append(" ... ");
                        // Показуємо останні previewSize стовпців
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
