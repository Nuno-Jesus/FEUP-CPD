package org.example.db;

public class User {
    private String username;
    private String password;
    private double elo;

    public User(String username, String password, double elo) {
        this.username = username;
        this.password = password;
        this.elo = elo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getElo() {
        return elo;
    }

    public void setElo(double elo) {
        this.elo = elo;
    }

    @Override
    public String toString() {
        return "User {" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", elo=" + elo +
                '}';
    }
}
