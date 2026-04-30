package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * RPSImpl = actual SERVER LOGIC for Rock Paper Scissors game.
 *
 * This is what your Node.java should register using:
 * .addService(new RPSImpl())
 *
 * This class is responsible for:
 * - creating matches
 * - storing persistent game state
 * - processing moves
 * - returning match status
 */
public class RPSImpl extends RPSGrpc.RPSImplBase {

    /**
     * Persistent storage of matches.
     * Key = matchId
     * Value = MatchState (custom object below)
     */
    private final Map<String, MatchState> matches = new HashMap<>();

    private final Random random = new Random();

    // ===========================
    // JOIN MATCH
    // ===========================
    @Override
    public void joinMatch(JoinReq req, StreamObserver<JoinRes> responseObserver) {

        String matchId = "MATCH-" + System.currentTimeMillis();

        MatchState match = new MatchState();
        match.player1 = req.getPlayerName();
        match.playerScore = 0;
        match.opponentScore = 0;

        matches.put(matchId, match);

        JoinRes response = JoinRes.newBuilder()
                .setMatchId(matchId)
                .setMessage("Player " + req.getPlayerName() + " joined. Waiting for opponent...")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===========================
    // PLAY MOVE
    // ===========================
    @Override
    public void playMove(MoveReq req, StreamObserver<MoveRes> responseObserver) {

        MatchState match = matches.get(req.getMatchId());

        if (match == null) {
            responseObserver.onNext(MoveRes.newBuilder()
                    .setResult("ERROR")
                    .setMessage("Match not found")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        Choice playerMove = req.getChoice();
        Choice opponentMove = randomMove();

        String roundResult;

        // RPS logic
        if (playerMove == opponentMove) {
            roundResult = "draw";
        } else if (wins(playerMove, opponentMove)) {
            match.playerScore++;
            roundResult = "win";
        } else {
            match.opponentScore++;
            roundResult = "lose";
        }

        // Persistent history (REQUIRED BY ASSIGNMENT)
        String summary = req.getPlayerName() +
                " played " + playerMove +
                " vs " + opponentMove +
                " => " + roundResult;

        match.roundSummary.add(summary);

        boolean gameOver = match.playerScore >= 3 || match.opponentScore >= 3;

        MoveRes response = MoveRes.newBuilder()
                .setResult(roundResult)
                .setPlayerScore(match.playerScore)
                .setOpponentScore(match.opponentScore)
                .setGameOver(gameOver)
                .setMessage("Round completed")
                .addAllRoundSummary(match.roundSummary)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===========================
    // GET STATUS
    // ===========================
    @Override
    public void getStatus(StatusReq req, StreamObserver<StatusRes> responseObserver) {

        MatchState match = matches.get(req.getMatchId());

        if (match == null) {
            responseObserver.onNext(StatusRes.newBuilder()
                    .setStatus("Match not found")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        StatusRes response = StatusRes.newBuilder()
                .setStatus("In progress")
                .setPlayerScore(match.playerScore)
                .setOpponentScore(match.opponentScore)
                .setGameOver(match.playerScore >= 3 || match.opponentScore >= 3)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===========================
    // GAME LOGIC HELPERS
    // ===========================
    private Choice randomMove() {
        Choice[] values = Choice.values();
        return values[random.nextInt(values.length)];
    }

    private boolean wins(Choice p, Choice o) {
        return (p == Choice.ROCK && o == Choice.SCISSORS)
                || (p == Choice.PAPER && o == Choice.ROCK)
                || (p == Choice.SCISSORS && o == Choice.PAPER);
    }

    // ===========================
    // INTERNAL STATE CLASS
    // ===========================
    private static class MatchState {
        String player1;
        int playerScore;
        int opponentScore;

        // REQUIRED: persistent repeated field behavior
        java.util.List<String> roundSummary = new java.util.ArrayList<>();
    }
}