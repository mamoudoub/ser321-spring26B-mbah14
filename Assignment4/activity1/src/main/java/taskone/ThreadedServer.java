package taskone;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Multi-threaded Task Management Server.
 * Handles each client in a separate thread.
 */
public class ThreadedServer {

    private static final int DEFAULT_PORT = 8888;
    private static TaskList taskList = new TaskList();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + DEFAULT_PORT);
            }
        }

        System.out.println("Task Management Server starting on port " + port);
        System.out.println("Mode: MULTI-THREADED (one thread per client)");
        System.out.println("Waiting for clients...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                Thread clientThread = new Thread(() -> {
                    try {
                        Performer performer = new Performer(clientSocket, taskList);
                        performer.doPerform();
                    } catch (Exception e) {
                        System.err.println("Client thread error: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            if (!clientSocket.isClosed()) {
                                clientSocket.close();
                            }
                        } catch (IOException e) {
                            System.err.println("Error closing client socket: " + e.getMessage());
                        }
                        System.out.println("Client disconnected");
                    }
                });

                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}