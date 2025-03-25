public class MatrixMultiplicationTests {
    public static void main(String[] args){
        final int[] MATRIX_SIZES = { 32, 64, 128, 256, 512}; // , 32, 64, 128, 256, 512, 1024, 2048
        final int[] NUM_THREADS = {1, 2, 4, 8, 16, 32};


        for (int size : MATRIX_SIZES){
            Matrix matrixA = new Matrix(size);
            Matrix matrixB = new Matrix(size);

            matrixA.fillWithRandomNumbers();
            matrixB.fillWithRandomNumbers();

//            matrixA.printMatrix();
//            matrixB.printMatrix();

            for (int numThreads : NUM_THREADS){
                Result resultFox = FoxAlgorithm.multiply(matrixA, matrixB, numThreads);
                Result resultStripped = StrippedAlgorithm.multiply(matrixA, matrixB, numThreads);
                Result resultSimple = SimpleMatrixMultiplication.multiply(matrixA, matrixB);


                System.out.println("Fox. Threads: " + numThreads +  " , time: "  + resultFox.getCalculationTime() / 1_000_000_000.0);
                System.out.println("Stripped. Threads: " + numThreads +  " , time: "  + resultStripped.getCalculationTime() / 1_000_000_000.0);
                System.out.println("Simple. Threads: " + numThreads +  " , time: "  +  resultSimple.getCalculationTime() / 1_000_000_000.0);






            }
        }
    }
}
