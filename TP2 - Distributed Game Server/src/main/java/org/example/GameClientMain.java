package org.example;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GameClientMain {

    public static void main(String[] args) {
        if (args.length < 1)
            return;
        GameClient client = new GameClient(Integer.parseInt(args[0]));
        client.start();
    }
}
