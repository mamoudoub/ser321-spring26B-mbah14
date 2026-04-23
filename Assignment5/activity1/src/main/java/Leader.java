/**
 * Leader.java
 *
 * Distributed Consensus System - Leader Node
 *
 * This class implements the leader in a distributed leader-worker architecture.
 * The leader assigns arithmetic tasks to worker nodes, collects their responses,
 * and determines a consensus result using majority voting.
 *
 * The leader uses CountDownLatch to synchronize worker responses and ensures
 * that all worker results are collected (or a timeout occurs) before computing
 * consensus.
 *
 * Author: Mamoudou Bah
 * Version: 1.0 Date: 4/23/2026
 * Course: SER321 - Principles of Distributed Software Systems
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Leader {

    private static final int MIN_WORKERS = 3;
    private static final int MAX_WORKERS = 5;
    private static final int TIMEOUT_SECONDS = 20;

    private List<WorkerHandler> workers = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        new Leader().start(port);
    }

    public void start(int port) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Leader starting on port " + port);
        System.out.println("Waiting for workers to connect... (need at least 3)");

        int id = 1;

        while (workers.size() < MAX_WORKERS) {
            Socket socket = serverSocket.accept();
            WorkerHandler worker = new WorkerHandler(socket, "Worker" + id++);
            workers.add(worker);

            System.out.println(worker.name + " connected from " +
                    socket.getRemoteSocketAddress());

            if (workers.size() >= MIN_WORKERS) {
                System.out.println("Currently connected: " + workers.size());
            }
        }

        System.out.println("All " + workers.size() +
                " workers connected. Starting consensus rounds...\n");

        Scanner scanner = new Scanner(System.in);
        int round = 1;

        while (true) {
            System.out.println("Please enter an arithmetic task (or 'quit'):");
            String task = scanner.nextLine();

            if (task.equalsIgnoreCase("quit")) break;

            System.out.println("Round " + round++ +
                    ": Assigning task \"" + task + "\"");

            CountDownLatch latch = new CountDownLatch(workers.size());
            Map<String, Integer> results = new ConcurrentHashMap<>();

            // Send task
            for (WorkerHandler w : workers) {
                w.prepareForRound(latch, results);
                w.send(task);
            }

            // Wait with timeout
            boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                System.out.println("Timeout reached. Proceeding with partial results...");
            }

            // Print responses
            results.forEach((name, value) ->
                    System.out.println("Received from " + name + ": " + value)
            );

            // Consensus
            Map<Integer, Integer> frequency = new HashMap<>();
            for (int val : results.values()) {
                frequency.put(val, frequency.getOrDefault(val, 0) + 1);
            }

            int consensus = -1;
            int maxVotes = 0;

            for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() > maxVotes) {
                    consensus = entry.getKey();
                    maxVotes = entry.getValue();
                }
            }

            int total = workers.size();

            if (maxVotes >= Math.ceil(total / 2.0)) {
                System.out.println("Consensus: " + consensus +
                        " (" + maxVotes + "/" + total + " workers agreed)");
                broadcast("CONSENSUS:" + consensus + ":" + maxVotes + ":" + total);
            } else {
                System.out.println("No consensus reached.");
                System.out.println("Vote distribution: " + frequency);
                broadcast("NO_CONSENSUS:" + frequency.toString());
            }

            System.out.println("Announcing result to all workers...\n");
        }

        serverSocket.close();
    }

    private void broadcast(String msg) {
        for (WorkerHandler w : workers) {
            w.send(msg);
        }
    }
}

class WorkerHandler {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String name;

    CountDownLatch latch;
    Map<String, Integer> results;

    public WorkerHandler(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        listen();
    }

    public void prepareForRound(CountDownLatch latch, Map<String, Integer> results) {
        this.latch = latch;
        this.results = results;
    }

    public void send(String msg) {
        out.println(msg);
    }

    private void listen() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    int value = Integer.parseInt(line);
                    results.put(name, value);
                    latch.countDown();
                }
            } catch (Exception ignored) {}
        }).start();
    }
}