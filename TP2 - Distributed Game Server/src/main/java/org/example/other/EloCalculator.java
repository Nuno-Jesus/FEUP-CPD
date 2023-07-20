package org.example.other;

import org.example.db.Database;
import org.example.db.User;
import org.example.other.Player;

import java.util.ArrayList;
import java.util.List;

public class EloCalculator {
    private static final int K_FACTOR = 32;
    private final List<Player> players; // list of players in the game
    private final List<Player> winners; // list of winners in the game
    private final Database db; // database object

    public EloCalculator(List<Player> players, List<Player> winners, Database db) {
        this.players = players;
        this.db = db;
        this.winners = winners;
    }

    public void updateElo(){
        double eloChange;

        // calculate new elo for each player
        for (Player player : players) {
            // losers lose 5 elo
            eloChange = -5;

            // winner gets additional 5 elo
            if (winners.contains(player)){
                eloChange = 10;
            }

            // find the corresponding user in the database and update their elo
            User user = db.userExists(player.getUsername());
            if (user != null) {
                user.setElo(user.getElo() + eloChange);
            }
        }
    }

}
