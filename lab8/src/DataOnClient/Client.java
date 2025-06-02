package DataOnClient;

import matrix.Matrix;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT_NUMBER = 12345;
    private static final int MATRIX_SIZE = 500;
    private static final int THREAD_NUM = 6;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT_NUMBER);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Client data app running...");

            long startTime = System.currentTimeMillis();
            Matrix matrixA = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE, 10);
            Matrix matrixB = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE, 10);

            out.writeObject(matrixA);
            out.writeObject(matrixB);
            out.writeInt(THREAD_NUM);
            out.flush();

            Matrix result = (Matrix) in.readObject();
            long endTime = System.currentTimeMillis();

            System.out.println("Got result matrix with size: " + result.getRows() + "x" + result.getCols());
            System.out.println("Execution time: " + (endTime - startTime) + " ms");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
