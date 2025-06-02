package DataOnClient;

import matrix.Matrix;
import matrix.FoxAlgorithm;

import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT_NUMBER = 12345;

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

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            Matrix matrixA = (Matrix) in.readObject();
            Matrix matrixB = (Matrix) in.readObject();
            int threadNum = in.readInt();

            Matrix resultMatrix = FoxAlgorithm.multiply(matrixA, matrixB, threadNum);

            out.writeObject(resultMatrix);
            out.flush();
        } catch (IOException | ClassNotFoundException e) {
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
