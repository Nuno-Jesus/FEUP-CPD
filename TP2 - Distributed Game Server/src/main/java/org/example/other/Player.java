package org.example.other;

import org.example.utils.GameMessenger;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Player {
    private SocketChannel socketChannel;
    private final String username;
    private GameMode option;
    private int score;
    private double elo;
    private boolean playing;
    private boolean blewUp;
    private boolean disconnected;
    private long disconnectedSince;

    public Player(SocketChannel socketChannel, String username, GameMode mode, double elo){
        this.socketChannel = socketChannel;
        this.username = username;
        this.elo = elo;
        this.score = 0;
        this.option = mode;
        this.playing = false;
        this.blewUp = false;
        this.disconnectedSince = 0;
    }

    public SocketChannel getSocket() {
        return socketChannel;
    }

    public void setSocket(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }


    public String getUsername() {
        return username;
    }

    public double getElo() {
        return elo;
    }

    public void setElo(double elo) {
        this.elo += elo;
    }


    public GameMode getOption() {
        return option;
    }

    public void setOption(GameMode option) {
        this.option = option;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score += score;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setBlewUp(boolean blewUp) {
        this.blewUp = blewUp;
    }

    public boolean hasBlewUp() {
        return blewUp;
    }

    public boolean hasLeftServer() {
//        System.out.println("Checking if player " + username + " has left the server");

        // ping the socket
        ByteBuffer buffer = ByteBuffer.allocate(3);
        try {
            socketChannel.read(buffer);
//            String message = new String(buffer.array()).trim();
//            System.out.println("Received message from player " + username + ": " + message);
        } catch (IOException e) {
//            e.printStackTrace();

            disconnected = true;

            if (disconnectedSince == 0)
            {
                System.out.println("Player \"" + username + "\" disconnected from the server");
                disconnectedSince = System.currentTimeMillis();
            }

            return true;
        }
//        System.out.println("Player " + username + " is still connected to the server");
        return false;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public long disconnectedSince() {
        return disconnectedSince;
    }

}
