package org.example;

import org.example.cards.*;
import org.example.db.Database;
import org.example.other.GameMode;
import org.example.other.Player;
import org.example.other.ServerResponse;
import org.example.other.EloCalculator;
import org.example.utils.Color;
import org.example.utils.GameMessenger;
import org.example.utils.GameQueue;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final int id;
    private final List<Player> players;
    private final GameMode mode;
    private final CardDeck deck;
    private List<Player> winners;


    public Game(List<Player> players, GameMode mode, int id) {
        this.players = players;
        this.mode = mode;
        this.deck = new CardDeck();
        this.id = id;
        this.winners = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void start() {
        System.out.println("[" + mode + "] Game " + id + " has started with " + players.size() + " players");

        // set players' status to playing
        players.forEach(player -> player.setPlaying(true));

        // shuffle the deck
        deck.shuffle();

        // countdown from 3 to 1
        startCountdown();

        // play the first round
        playFirstRound();

        // play remaining rounds, return the winner
        this.winners = playGame();

        // Remove players from server
        GameQueue.removeFromServer(players);
    }

    /**
     * Countdown from 3 to 1
     */
    public void startCountdown() {
        for (int i = 3; i > 0; i--) {
            String message = "The game will start in " + i + " second(s)";
            GameMessenger.broadcastMessage(message + "#N", this.players);
            // sleep for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     First round -> Give each player a card
     */
    public void playFirstRound() {
        for (Player player : players) {
            // draw a card
            Card card = deck.drawCard();

            // update the player's score
            player.setScore(card.getValue());

            // send the card to the player
            String message = log(card, player);

            GameMessenger.sendMessage(player.getSocket(), message + "#N");
        }
    }

    /**
     * Called after drawing the first card for each player
     * @return the winner(s) of the game - in case of a tie, returns all players with the highest score
     */
    public List<Player> playGame() {
        String message;
        String response;

        boolean gameFinished = false;

        while (!gameFinished) {
            // receive responses from each player
            for (Player player : players) {
                if (!player.isPlaying()) {
                    // tell the player they're not playing
                    GameMessenger.sendMessage(player.getSocket(), "Waiting for other players to finish...#N");
                    continue;
                }

                // ask each player if they want to draw another card
                GameMessenger.sendMessage(player.getSocket(), "Do you want to draw another card? [y/n]#Y");

                // tell other players that this player is drawing a card
                GameMessenger.broadcastMessage(player.getUsername() + " is drawing a card...#N", this.players, player);

                // read the response from the player
                response = GameMessenger.readMessage(player.getSocket());

                while (!response.equals("y") && !response.equals("n")) {
                    GameMessenger.sendMessage(player.getSocket(), "Do you want to draw another card? [y/n]#Y");
                    response = GameMessenger.readMessage(player.getSocket());
                }

                // did the player ask for another card?
                if (response.equals("y")) {
                    // draw a card
                    Card card = deck.drawCard();

                    // update the player's score
                    player.setScore(card.getValue());

                    // send the card to the player
                    message = log(card, player);
                    GameMessenger.sendMessage(player.getSocket(), message + "#N");

                    if (player.getScore() > 21) {
                        player.setBlewUp(true);
                        player.setPlaying(false);

                        player.setScore(-card.getValue());

                        message = ServerResponse.BLEW_UP.getMessage() + ":" + player.getScore();
                        GameMessenger.sendMessage(player.getSocket(), message + "#N");
                    }
                } else {
                    player.setPlaying(false);
                    message = "You've decided to stop drawing cards, your final score is " + player.getScore();
                    GameMessenger.sendMessage(player.getSocket(), message + "#N");
                }

                if (isGameOver()) {
                    gameFinished = true;
                    break;
                }
            }
        }

        // find the winners
        List<Player> winners = getWinner();
        boolean tie = winners.size() > 1;

        // message structure
        // No winners -> GAME_END:NO_WINNERS
        // 1 winner -> GAME_END:WINNER_USERNAME:WINNER_SCORE
        // tie -> GAME_END:TIE:WINNER1_USERNAME:WINNER_USERNAME...:SCORE

        if (winners.size() == 0)
            GameMessenger.broadcastMessage(ServerResponse.GAME_END + ":" + ServerResponse.NO_WINNERS + "#N", this.players);
        else if (tie) {
            StringBuilder finalMessage = new StringBuilder();
            finalMessage.append(ServerResponse.GAME_END).append(":").append(ServerResponse.TIE);

            for (Player winner : winners){
                finalMessage.append(":").append(winner.getUsername());
            }

            finalMessage.append(":").append(winners.get(0).getScore());

            GameMessenger.broadcastMessage(finalMessage + "#N", this.players);
        }
        else
        {
            Player winner = winners.get(0);
            GameMessenger.broadcastMessage(ServerResponse.GAME_END + ":" + winner.getUsername()
                    + ":" + winner.getScore() + "#N", this.players);
        }

        return winners;
    }

    /**
     * Checks if the game is over
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        for (Player player : players)
            if (player.isPlaying())
                return false;
        return true;
    }

    /**
     * Logs the card drawn by the player
     * @param card the card drawn
     * @param player the player that drew the card
     * @return the log message to send to the player
     */
    public String log(Card card, Player player)
    {
        return "You drew:\n" + card + "[Value: " +
                Color.BLUE_BRIGHT + card.getValue() + Color.RESET +
                ", Your Score: " + Color.GREEN_BRIGHT + player.getScore() + Color.RESET + "]";
    }

    /**
     * Gets the winner(s) of the game
     * @return List of winners (in case of a tie, returns all players with the highest score)
     */
    public List<Player> getWinner() {
        // winner is the player with the highest score that didn't blow up
        // in case of a tie, returns tied players
        List<Player> winners = new ArrayList<>();

        Player winner = null;

        // sort the players by score
        players.sort((p1, p2) -> p2.getScore() - p1.getScore());

        // find the winners
        for (Player player : players) {
            if (!player.hasBlewUp()) {
                // add the first player that didn't blow up
                if (winner == null) {
                    winner = player;
                    winners.add(player);
                }
                // add all players with the same score as the winner
                else if (player.getScore() == winner.getScore()) {
                    winners.add(player);
                }

            }
        }
        return winners;
    }

    /**
     * Updates the elo of each player in the database
     * @param db the database
     */
    public void updatePlayerElo(Database db) {
        EloCalculator calculator = new EloCalculator(players, winners, db);
        calculator.updateElo();
    }
}