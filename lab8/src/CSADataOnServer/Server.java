package CSADataOnServer;

import java.io.*;
import java.net.*;

public class Server {
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Сервер запущено на порті " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клієнт підключився: " + clientSocket);
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Не вдалося прослуховувати порт " + SERVER_PORT);
            System.exit(-1);
        }
    }
}
