package CSADataOnClient;

import Matrix.FoxAlgorithm;
import Matrix.Matrix;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientHandler extends Thread {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        // Потік, який обробляє одного клієнта
        try (
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // Отримуємо дві матриці та кількість потоків для паралельної обробки
            System.out.println("Сервер отримує дві матриці та кількість потоків для паралельної обробки...");
            Matrix matrixA = (Matrix) input.readObject();
            System.out.println("Матриця A:");
            System.out.println(matrixA);

            Matrix matrixB = (Matrix) input.readObject();
            System.out.println("Матриця B:");
            System.out.println(matrixB);

            int threadCount = input.readInt();
            System.out.println("Кількість потоків: " + threadCount);

            // Обчислюємо результат за допомогою алгоритму Фокса з вказаною кількістю потоків
            System.out.println("Обчислюємо результат за допомогою алгоритму Фокса...");
            Matrix resultMatrix = FoxAlgorithm.multiply(matrixA, matrixB, threadCount);

            // Відправляємо результат назад клієнту
            output.writeObject(resultMatrix);
            System.out.println("Відправляємо результат назад клієнту...");
            output.flush(); // надсилає всі дані, що накопичились у буфері

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // Закриваємо сокет після завершення обробки клієнта
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
