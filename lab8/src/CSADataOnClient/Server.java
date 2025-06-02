package CSADataOnClient;

import java.io.*;
import java.net.*;

public class Server {
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        // Створюємо серверний сокет на вказаному порту
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Сервер запущено на порту " + SERVER_PORT);

            while (true) {
                // Приймаємо нове з'єднання від клієнта
                Socket clientConnection = serverSocket.accept();
                System.out.println("Клієнт підключився: " + clientConnection);

                // Створюємо окремий потік для обробки кожного клієнта
                new ClientHandler(clientConnection).start();
            }
        } catch (IOException e) {
            System.err.println("Не вдалося запустити сервер на порту: " + SERVER_PORT);
            System.exit(-1);
        }
    }
}


