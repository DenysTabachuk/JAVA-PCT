import mpi.MPI;

public class MatrixMultiplierBlocking {
    static final int TAG_FROM_MASTER = 1;
    static final int TAG_FROM_WORKER = 2;
    private boolean verbose;

    public MatrixMultiplierBlocking() {
        this.verbose = false;
    }

    public MatrixMultiplierBlocking(boolean verbose) {
        this.verbose = verbose;
    }

    public int[][] multiply(int[][] matrixA, int[][] matrixB, int currentTaskId, int totalTasks, int masterTaskId) {
        // Matrix dimensions
        int totalRowsA = (matrixA != null) ? matrixA.length : 0;      // Number of rows in A
        int totalColsA = (matrixA != null) ? matrixA[0].length : matrixB.length; // Columns of A == rows of B
        int totalColsB = matrixB[0].length;                           // Number of columns in B
        int numberOfWorkers = totalTasks - 1;

        int[][] resultMatrix = null; // Final result matrix (only created by master)

        // Arrays used for sending offset and number of rows to workers
        int[] rowOffset = new int[1];
        int[] numRowsToSend = new int[1];

        // --- Master process ---
        if (currentTaskId == masterTaskId) {
            resultMatrix = new int[totalRowsA][totalColsB];

            int averageRowsPerWorker = totalRowsA / numberOfWorkers;
            int extraRows = totalRowsA % numberOfWorkers;
            rowOffset[0] = 0; // Starting row offset for sending

            if (verbose)
                System.out.println("Master started with " + totalTasks + " total tasks.");

            // Send portions of Matrix A and full Matrix B to workers
            for (int workerId = 1; workerId <= numberOfWorkers; workerId++) {
                numRowsToSend[0] = (workerId <= extraRows) ? averageRowsPerWorker + 1 : averageRowsPerWorker;

                // Send meta info and data to worker
                MPI.COMM_WORLD.Send(rowOffset, 0, 1, MPI.INT, workerId, TAG_FROM_MASTER);
                MPI.COMM_WORLD.Send(numRowsToSend, 0, 1, MPI.INT, workerId, TAG_FROM_MASTER);
                MPI.COMM_WORLD.Send(matrixA, rowOffset[0], numRowsToSend[0], MPI.OBJECT, workerId, TAG_FROM_MASTER);
                MPI.COMM_WORLD.Send(matrixB, 0, totalColsA, MPI.OBJECT, workerId, TAG_FROM_MASTER);

                if (verbose) {
                    System.out.println("Sent " + numRowsToSend[0] + " rows to task " + workerId +
                            " (offset " + rowOffset[0] + ")");
                }

                rowOffset[0] += numRowsToSend[0]; // Update offset for next worker
            }

            // Receive computed sub-results from all workers
            for (int workerId = 1; workerId <= numberOfWorkers; workerId++) {
                MPI.COMM_WORLD.Recv(rowOffset, 0, 1, MPI.INT, workerId, TAG_FROM_WORKER);
                MPI.COMM_WORLD.Recv(numRowsToSend, 0, 1, MPI.INT, workerId, TAG_FROM_WORKER);
                MPI.COMM_WORLD.Recv(resultMatrix, rowOffset[0], numRowsToSend[0], MPI.OBJECT, workerId, TAG_FROM_WORKER);

                if (verbose) {
                    System.out.println("Received results from task " + workerId);
                }
            }

        }
        // --- Worker process ---
        else {
            // Receive metadata
            MPI.COMM_WORLD.Recv(rowOffset, 0, 1, MPI.INT, masterTaskId, TAG_FROM_MASTER);
            MPI.COMM_WORLD.Recv(numRowsToSend, 0, 1, MPI.INT, masterTaskId, TAG_FROM_MASTER);

            // Allocate memory for submatrices
            int[][] subMatrixA = new int[numRowsToSend[0]][totalColsA];
            int[][] fullMatrixB = new int[totalColsA][totalColsB];
            int[][] subResult = new int[numRowsToSend[0]][totalColsB];

            // Receive actual data
            MPI.COMM_WORLD.Recv(subMatrixA, 0, numRowsToSend[0], MPI.OBJECT, masterTaskId, TAG_FROM_MASTER);
            MPI.COMM_WORLD.Recv(fullMatrixB, 0, totalColsA, MPI.OBJECT, masterTaskId, TAG_FROM_MASTER);

            // Perform matrix multiplication
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
                System.out.println("Worker " + currentTaskId + " completed computation.");
            }

            // Send results back to master
            MPI.COMM_WORLD.Send(rowOffset, 0, 1, MPI.INT, masterTaskId, TAG_FROM_WORKER);
            MPI.COMM_WORLD.Send(numRowsToSend, 0, 1, MPI.INT, masterTaskId, TAG_FROM_WORKER);
            MPI.COMM_WORLD.Send(subResult, 0, numRowsToSend[0], MPI.OBJECT, masterTaskId, TAG_FROM_WORKER);
        }

        return resultMatrix;
    }
}
