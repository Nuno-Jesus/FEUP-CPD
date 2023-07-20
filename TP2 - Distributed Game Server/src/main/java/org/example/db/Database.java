package org.example.db;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;



public class Database {
    private final String filePath = "src/main/java/org/example/db/users.json";
    private List<User> users = new ArrayList<>();   // List of users

    public Database() {
        try {
            // Read the JSON file as a string
            String json = readFileAsString();

            // deal with empty file
            if (json.length() == 0) {
                return;
            }

            // Parse the JSON string and save it to a list of User objects
            users = parseJson(json);

            // Print the users
            /*for (User user : users) {
                System.out.println(user);
            }*/

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static List<User> parseJson(String json) {
        List<User> users = new ArrayList<>();

        // Remove the square brackets from the JSON string
        json = json.substring(1, json.length() - 1);

        // Split the JSON string into individual user objects
        String[] userStrings = json.split("},\\s*\\{");

        for (String userString : userStrings) {
            // Remove any surrounding curly braces
            userString = userString.replaceAll("[{}]", "");

            // Split the user string into key-value pairs
            String[] keyValuePairs = userString.split(",");

            String username = null;
            String password = null;
            double elo = 0.0;

            for (String keyValuePair : keyValuePairs) {
                // Split the key-value pair into key and value
                String[] parts = keyValuePair.split(":");
                String key = parts[0].replaceAll("\"", "").trim();
                String value = parts[1].replaceAll("\"", "").trim();

                switch (key) {
                    case "username" -> username = value;
                    case "password" -> password = value;
                    case "elo" -> elo = Double.parseDouble(value);
                }
            }

            // Create a User object and add it to the list
            User user = new User(username, password, elo);
            users.add(user);
        }

        return users;
    }

    private String readFileAsString() throws IOException{
        StringBuilder contentBuilder = new StringBuilder();

        Scanner scanner = new Scanner(new File(filePath));

        while (scanner.hasNextLine()) {
            contentBuilder.append(scanner.nextLine());
        }

        scanner.close();
        return contentBuilder.toString();
    }


    /**
     * Checks if a user exists in the database
     * @param username The username to check
     * @return The user object if the user exists, null otherwise
     */
    public User userExists(String username){
        for (User user : users) {
            if (user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }

    /**
     * Checks if a user exists in the database and if the password matches
     * @param username The username to check
     * @param password The password to check
     * @return True if the user exists and the password matches, false otherwise
     */
    public boolean doesPasswordMatch(String username, String password){
        for (User user : users) {
            if (user.getUsername().equals(username)){
                return user.getPassword().equals(password);
            }
        }
        return false;
    }

    /**
     * Adds a new user to the database
     * @param username The username of the new user
     * @param password The password of the new user
     */
    public void addUser(String username, String password){
        // Create new user object
        User newUser = new User(username, password, 0);

        // Add user to the list
        users.add(newUser);
        this.writeToFile();
    }

    public void writeToFile() {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            // Convert the list of users to JSON string
            String json = toJson(users);

            // Write the JSON string to the file
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toJson(List<User> users) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        // Iterate over the list of users and convert each user to JSON
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            json.append("{\"username\":\"")
                    .append(user.getUsername())
                    .append("\",\"password\":\"")
                    .append(user.getPassword())
                    .append("\",\"elo\":")
                    .append(user.getElo())
                    .append("}");

            // Add a comma separator if not the last user
            if (i < users.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }


    public List<User> getUsers() {
        return users;
    }
}