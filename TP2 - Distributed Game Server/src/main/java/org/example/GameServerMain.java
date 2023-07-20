package org.example;

public class GameServerMain {
    public static void main(String[] args) {
        if (args.length < 1)
            return;
        GameServer server = new GameServer(Integer.parseInt(args[0]));
        server.start();
    }
}
