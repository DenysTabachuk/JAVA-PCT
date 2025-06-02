package CSADataOnServer;

import Matrix.Matrix;
import Matrix.FoxAlgorithm;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


class ClientHandler extends Thread {
    private final Socket clientSocket;

    private static final int THREAD_COUNT = 12;     // Кількість потоків для паралельного множення
    private static final int MATRIX_SIZE = 500;    // Розмір квадратної матриці

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                // Створюємо потік для відправки об'єктів клієнту
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            // Генерація випадкових матриць A і B на сервері
            System.out.println("Сервер генерує матриці: ");
            Matrix matrixA = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE);
            System.out.println("Матриця А: ");
            System.out.println(matrixA);


            Matrix matrixB = Matrix.randomMatrix(MATRIX_SIZE, MATRIX_SIZE);
            System.out.println("Матриця B: ");
            System.out.println(matrixB);


            // Множення матриць за допомогою паралельного алгоритму Фокса
            System.out.println("Множення матриць за допомогою паралельного алгоритму Фокса...");
            Matrix resultMatrix = FoxAlgorithm.multiply(matrixA, matrixB, THREAD_COUNT);

            // Відправка результату клієнту
            System.out.println("Відправка результатів клієнту...");
            output.writeObject(matrixA);
            output.writeObject(matrixB);
            output.writeObject(resultMatrix);
            output.flush();

        } catch (IOException e) {
            System.err.println("Помилка під час обробки клієнта: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); // Закриваємо з'єднання після обробки клієнта
            } catch (IOException e) {
                System.err.println("Помилка при закритті сокета: " + e.getMessage());
            }
        }
    }
}
