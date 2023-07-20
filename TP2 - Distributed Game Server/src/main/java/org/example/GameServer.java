package org.example;

import org.example.db.Database;
import org.example.db.User;
import org.example.other.GameMode;
import org.example.other.Player;
import org.example.other.ServerResponse;
import org.example.utils.GameMessenger;
import org.example.utils.GameQueue;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;


public class GameServer {
    private static final int PLAYERS_PER_GAME = 2;
    private int game_id = 0;
    private static final Database userDatabase = new Database();
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final int port;
    private static double maxEloDifference = 5; // used when finding a suitable opponent for a ranked game
    private long firstInRankedQueue = 0;    // keeps a track of the player that is for the longest time in the queue for ranked games
    private final ReentrantLock queueLock = new ReentrantLock();
    private final ReentrantLock fileLock = new ReentrantLock();
    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // Bind server socket channel to port
            serverSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("Server is listening on port " + port);

            // Setup threads to handle ranked games arrangements and disconnections
            this.setupThreads();

            // "main" thread that handles new connections
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("New client connected: " + socketChannel.getRemoteAddress());
                executor.submit(() -> {
                    Player player;
                    player = this.authenticate(socketChannel);

                    this.enterQueue(player);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setupThreads()
    {
        // thread to handle ranked games arrangements
        executor.submit(() -> {
            while (true) {
                // check if there are enough players in the queue
                queueLock.lock();
                handleRankedGameLogic();
                queueLock.unlock();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Thread to handle disconnections
        executor.submit(() -> {
            while (true) {
                // has anyone disconnected?
                queueLock.lock();
                GameQueue.handleDisconnections();
                queueLock.unlock();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void enterQueue(Player player)
    {
        queueLock.lock();

        boolean wasAlreadyInQueue = GameQueue.isPlayerInQueue(player) != null;
        if (wasAlreadyInQueue)
            System.out.println("User : " + player.getUsername() + " has reconnected to the server and wants to play in " + player.getOption() + " mode");
        else
            System.out.println("User : " + player.getUsername() + " has connected to the server and wants to play in " + player.getOption() + " mode");

        if (player.getOption().equals(GameMode.SIMPLE)) {
            if (!wasAlreadyInQueue)
                GameQueue.addPlayer(player);

            if (GameQueue.getSimpleQueue().size() == PLAYERS_PER_GAME) {
                // are all players ready?
                if (GameQueue.arePlayersReady(player.getOption())) {
                    System.out.println("All players are ready");
                    // start the game
                    GameMessenger.broadcastMessage(ServerResponse.START.getMessage(), GameQueue.getSimpleQueue());
                    handleGameExecution(GameMode.SIMPLE, GameQueue.getSimpleQueue());
                }

                else {
                    System.out.println("Not all players are ready");
                }

            }
            else
                System.out.println(GameQueue.getSimpleQueue().size() + "/" + PLAYERS_PER_GAME + " players are in the simple queue");
        }
        else if (player.getOption().equals(GameMode.RANKED)) {
            if (GameQueue.getRankedQueue().size() == 0) {
                // first player in the ranked queue
                firstInRankedQueue = System.currentTimeMillis();
            }

            if (!wasAlreadyInQueue)
                GameQueue.addPlayer(player);
        }
        queueLock.unlock();
    }


    /**
     * Given a new connection, this method handles it
     *
     * @param socketChannel - the socket that is trying to connect to the server
     * @return - the player that has connected to the server
     */
    public Player authenticate(SocketChannel socketChannel) {
        String username;
        String password;
        ServerResponse response;

        // keep reading until the client has logged in
        do {
            // split the message (username:password)
            String message = GameMessenger.readMessage(socketChannel);
            username = message.split(":")[0];
            password = message.split(":")[1];

            response = handleAuth(username, password, socketChannel);
            GameMessenger.sendMessage(socketChannel, response.toString());
        } while (response != ServerResponse.OK && response != ServerResponse.ALREADY_IN_QUEUE);

        if (response == ServerResponse.ALREADY_IN_QUEUE)
            return GameQueue.isPlayerInServer(username);

        // Read game mode from user -> "SIMPLE" or "RANKED"
        String gameMode = GameMessenger.readMessage(socketChannel);

        double elo = 0;

        // does the player exist in the database?
        User usr = userDatabase.userExists(username);
        if (usr != null)
            elo = usr.getElo();

        return new Player(socketChannel, username, GameMode.valueOf(gameMode), elo);
    }


    /**
     * Given a username and a password, this method handles the authentication using the database
     *
     * @param username      - the username of the user
     * @param password      - the password of the user
     * @param socketChannel - the socket of the user
     * @return - the response of the server, depending on the result of the authentication
     */
    public ServerResponse handleAuth(String username, String password, SocketChannel socketChannel) {
        ServerResponse response;

        if (userDatabase.userExists(username) != null) {
            // is a player with the same username already logged in?

            if (userDatabase.doesPasswordMatch(username, password)) {
                Player player;
                player = GameQueue.isPlayerInServer(username);

                // already in a queue
                if (player != null) {
                    // is this a player that has disconnected?
                    if (player.isDisconnected()) {
                        System.out.println("Player " + player.getUsername() + " has rejoined the queue for a " + player.getOption() + " game");

                        // reconnected
                        player.setSocket(socketChannel);
                        player.setDisconnected(false);

                        return ServerResponse.ALREADY_IN_QUEUE;
                    } else {
                        return ServerResponse.ALREADY_LOGGED_IN;
                    }
                }

                // not in a queue
                else {
                    response = ServerResponse.OK;
                }
            }

            else
                response = ServerResponse.WRONG_PASSWORD;
        }
        else {
            if (username.length() < 5)
                response = ServerResponse.SHORT_USERNAME;
            else {
                response = ServerResponse.OK;
                fileLock.lock();
                userDatabase.addUser(username, password);
                fileLock.unlock();
            }
        }

        return response;
    }

    
    public void handleGameExecution(GameMode gameMode, List<Player> players) {
        List<Player> batch = new ArrayList<>(players);

        game_id++;

        if (queueLock.getHoldCount() > 0)
            queueLock.unlock();

        // clear the queue
        GameQueue.clearQueue(GameQueue.getQueue(gameMode), players);

        // start the game
        Game game = new Game(batch, gameMode, game_id);
        game.start();

        // the game has ended, time to update the players elo and database
        System.out.println("[SIMPLE] Game " + game.getId() + " has ended");

        fileLock.lock();

        // update the elo of the players
        game.updatePlayerElo(userDatabase);

        // write the updated elo to the database
        userDatabase.writeToFile();
        fileLock.unlock();
    }


    public void handleRankedGameLogic() {
        // no one on the queue -> no need to lighten the conditions
        if (GameQueue.getRankedQueue().size() == 0){
            return;
        }

        else {
            // has the first player in the queue waited for more than 10 seconds?
            if (System.currentTimeMillis() - firstInRankedQueue > 10000) {
                maxEloDifference += 5;
                firstInRankedQueue = System.currentTimeMillis();
                System.out.println("Increased maxEloDifference to " + maxEloDifference);
            }
        }

        if (GameQueue.getRankedQueue().size() >= PLAYERS_PER_GAME) {
            // check if there are enough players in the queue with similar elo
            List<Player> suitablePlayers = selectRankedPlayers(GameQueue.getRankedQueue());

            if (suitablePlayers != null){
                // start the game
                GameMessenger.broadcastMessage(ServerResponse.START.getMessage(), suitablePlayers);
                handleGameExecution(GameMode.RANKED, suitablePlayers);
            }

        }
        else
            System.out.println(GameQueue.getRankedQueue().size() + "/" + PLAYERS_PER_GAME + " players are in the ranked queue");
    }


    /**
     * Given a list of players, this method selects a batch of PLAYERS_PER_GAME players to play a ranked game
     * @param players - List of players to check for a suitable batch
     * @return - a list of PLAYERS_PER_GAME players to play a ranked game or null if there are not enough players or the elo difference is too high
     */
    public List<Player> selectRankedPlayers(List<Player> players) {
        // sort the players in the ranked queue by elo
        players.sort(Comparator.comparingDouble(Player::getElo));

        List<Player> selectedPlayers = new ArrayList<>();
        selectedPlayers.add(players.get(0));    // Add the first player to the list

        // Iterate through the remaining players
        for (int i = 1; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            Player lastSelectedPlayer = selectedPlayers.get(selectedPlayers.size() - 1);

            // Check if the Elo difference is within the threshold
            if (Math.abs(currentPlayer.getElo() - lastSelectedPlayer.getElo()) <= maxEloDifference) {
                selectedPlayers.add(currentPlayer);
            } else {
                break; // Stop iterating if the Elo difference exceeds the threshold
            }
        }

        // Check the size of the selected players list
        if (selectedPlayers.size() < PLAYERS_PER_GAME) {
            // Not enough suitable players within the Elo difference threshold
            return null;
        } else {
            // Return the first N players from the selected players list
            return selectedPlayers.subList(0, PLAYERS_PER_GAME);
        }
    }
}
