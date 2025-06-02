package DataOnServer;

import matrix.Matrix;
import matrix.FoxAlgorithm;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Server is running on port " + PORT_NUMBER);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT_NUMBER);
            System.exit(-1);
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private static final int THREAD_NUM = 12;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            Matrix matrixA = Matrix.randomMatrix(1000, 1000, 10);
            Matrix matrixB = Matrix.randomMatrix(1000, 1000, 10);

            Matrix resultMatrix = FoxAlgorithm.multiply(matrixA, matrixB, THREAD_NUM);

            out.writeObject(resultMatrix);
            out.writeObject(matrixA);
            out.writeObject(matrixB);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
