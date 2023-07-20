package org.example.utils;

import org.example.other.Player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public abstract class GameMessenger {
    public static void sendMessage(SocketChannel socketChannel, String message) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put(message.getBytes());
        buffer.flip();
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
//            System.out.println("SendMessage: " + e.getMessage());
        }
        buffer.clear();
    }


    public static String readMessage(SocketChannel socketChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            socketChannel.read(buffer);
        } catch (IOException e) {
//            System.out.println("ReadMessage: " + e.getMessage());
        }
        buffer.flip();
        String message = new String(buffer.array(), 0, buffer.limit());
        buffer.clear();

        return message;
    }


    /**
     * Sends the same message to all players in the game -> used for warnings, game over, etc.
     *
     * @param message the message to send
     */
    public static void broadcastMessage(String message, List<Player> players ) {
        players.forEach(player -> GameMessenger.sendMessage(player.getSocket(), message));
    }


    /**
     * Sends a message to all players in the game except the specified player
     * @param message the message to send
     * @param player2Ignore the player to exclude
     */
    public static void broadcastMessage(String message, List<Player> players , Player player2Ignore) {
        players.forEach(player -> {
            if (!player.equals(player2Ignore))
                GameMessenger.sendMessage(player.getSocket(), message);
        });
    }
}
