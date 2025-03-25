import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StrippedAlgorithm {

//    Стрічковий алгоритм для множення матриць зазвичай передбачає, що матриці множаться за допомогою концепції "стрічки".
//    Це означає, що обчислення результату відбувається за допомогою певної послідовності обчислень, де кожен потік може працювати
//    над певною частиною обчислень, розподіленою по "стрічці" (наприклад, по певним ділянкам матриць).

    public static Result multiply(Matrix matrixA, Matrix matrixB, int numThreads) {
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Runnable> tasks = new ArrayList<>();

        int n = matrixA.getMatrixSize();
        Matrix resultMatrix = new Matrix(n);


        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int col = (i + j) % n;
                RowColMultiplyTask task = new RowColMultiplyTask(matrixA.getRow(j), matrixB.getColumn(col), resultMatrix, j, col);
                tasks.add(task);
            }
        }

        for (Runnable task : tasks) {
            executor.submit(task);
        }


        executor.shutdown();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        return new Result(resultMatrix, duration);
    }


    public static class  StripedAlgorithmThread implements Runnable {
        private int[] row;
        private int[] col;
        private Matrix resultMatrix;
        private int rowIndex;
        private int colIndex;

        public RowColMultiplyTask(int[] row, int[] col, Matrix resultMatrix, int rowIndex, int colIndex) {
            this.row = row;
            this.col = col;
            this.resultMatrix = resultMatrix;
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        @Override
        public void run() {
            int result = 0;
            for (int i = 0; i < row.length; i++) {
                result += row[i] * col[i];
            }

            // Синхронізуємо доступ до матриці результату
            synchronized (resultMatrix) {
                resultMatrix.setElement(rowIndex, colIndex, result);
            }
        }
    }
}
