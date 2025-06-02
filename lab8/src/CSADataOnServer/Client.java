package CSADataOnServer;

import Matrix.Matrix;

import java.io.*;
import java.net.*;


public class Client {
    private static final String SERVER_HOST = "localhost";  // IP-адреса або домен сервера
    private static final int SERVER_PORT = 8080;            // Порт сервера

    public static void main(String[] args) {
        try (
                // Підключення до сервера через сокет
                Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream())
        ) {
            System.out.println("Клієнт підключився до сервера за адресою " + SERVER_HOST + ":" + SERVER_PORT);
            long startTime = System.currentTimeMillis();

            // Отримання матриць від сервера
            Matrix matrixA = (Matrix) input.readObject();
            Matrix matrixB = (Matrix) input.readObject();
            Matrix resultMatrix = (Matrix) input.readObject();

            long endTime = System.currentTimeMillis();

            // Вивід отриманих матриць
            System.out.println("\nМатриця A:");
            System.out.println(matrixA);

            System.out.println("\nМатриця B:");
            System.out.println(matrixB);

            System.out.println("\nРезультат множення матриць (A × B):");
            System.out.println(resultMatrix);

            // Вивід розмірів матриць і часу виконання
            System.out.println("\nРозміри матриць:");
            System.out.println("Матриця A: " + matrixA.getRows() + "×" + matrixA.getCols());
            System.out.println("Матриця B: " + matrixB.getRows() + "×" + matrixB.getCols());
            System.out.println("Результат: " + resultMatrix.getRows() + "×" + resultMatrix.getCols());
            System.out.println("Час виконання: " + (endTime - startTime) + " мс");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Помилка клієнта: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
