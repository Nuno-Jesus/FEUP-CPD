package org.example.utils;

import org.example.other.GameMode;
import org.example.other.Player;
import org.example.other.ServerResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class GameQueue {
    private static final List<Player> players = new ArrayList<>(); // List of sockets that are connected to the server
    private static final List<Player> simpleQueue = new ArrayList<>(); // List of sockets that are waiting to be assigned to a non-ranked game
    private static final List<Player> rankedQueue = new ArrayList<>(); // List of sockets that are waiting to be assigned to a ranked game
    private static final int MAX_DISCONNECTED_TIME = 20; // 20 seconds

    public static List<Player> getPlayers() {
        return players;
    }

    public static List<Player> getSimpleQueue() {
        return simpleQueue;
    }

    public static List<Player> getRankedQueue() {
        return rankedQueue;
    }

    public static List<Player> getQueue(GameMode mode) {
        return mode.equals(GameMode.SIMPLE) ? simpleQueue : rankedQueue;
    }

    public static void addPlayer(Player player) {
        if (player.getOption().equals(GameMode.SIMPLE))
            simpleQueue.add(player);
        else
            rankedQueue.add(player);

        players.add(player);
    }

    /**
     * When a game starts, this method removes the players from the queue
     * @param queue The queue to remove the players from
     * @param batch2Remove The players to remove
     * NOTE: In the simple mode, the batch is always the same as the queue
     */
    public static void clearQueue(List<Player> queue, List<Player> batch2Remove) {
        queue.removeAll(batch2Remove);
    }

    public static void removeFromServer(List<Player> playersToRemove) {
        players.removeAll(playersToRemove);
    }


    /**
     * Given a player, checks if he is in the server
     *
     * @param username The username of the player
     * @return The player if he is in the server, null otherwise
     */
    public static Player isPlayerInServer(String username) {
        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }

        return null;
    }

    public static Player isPlayerInQueue(Player player) {
        List<Player> queue = player.getOption().equals(GameMode.SIMPLE) ? simpleQueue : rankedQueue;

        for (Player p : queue) {
            if (p.getUsername().equals(player.getUsername())) {
                return p;
            }
        }

        return null;
    }

    public static void handleDisconnections() {
        long currTime = System.currentTimeMillis();

        // check in the ranked queue
        hasPlayerLeftQueue(currTime, GameQueue.getRankedQueue());

        // check in the simple queue
        hasPlayerLeftQueue(currTime, GameQueue.getSimpleQueue());
    }

    private static void hasPlayerLeftQueue(long currTime, List<Player> queue) {
        List<Player> tmp = queue.stream().filter(player -> !player.isPlaying()).toList();

        if (tmp.size() == 0)
            return;

        GameMessenger.broadcastMessage(ServerResponse.WAITING.getMessage(), tmp);
        for (Player player : tmp) {
            // System.out.println("Checking player " + player.getUsername());
            if (player.hasLeftServer()) {
                // has he left the server for more than MAX_DISCONNECTED_TIME seconds?
                if (currTime - player.disconnectedSince() > MAX_DISCONNECTED_TIME * 1000) {
                    System.out.println("Player " + player.getUsername() + " has been disconnected for more than "
                            + MAX_DISCONNECTED_TIME + " seconds");
                    GameQueue.getPlayers().remove(player);
                    queue.remove(player);
                }
            }
        }
    }

    public static boolean arePlayersReady(GameMode gameMode) {
        List<Player> queue = gameMode.equals(GameMode.SIMPLE) ? GameQueue.getSimpleQueue() : GameQueue.getRankedQueue();

        for (Player player : queue) {
            if (player.isDisconnected()) {
                return false;
            }
        }

        return true;
    }
}
