package DataOnServer;

import matrix.Matrix;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT_NUMBER);
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Server data app running...");

            long startTime = System.currentTimeMillis();
            
            // Receive all matrices
            Matrix matrixA = (Matrix) in.readObject();
            Matrix matrixB = (Matrix) in.readObject();
            Matrix result = (Matrix) in.readObject();
            
            long endTime = System.currentTimeMillis();

            System.out.println("\nMatrix A:");
            System.out.println(matrixA);
            
            System.out.println("\nMatrix B:");
            System.out.println(matrixB);
            
            System.out.println("\nResult Matrix:");
            System.out.println(result);
            
            System.out.println("\nMatrix sizes:");
            System.out.println("Matrix A: " + matrixA.getRows() + "x" + matrixA.getCols());
            System.out.println("Matrix B: " + matrixB.getRows() + "x" + matrixB.getCols());
            System.out.println("Result: " + result.getRows() + "x" + result.getCols());
            System.out.println("Execution time: " + (endTime - startTime) + " ms");

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
