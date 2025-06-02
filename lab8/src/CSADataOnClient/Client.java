package CSADataOnClient;

import Matrix.Matrix;

import java.io.*;
import java.net.*;

public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final int MATRIX_SIZE = 500; // (матриці будуть квадратні MATRIX_SIZE x MATRIX_SIZE)
    private static final int THREAD_COUNT = 12;

    public static void main(String[] args) {
        // Підключення до сервера через сокет
        try (Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream output = new ObjectOutputStream(serverSocket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(serverSocket.getInputStream())) {

            System.out.println("Клієнт запущено. З’єднання з сервером встановлено...");

            // Генеруємо дві випадкові матриці
            System.out.println("Клієнт генерує матриці...");
            long startTime = System.currentTimeMillis();
            Matrix matrixA = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE);
            Matrix matrixB = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE);

            // Надсилаємо матриці та кількість потоків на сервер
            System.out.println("Клієнт надсилає матриці...");
            output.writeObject(matrixA);
            output.writeObject(matrixB);
            output.writeInt(THREAD_COUNT);
            output.flush();

            // Отримуємо результат множення матриць з сервера
            System.out.println("Клієнт отримує результати з сервера...");
            Matrix resultMatrix = (Matrix) input.readObject();
            long endTime = System.currentTimeMillis();

            // Вивід розміру та часу виконання
            System.out.println("Отримано результат: " + resultMatrix.getRows() + "x" + resultMatrix.getCols());
            System.out.println("Час виконання: " + (endTime - startTime) + " мс");
            System.out.println("Результуюча матриця:");
            System.out.println(resultMatrix);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
