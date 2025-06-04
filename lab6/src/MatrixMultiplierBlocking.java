import mpi.MPI;

public class MatrixMultiplierBlocking {
    static final int TAG_FROM_MASTER = 1; // Тег для повідомлень від майстра до виконавців
    static final int TAG_FROM_WORKER = 2; // Тег для повідомлень від виконавців до майстра
    private boolean verbose;

    public MatrixMultiplierBlocking() {
        this.verbose = false;
    }

    public MatrixMultiplierBlocking(boolean verbose) {
        this.verbose = verbose;
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB, int taskId, int totalTasks, int MASTER) {
        int totalRowsA = (matrixA != null) ? matrixA.length : 0;
        int totalColsA = (matrixA != null) ? matrixA[0].length : matrixB.length; // Кількість стовпців у A = кількість рядків у B
        int totalColsB = matrixB[0].length;
        int numberOfWorkers = totalTasks - 1;

        int[][] resultMatrix = null;

        // Масиви з одним елементом для передачі інформації між процесами (через MPI треба передавати масиви)
        int[] rowOffset = new int[1];      // Початковий індекс рядка, який надсилається або отримується
        int[] numRowsToSend = new int[1];  // Кількість рядків, які будуть надіслані або отримані

        // --- Логіка майстер-процесу ---
        if (taskId == MASTER) {
            resultMatrix = new int[totalRowsA][totalColsB];

            // Визначення, скільки рядків треба віддати кожному worker'у
            int averageRowsPerWorker = totalRowsA / numberOfWorkers;
            int extraRows = totalRowsA % numberOfWorkers; // Щоб розподілити непарні рядки

            rowOffset[0] = 0; // Початковий індекс рядка для першого worker'а

            if (verbose)
                System.out.println("Master started with " + totalTasks + " tasks.");

            // Розсилка частин матриці A і повної матриці B кожному worker'у
            for (int workerId = 1; workerId <= numberOfWorkers; workerId++) {
                // Визначення скільки рядків надіслати worker'у (розподіл із урахуванням "залишку")
                numRowsToSend[0] = (workerId <= extraRows) ? averageRowsPerWorker + 1 : averageRowsPerWorker;

                // Надсилання зсуву (індексу початку частини) worker'у
                MPI.COMM_WORLD.Send(rowOffset, 0, 1, MPI.INT, workerId, TAG_FROM_MASTER);
                // Надсилання кількості рядків, які worker має обробити
                MPI.COMM_WORLD.Send(numRowsToSend, 0, 1, MPI.INT, workerId, TAG_FROM_MASTER);
                // Надсилання відповідної підматриці A (рядки починаючи з rowOffset[0], довжина numRowsToSend[0])
                MPI.COMM_WORLD.Send(matrixA, rowOffset[0], numRowsToSend[0], MPI.OBJECT, workerId, TAG_FROM_MASTER);
                // Надсилання повної матриці B (всі рядки, щоб worker міг виконати множення)
                MPI.COMM_WORLD.Send(matrixB, 0, totalColsA, MPI.OBJECT, workerId, TAG_FROM_MASTER);

                if (verbose) {
                    System.out.println("Sent " + numRowsToSend[0] + " rows to task " + workerId +
                            " (offset " + rowOffset[0] + ")");
                }

                // Оновлення зсуву для наступного worker'а (щоб надсилати наступні рядки без перекриття)
                rowOffset[0] += numRowsToSend[0];
            }

            // Отримання результатів від усіх worker'ів (підрезультати множення)
            for (int workerId = 1; workerId <= numberOfWorkers; workerId++) {
                // Отримання зсуву (індексу рядка) обробленої частини
                MPI.COMM_WORLD.Recv(rowOffset, 0, 1, MPI.INT, workerId, TAG_FROM_WORKER);
                // Отримання кількості оброблених рядків
                MPI.COMM_WORLD.Recv(numRowsToSend, 0, 1, MPI.INT, workerId, TAG_FROM_WORKER);
                // Отримання частини результатної матриці (результату множення для частини рядків)
                MPI.COMM_WORLD.Recv(resultMatrix, rowOffset[0], numRowsToSend[0], MPI.OBJECT, workerId, TAG_FROM_WORKER);

                if (verbose) {
                    System.out.println("Received result from task " + workerId);
                }
            }

        }
        // --- Логіка воркера ---
        else {
            // Отримання зсуву (індексу початку частини рядків матриці A для обробки)
            MPI.COMM_WORLD.Recv(rowOffset, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);
            // Отримання кількості рядків для обробки
            MPI.COMM_WORLD.Recv(numRowsToSend, 0, 1, MPI.INT, MASTER, TAG_FROM_MASTER);

            int[][] subMatrixA = new int[numRowsToSend[0]][totalColsA];
            int[][] fullMatrixB = new int[totalColsA][totalColsB];
            int[][] subResult = new int[numRowsToSend[0]][totalColsB];

            // Отримання частини матриці A
            MPI.COMM_WORLD.Recv(subMatrixA, 0, numRowsToSend[0], MPI.OBJECT, MASTER, TAG_FROM_MASTER);
            // Отримання повної матриці B
            MPI.COMM_WORLD.Recv(fullMatrixB, 0, totalColsA, MPI.OBJECT, MASTER, TAG_FROM_MASTER);

            // Обчислення множення підматриці A на матрицю B
            for (int i = 0; i < numRowsToSend[0]; i++) {
                for (int colB = 0; colB < totalColsB; colB++) {
                    int sum = 0;
                    for (int j = 0; j < totalColsA; j++) {
                        sum += subMatrixA[i][j] * fullMatrixB[j][colB];
                    }
                    subResult[i][colB] = sum;
                }
            }

            if (verbose) {
                System.out.println("Worker " + taskId + " finished computation.");
            }

            // Надсилання назад майстру:
            MPI.COMM_WORLD.Send(rowOffset, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            MPI.COMM_WORLD.Send(numRowsToSend, 0, 1, MPI.INT, MASTER, TAG_FROM_WORKER);
            MPI.COMM_WORLD.Send(subResult, 0, numRowsToSend[0], MPI.OBJECT, MASTER, TAG_FROM_WORKER);
        }

        return resultMatrix;
    }
}
