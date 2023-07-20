package org.example;

import org.example.utils.GameMessenger;
import org.example.other.ServerResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class GameClient {
    private String username;
    private final int port;
    private SocketChannel socketChannel;

    public GameClient(int port) {
        this.port = port;
    }

    public void start() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", port));
            System.out.println("Starting client on port " + port);

            //Authenticate and ask for game mode to queue up
            Scanner scanner = new Scanner(System.in);
            boolean isAlreadyInQueue = this.authenticate(scanner);

            // send the game mode to the server if the user is not already in queue
            if (!isAlreadyInQueue){
                int choice = this.askGameMode(scanner);

                // Send game mode to server
                GameMessenger.sendMessage(socketChannel, choice == 1 ? "RANKED" : "SIMPLE");
                System.out.println("Joined the queue to play " + (choice == 1 ? "RANKED" : "SIMPLE") + " mode!");
            }

            // keep reading from server until the response is "START"
            ServerResponse response;
            do {
                response = ServerResponse.valueOf(GameMessenger.readMessage(socketChannel));
                if (response == ServerResponse.START)
                    break;
                GameMessenger.sendMessage(socketChannel, "ACK");
            } while (true);

            play();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public boolean authenticate(Scanner scanner)
    {
        ServerResponse response;
        boolean isAlreadyInQueue = false;

        // Loop until the user has logged in
        while (true) {
            System.out.print("Username: ");
            this.username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            // Send username and password to server (with a delimiter ":")
            GameMessenger.sendMessage(socketChannel,this.username + ":" + password);

            response = ServerResponse.valueOf(GameMessenger.readMessage(socketChannel));
            if (response == ServerResponse.WRONG_PASSWORD)
                System.out.println("Login failed! -> Wrong password!");
            else if (response == ServerResponse.SHORT_USERNAME)
                System.out.println("Login failed! -> Username too short!");
            else if (response == ServerResponse.ALREADY_LOGGED_IN)
                System.out.println("Login failed! -> You are already logged in!");
            else if (response == ServerResponse.ALREADY_IN_QUEUE)
            {
                System.out.println("Reconnected to the queue...");
                isAlreadyInQueue = true;
                break;
            }
            else if (response == ServerResponse.OK)
                break;
        }

        return isAlreadyInQueue;
    }

    public int askGameMode(Scanner scanner)
    {
        int choice = 0;

        System.out.println("Welcome " + this.username + "!");
        System.out.println("Choose a game mode: ");
        System.out.println("1. Ranked");
        System.out.println("2. Simple");
        System.out.print("Your choice: ");

        choice = scanner.nextInt();
        return (choice);
    }

    public void play() {
        // The Game has started

        String response;
        boolean needsInput;
        boolean gameEnded = false;


        int count = -3;

        while (!gameEnded) {
            // increment response count
            count++;

            // get response from server
            response = GameMessenger.readMessage(socketChannel);

            // is it expecting input?
            needsInput = expectsResponse(response);
            response = response.split("#")[0];

            if (count < 1 && !response.equals(ServerResponse.WAITING.toString()))
                System.out.println(response);
            else
                gameEnded = handleResponse(count, response);

            if (needsInput){
                // ask user for input
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();

                // send input to server
                GameMessenger.sendMessage(socketChannel, input);
            }
        }
    }

    public boolean handleResponse(int count, String response){
        // Did the user blow up?
        if (response.startsWith(ServerResponse.BLEW_UP.toString())) {
            int score = Integer.parseInt(response.split(":")[1]);
            System.out.println("[" + count + "] " + "You blew up! Your final score is: " + score);
        }

        // Is the game over?
        else if (response.startsWith(ServerResponse.GAME_END.toString())) {
            String [] tokens = response.split(":");
            String winner = response.split(":")[1];
            int score;

            if (winner.equals(username)) {
                score = Integer.parseInt(response.split(":")[2]);
                System.out.println("[" + count + "] " + "Game has ended! You won with a score of " + score);
            }
            else if (winner.equals(ServerResponse.NO_WINNERS.getMessage()))
                System.out.println("[" + count + "] " + "Game has ended! No winners!");
            else if (winner.equals(ServerResponse.TIE.getMessage())){
                StringBuilder msg = new StringBuilder();
                msg.append("[").append(count).append("] ").append("Game has ended! It's a tie between: ");

                // append players that tied
                for (int i = 2; i < tokens.length - 1; i++) {
                    msg.append(tokens[i]);

                    if (i != tokens.length - 2)
                        msg.append(", ");
                }

                // append score
                msg.append(" with a score of ").append(tokens[tokens.length - 1]);
                System.out.println(msg);
            }

            else {
                score = Integer.parseInt(response.split(":")[2]);
                System.out.println("[" + count + "] " + "Game has ended! Winner is: " + winner + " with a score of " + score);
            }

            return true;
        }
        else
            System.out.println("[" + count + "] " + response);

        return false;
    }

    public boolean expectsResponse(String message){
        // if the message ends with &Y, it means that the server expects a response
        return message.endsWith("#Y");
    }
}
