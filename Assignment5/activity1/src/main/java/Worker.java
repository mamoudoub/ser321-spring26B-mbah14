import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Worker {

    public static void main(String[] args) throws Exception {

        String name = args[0];
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        Socket socket = new Socket(host, port);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

        Scanner scanner = new Scanner(System.in);

        System.out.println(name + " connecting to leader at " + host + ":" + port);
        System.out.println("Connected successfully!");

        while (true) {
            String msg = in.readLine();

            if (msg == null) break;

            if (msg.startsWith("CONSENSUS")) {
                String[] parts = msg.split(":");
                int result = Integer.parseInt(parts[1]);
                int agree = Integer.parseInt(parts[2]);
                int total = Integer.parseInt(parts[3]);

                System.out.println("Consensus announced: " + result +
                        " (" + agree + "/" + total + " workers agreed)");

                System.out.println("Waiting for next task...");
                continue;
            }

            if (msg.startsWith("NO_CONSENSUS")) {
                System.out.println("No consensus reached.");
                System.out.println("Details: " + msg);
                continue;
            }

            // Task received
            System.out.println("Task received: " + msg);
            System.out.print("> Enter your result: ");

            int result = Integer.parseInt(scanner.nextLine());

            out.println(result);

            System.out.println("Result submitted to leader.");
        }

        socket.close();
    }
}