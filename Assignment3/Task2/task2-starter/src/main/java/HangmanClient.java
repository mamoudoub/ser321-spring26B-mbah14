import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 * Hangman Game Client - Student Starter Code
 *
 * Your task: Implement the protocol communication for all game features.
 *
 * What's provided:
 * - Complete menu structure with different game states
 * - Name handling as a complete example
 * - Some method stubs as examples
 *
 * What you need to implement:
 * - Protocol requests/responses for all game operations
 * - Proper response handling and display
 */
public class HangmanClient {
    static Socket sock;
    static ObjectOutputStream oos;
    static ObjectInputStream in;

    static Scanner scanner = new Scanner(System.in);
    static boolean inGame = false;
    static boolean hasName = false;
    static String playerName = "";

    static String currentWordDisplay = "";
    static int currentMisses = 0;
    static int currentPoints = 0;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            sock = new Socket(host, port);
            oos = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());

            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║     WELCOME TO HANGMAN GAME!           ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println();

            boolean running = true;
            while (running) {
                if (!hasName) {
                    running = showInitialMenu();
                } else if (!inGame) {
                    running = showMainMenu();
                } else {
                    running = showGameMenu();
                }
                System.out.println();
            }

            closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial menu - before name is set
     */
    static boolean showInitialMenu() {
        System.out.println("────────────────────────────────────────");
        System.out.println("  1. Set Your Name");
        System.out.println("  2. Quit");
        System.out.println("────────────────────────────────────────");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                setName();
                return true;
            case "2":
                quit();
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    /**
     * Main menu - after name set, no active game
     */
    static boolean showMainMenu() {
        System.out.println("────────────────────────────────────────");
        System.out.println("MAIN MENU:");
        System.out.println("  1. Start New Game");
        System.out.println("  2. View Leaderboard");
        System.out.println("  3. Quit");
        System.out.println("────────────────────────────────────────");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            //========= IMPLEMENTATION STARTS =====
            case "1":
                startGame();
                return true;
            case "2":
                viewLeaderboard();
                return true;
            case "3":
                quit();
                return false;
            //========= IMPLEMENTATION ENDS =====
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }

    /**
     * Game menu - during active game
     * Natural input: just type letter/word to guess
     * Commands: 1, 2, 3, 4, 0 for special actions
     */
    static boolean showGameMenu() {
        System.out.println("\n────────────────────────────────────────");
        System.out.println("Type a letter or word to guess");
        System.out.println("Or choose:");
        System.out.println("  1 - Show game state");
        System.out.println("  2 - See guessed letters");
        System.out.println("  3 - Get a hint (-8 points)");
        System.out.println("  4 - Give up (return to main menu)");
        System.out.println("  0 - Quit game");
        System.out.println("────────────────────────────────────────");
        System.out.print("Your input: ");
        String input = scanner.nextLine().trim();

        //========= IMPLEMENTATION STARTS =====
        if (input.equals("1")) {
            showGameState();
            return true;
        } else if (input.equals("2")) {
            showGuessedLetters();
            return true;
        } else if (input.equals("3")) {
            getHint();
            return true;
        } else if (input.equals("4")) {
            giveUp();
            return false;
        } else if (input.equals("0")) {
            quit();
            return false;
        }

        if (input.isEmpty()) {
            System.out.println("Please enter a letter, word, or command.");
            return true;
        }

        // Single character = letter guess, multiple = word guess
        if (input.length() == 1) {
            guessLetter(input.charAt(0));
        } else {
            guessWord(input);
        }
        return true;
        //========= IMPLEMENTATION ENDS =====
    }

    /**
     * EXAMPLE IMPLEMENTATION: Set player name
     */
    static void setName() {
        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine().trim();

        JSONObject request = new JSONObject();
        request.put("type", "name");
        request.put("name", name);

        JSONObject response = sendRequest(request);
        if (response != null) {
            if (response.getBoolean("ok")) {
                hasName = true;
                playerName = name;
                System.out.println("\n" + response.getString("message"));
                System.out.println();
            } else {
                System.out.println("✗ Error: " + response.getString("message"));
            }
        }
    }

    //========= IMPLEMENTATION STARTS =====
    static void startGame() {
        JSONObject request = new JSONObject();
        request.put("type", "start");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            inGame = true;
            currentWordDisplay = response.getString("word_display");
            currentMisses = response.getInt("misses");
            System.out.println("\nNew game started!");
            System.out.println(response.getString("hangman"));
            System.out.println("Word: " + currentWordDisplay);
        }
    }

    static void showGameState() {
        JSONObject request = new JSONObject();
        request.put("type", "state");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            currentWordDisplay = response.getString("word_display");
            currentMisses = response.getInt("misses");
            currentPoints = response.getInt("points");
            System.out.println("\nCurrent State:");
            System.out.println(response.getString("hangman"));
            System.out.println("Word: " + currentWordDisplay);
            System.out.println("Misses: " + currentMisses + ", Points: " + currentPoints);
        }
    }

    static void showGuessedLetters() {
        JSONObject request = new JSONObject();
        request.put("type", "guessed");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            System.out.println("\nGuessed Letters: " + response.getString("letters"));
        }
    }

    static void getHint() {
        JSONObject request = new JSONObject();
        request.put("type", "hint");

        JSONObject response = sendRequest(request);
        if (response != null) {
            if (response.getBoolean("ok")) {
                currentWordDisplay = response.getString("word_display");
                currentPoints = response.getInt("points");
                System.out.println("\nHint revealed: " + response.getString("letter"));
                System.out.println("Word: " + currentWordDisplay);
                System.out.println("Points: " + currentPoints);
            } else {
                System.out.println("\n✗ " + response.getString("message"));
            }
        }
    }

    static void guessLetter(char letter) {
        JSONObject request = new JSONObject();
        request.put("type", "guess");
        request.put("letter", String.valueOf(letter));

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            currentWordDisplay = response.getString("word_display");
            currentMisses = response.getInt("misses");
            currentPoints = response.getInt("points");
            System.out.println("\nWord: " + currentWordDisplay);
            System.out.println(response.getString("hangman"));
            System.out.println("Misses: " + currentMisses + ", Points: " + currentPoints);
            if (response.has("message")) {
                System.out.println(response.getString("message"));
                inGame = false;
            }
        } else if (response != null) {
            System.out.println("✗ " + response.getString("message"));
        }
    }

    static void guessWord(String word) {
        System.out.println("\n[Word guesses are not fully implemented yet]");
    }

    static void viewLeaderboard() {
        JSONObject request = new JSONObject();
        request.put("type", "leaderboard");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            System.out.println("\nLEADERBOARD:");
            System.out.println(response.getString("leaderboard"));
        }
    }

    static void giveUp() {
        System.out.print("\nAre you sure you want to give up? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            JSONObject request = new JSONObject();
            request.put("type", "giveup");
            JSONObject response = sendRequest(request);
            if (response != null && response.getBoolean("ok")) {
                System.out.println("\n" + response.getString("message"));
            }
            inGame = false;
        } else {
            System.out.println("\nContinuing game...");
        }
    }
    //========= IMPLEMENTATION ENDS =====

    /**
     * Quit game
     */
    static boolean quit() {
        JSONObject request = new JSONObject();
        request.put("type", "quit");

        JSONObject response = sendRequest(request);
        if (response != null && response.getBoolean("ok")) {
            System.out.println("\n" + response.getString("message"));
            System.out.println("Thanks for playing!");
        }
        return false; // Stop the main loop
    }

    /**
     * Helper: Send request and receive response
     * This handles the basic communication pattern
     */
    static JSONObject sendRequest(JSONObject request) {
        try {
            String req = request.toString();
            oos.writeObject(req);
            oos.flush();

            String res = (String) in.readObject();
            return new JSONObject(res);
        } catch (Exception e) {
            System.out.println("Error communicating with server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Close connection
     */
    static void closeConnection() {
        try {
            if (oos != null) oos.close();
            if (in != null) in.close();
            if (sock != null) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}