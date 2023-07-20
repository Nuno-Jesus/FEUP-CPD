package org.example.cards;

import org.example.utils.Color;

import java.util.*;

public class CardDeck {
    private final List<Card> cards;

    public CardDeck() {
        this.cards = new ArrayList<>();
        populateDeck();
    }

    /**
     * Fills the deck with 52 cards
     */
    private void populateDeck() {
        Map<String, String> suits = new HashMap<>(
                Map.of("Clubs", "♣", "Diamonds", "♦", "Hearts", "♥", "Spades", "♠")
        );
        String[] names = {"Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};
        int[] values = {11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};

        for (String suit : suits.keySet()) {
            for (int i = 0; i < names.length; i++) {
                String color = suit.equals("Diamonds") || suit.equals("Hearts") ? Color.RED_BRIGHT : Color.BLACK_BRIGHT;
                Card card = new Card(names[i], suit, values[i], suits.get(suit), color);
                cards.add(card);
            }
        }
    }

    /**
     * Shuffles the deck of cards
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Draws the top card from the deck
     * @return the card drawn
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }
}