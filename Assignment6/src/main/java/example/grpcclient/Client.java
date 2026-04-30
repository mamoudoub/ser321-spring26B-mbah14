package example.grpcclient;
import service.RPSGrpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import service.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.protobuf.Empty; // needed to use Empty


/**
 * Client that requests `parrot` method from the `EchoServer`.
 */
public class Client {
    private final EchoGrpc.EchoBlockingStub blockingStub;
    private final JokeGrpc.JokeBlockingStub blockingStub2;
    private final RegistryGrpc.RegistryBlockingStub blockingStub3;
    private final RegistryGrpc.RegistryBlockingStub blockingStub4;
    private final RPSGrpc.RPSBlockingStub blockingStub5;
    private final ConverterGrpc.ConverterBlockingStub blockingStub6;

    /** Construct client for accessing server using the existing channel. */
    public Client(Channel channel, Channel regChannel) {
        blockingStub  = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
        blockingStub4 = RegistryGrpc.newBlockingStub(channel);
        blockingStub5 = RPSGrpc.newBlockingStub(channel);
        blockingStub6 = ConverterGrpc.newBlockingStub(channel);
    }


    /** Construct client for accessing server using the existing channel. */
    public Client(Channel channel) {
        blockingStub = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub3 = null;
        blockingStub4 = null;
        blockingStub5 = RPSGrpc.newBlockingStub(channel);
        blockingStub6 = ConverterGrpc.newBlockingStub(channel);
    }

    public void askServerToParrot(String message) {

        ClientRequest request = ClientRequest.newBuilder().setMessage(message).build();
        ServerResponse response;
        try {
            response = blockingStub.parrot(request);
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
            return;
        }
        System.out.println("Received from server: " + response.getMessage());
    }

    public void askForJokes(int num) {
        JokeReq request = JokeReq.newBuilder().setNumber(num).build();
        JokeRes response;

        Empty empt = Empty.newBuilder().build();

        try {
            response = blockingStub2.getJoke(request);
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
        System.out.println("Your jokes: ");
        for (String joke : response.getJokeList()) {
            System.out.println("--- " + joke);
        }
    }

    public void setJoke(String joke) {
        JokeSetReq request = JokeSetReq.newBuilder().setJoke(joke).build();
        JokeSetRes response;

        try {
            response = blockingStub2.setJoke(request);
            System.out.println(response.getOk());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
    }

    public void getNodeServices() {
        GetServicesReq request = GetServicesReq.newBuilder().build();
        ServicesListRes response;
        try {
            response = blockingStub4.getServices(request);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
    }

    public void getServices() {
        GetServicesReq request = GetServicesReq.newBuilder().build();
        ServicesListRes response;
        try {
            response = blockingStub3.getServices(request);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
    }

    public void findServer(String name) {
        FindServerReq request = FindServerReq.newBuilder().setServiceName(name).build();
        SingleServerRes response;
        try {
            response = blockingStub3.findServer(request);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
    }

    public void findServers(String name) {
        FindServersReq request = FindServersReq.newBuilder().setServiceName(name).build();
        ServerListRes response;
        try {
            response = blockingStub3.findServers(request);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e);
            return;
        }
    }

    // ===========================
    // RPS GAME METHODS
    // ===========================

    public void joinMatch(String player) {
        JoinReq request = JoinReq.newBuilder()
                .setPlayerName(player)
                .build();

        try {
            JoinRes response = blockingStub5.joinMatch(request);
            System.out.println("Match ID: " + response.getMatchId());
            System.out.println("Match started successfully!");
            System.out.println(response.getMessage());
        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

    public void playMove(String matchId, String player, Choice choice) {
        MoveReq request = MoveReq.newBuilder()
                .setMatchId(matchId)
                .setPlayerName(player)
                .setChoice(choice)
                .build();

        try {
            MoveRes response = blockingStub5.playMove(request);
            System.out.println("Result: " + response.getResult());
            System.out.println("Score: " + response.getPlayerScore() + " - " + response.getOpponentScore());
            System.out.println(response.getMessage());

            for (String s : response.getRoundSummaryList()) {
                System.out.println("Round: " + s);
            }

        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

    public void getStatus(String matchId) {
        StatusReq request = StatusReq.newBuilder()
                .setMatchId(matchId)
                .build();

        try {
            StatusRes response = blockingStub5.getStatus(request);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Score: " + response.getPlayerScore() + " - " + response.getOpponentScore());
            System.out.println("Game Over: " + response.getGameOver());

            for (String h : response.getMoveHistoryList()) {
                System.out.println("History: " + h);
            }

        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }

    // ===========================
    // RPS CONVERT METHOD
    // ===========================
    public void convertValue(double value, String from, String to) {

        ConversionRequest request = ConversionRequest.newBuilder()
                .setValue(value)
                .setFromUnit(from)
                .setToUnit(to)
                .build();

        try {
            ConversionResponse response = blockingStub6.convert(request);

            if (response.getIsSuccess()) {
                System.out.println("Converted value: " + response.getResult());
            } else {
                System.out.println("Conversion error: " + response.getError());
            }

        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
        }
    }
    // ===== END CONVERT METHOD =======


    /**
     * Main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.out
                    .println("Expected arguments: <host(String)> <port(int)> <regHost(string)> <regPort(int)> <message(String)> <regOn(bool)>");
            System.exit(1);
        }

        int port = 9099;
        int regPort = 9003;
        String host = args[0];
        String regHost = args[2];
        String message = args[4];

        try {
            port = Integer.parseInt(args[1]);
            regPort = Integer.parseInt(args[3]);
        } catch (NumberFormatException nfe) {
            System.out.println("[Port] must be an integer");
            System.exit(2);
        }

        String target = host + ":" + port;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext().build();

        String regTarget = regHost + ":" + regPort;
        ManagedChannel regChannel = ManagedChannelBuilder.forTarget(regTarget)
                .usePlaintext().build();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {

            // ##############################################################################
            // ## Assume we know the port here from the service node it is basically set through Gradle
            // here.
            // In your version you should first contact the Registry to check which services
            // are available and what the port etc is.

            /**
             * Your client should start off with
             * 1. contacting the Registry to check for the available services
             * 2. List the services in the terminal and the client can
             *    choose one (preferably through numbering)
             * 3. Based on what the client chooses
             *    the terminal should ask for input
             * 4. The request should be sent to the selected service
             *    and return response in a clean way
             */

            Client client = new Client(channel, regChannel);

            // call the parrot service on the server
            client.askServerToParrot(message);

            // ===========================
            // MENU SYSTEM (TASK 1 FIX)
            // ===========================
            while (true) {

                System.out.println("\n================ MENU ================");
                System.out.println("1. Echo (Parrot)");
                System.out.println("2. Joke Service");
                System.out.println("3. Converter Service");
                System.out.println("4. RPS Game");
                System.out.println("5. Registry Calls");
                System.out.println("6. Exit");
                System.out.println("=====================================");
                System.out.print("Choose option: ");

                String choice = reader.readLine();

                switch (choice) {

                    // --------------------------
                    // ECHO SERVICE
                    // --------------------------
                    case "1":
                        System.out.print("Enter message: ");
                        String msg = reader.readLine();
                        client.askServerToParrot(msg);
                        break;

                    // --------------------------
                    // JOKE SERVICE
                    // --------------------------
                    case "2":
                        System.out.println("\n=== JOKE SERVICE ===");

                        System.out.print("How many jokes would you like? ");
                        String num = reader.readLine();

                        // calling the joke service from the server with num from user input
                        client.askForJokes(Integer.valueOf(num));

                        // adding a joke to the server
                        client.setJoke("I made a pencil with two erasers. It was pointless.");

                        // showing 6 jokes (as required by assignment sample code)
                        client.askForJokes(6);

                        // list all services on node (without registry)
                        System.out.println("\nServices on the connected node (without registry):");
                        client.getNodeServices();

                        break;

                    // --------------------------
                    // CONVERTER SERVICE
                    // --------------------------
                    case "3":
                        System.out.println("\n=== CONVERTER SERVICE ===");

                        System.out.print("Enter value: ");
                        double value = Double.parseDouble(reader.readLine());

                        System.out.print("Enter from unit (KILOMETER, MILE, CELSIUS, etc): ");
                        String from = reader.readLine().trim().toUpperCase();

                        System.out.print("Enter to unit: ");
                        String to = reader.readLine().trim().toUpperCase();

                        client.convertValue(value, from, to);
                        break;

                    // --------------------------
                    // RPS GAME
                    // --------------------------
                    case "4":
                        System.out.println("\n=== RPS GAME ===");

                        System.out.print("Enter player name: ");
                        String player = reader.readLine();

                        client.joinMatch(player);

                        System.out.print("Enter match ID: ");
                        String matchId = reader.readLine();

                        System.out.print("Move (ROCK, PAPER, SCISSORS): ");
                        String move = reader.readLine().trim().toUpperCase();

                        client.playMove(matchId, player, Choice.valueOf(move));

                        client.getStatus(matchId);
                        break;

                    // --------------------------
                    // REGISTRY TESTING
                    // --------------------------
                    case "5":

                        if (args[5].equals("true")) {

                            System.out.println("\n=== REGISTRY SERVICES ===");

                            // get thread's services
                            client.getServices();

                            // get parrot
                            client.findServer("services.Echo/parrot");

                            // get all setJoke
                            client.findServers("services.Joke/setJoke");

                            // get getJoke
                            client.findServer("services.Joke/getJoke");

                            // does not exist (test error handling)
                            client.findServer("random");

                        } else {
                            System.out.println("Registry disabled (regOn=false)");
                        }

                        break;

                    // --------------------------
                    // EXIT
                    // --------------------------
                    case "6":
                        System.out.println("Exiting client...");
                        return;

                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            if (args[5].equals("true")) {
                regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }
}