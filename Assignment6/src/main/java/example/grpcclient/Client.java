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

    /** Construct client for accessing server using the existing channel. */
    public Client(Channel channel, Channel regChannel) {
        blockingStub = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub3 = RegistryGrpc.newBlockingStub(regChannel);
        blockingStub4 = RegistryGrpc.newBlockingStub(channel);
        blockingStub5 = RPSGrpc.newBlockingStub(channel);
    }

    /** Construct client for accessing server using the existing channel. */
    public Client(Channel channel) {
        blockingStub = EchoGrpc.newBlockingStub(channel);
        blockingStub2 = JokeGrpc.newBlockingStub(channel);
        blockingStub3 = null;
        blockingStub4 = null;
        blockingStub5 = RPSGrpc.newBlockingStub(channel);
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

        try {

        // ##############################################################################
        // ## Assume we know the port here from the service node it is basically set through Gradle
        // here.
        // In your version you should first contact the registry to check which services
        // are available and what the port
        // etc is.

        /**
         * Your client should start off with
         * 1. contacting the Registry to check for the available services
         * 2. List the services in the terminal and the client can
         *    choose one (preferably through numbering)
         * 3. Based on what the client chooses
         *    the terminal should ask for input, eg. a new sentence, a sorting array or
         *    whatever the request needs
         * 4. The request should be sent to one of the
         *    available services (client should call the registry again and ask for a
         *    Server providing the chosen service) should send the request to this service and
         *    return the response in a good way to the client
         *
         * You should make sure your client does not crash in case the service node
         * crashes or went offline.
         */

        // Just doing some hard coded calls to the service node without using the
        // registry
        // create client
        Client client = new Client(channel, regChannel);

        // call the parrot service on the server
        client.askServerToParrot(message);

        // ask the user for input how many jokes the user wants
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // ===========================
        // RPS GAME (ADDED - TASK 2)
        // ===========================

        System.out.println("\n=== RPS GAME ===");

        System.out.println("Enter your name:");
        String player = reader.readLine();

        client.joinMatch(player);

        System.out.println("Enter match ID:");
        String matchId = reader.readLine();

        System.out.println("Choose move (ROCK, PAPER, SCISSORS):");
            String move = reader.readLine().trim().toUpperCase();

            client.playMove(matchId, player, Choice.valueOf(move.trim().toUpperCase()));

        client.getStatus(matchId);

        // Reading data using readLine
        System.out.println("How many jokes would you like?"); // NO ERROR handling of wrong input here.
        String num = reader.readLine();

        // calling the joked service from the server with num from user input
        client.askForJokes(Integer.valueOf(num));

        // adding a joke to the server
        client.setJoke("I made a pencil with two erasers. It was pointless.");

        // showing 6 joked
        client.askForJokes(Integer.valueOf(6));

        // list all the services that are implemented on the node that this client is connected to

        System.out.println("Services on the connected node. (without registry)");
        client.getNodeServices(); // get all registered services

        // ############### Contacting the registry just so you see how it can be done

        if (args[5].equals("true")) {
            // Comment these last Service calls while in Activity 1 Task 1, they are not needed and wil throw issues without the Registry running
            // get thread's services
            client.getServices(); // get all registered services

            // get parrot
            client.findServer("services.Echo/parrot"); // get ONE server that provides the parrot service

            // get all setJoke
            client.findServers("services.Joke/setJoke"); // get ALL servers that provide the setJoke service

            // get getJoke
            client.findServer("services.Joke/getJoke"); // get ALL servers that provide the getJoke service

            // does not exist
            client.findServer("random"); // shows the output if the server does not find a given service
        }

    } finally {
        // ManagedChannels use resources like threads and TCP connections. To prevent
        // leaking these
        // resources the channel should be shut down when it will no longer be used. If
        // it may be used
        // again leave it running.
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        if (args[5].equals("true")) {
            regChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
  }
}