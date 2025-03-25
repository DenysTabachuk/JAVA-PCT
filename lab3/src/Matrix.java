public class Matrix {
    private int[][] matrix;
    private int size;

    public Matrix(int size){
        this.matrix = new int[size][size];
        this.size = size;
    }

    public void setMatrix(int[][] matrix){
        this.matrix = matrix;
    }

    public void printMatrix(){
        System.out.println();
        for (int i = 0;  i < size; i++){
            for (int j = 0 ; j < size ; j++ ){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public  void fillWithRandomNumbers(){
        for (int i = 0;  i < size; i++){
            for (int j = 0 ; j < size ; j++ ){
                this.matrix[i][j] = (int) ( Math.random()  * 10 );
            }
        }
    }

    public int getMatrixSize(){
        return size;
    }

    public int[] getColumn(int colIndex){
        if (colIndex < 0 || colIndex >= matrix[0].length) {
            throw new IllegalArgumentException("Невірний індекс стовпця");
        }

        int[] column = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            column[i] = matrix[i][colIndex];
        }

        return column;
    }

    public int[] getRow(int rowIndex){
        if (rowIndex < 0 || rowIndex >= matrix.length) {
            throw new IllegalArgumentException("Невірний індекс рядка");
        }

        return matrix[rowIndex];
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    // Встановлення елемента матриці за індексами рядка та стовпця
    public void setElement(int row, int col, int value) {
        matrix[row][col] = value;
    }

}
