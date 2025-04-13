package speedUp;

public class Result {
    private Matrix resultMatrix;
    private long calculationTime;

    public Result(Matrix matrix, long calculationTime) {
        this.resultMatrix = matrix;
        this.calculationTime = calculationTime;
    }

    Matrix getResultMatrix() {
        return resultMatrix;
    }

    long getCalculationTime() {
        return calculationTime;
    }

    public void printResult() {
        for (int i = 0; i < resultMatrix.getMatrixSize(); i++) {
            for (int j = 0; j < resultMatrix.getMatrixSize(); j++) {
                System.out.print(resultMatrix.getMatrix()[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Час обчислення: " + calculationTime / 1_000_000_000.0 + " c");
    }
}
