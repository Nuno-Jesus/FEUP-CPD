package org.example.cards;

import org.example.utils.Color;

public class Card {
    private String name;
    private String suit;
    private int value;
    private String emoji;
    private String color;
    private String drawing;


    public Card(String name, String suit, int value, String emoji, String color) {
        this.name = name;
        this.suit = suit;
        this.value = value;
        this.emoji = emoji;
        this.color = color;
        this.drawing = createDrawing();
    }

    public String createDrawing(){
        String topName =(this.name.equals("10"))
                ? "|10     |"
                : "|"+this.name.charAt(0)+"      |";

        String bottomName =(this.name.equals("10"))
                ? "|     10|"
                : "|      "+this.name.charAt(0)+"|";

        String card =  "\t" + ".-------." + "\n";
               card += "\t" + topName + "\n";
               card += "\t" + "|   "+this.emoji+"   |" + "\n";
               card += "\t" + bottomName+ "\n";
               card += "\t" + "'-------'" + "\n";

        return this.color + card + Color.RESET;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        /*String card = String.valueOf(this.name.charAt(0));

        if (this.name.equals("10"))
            card = "10";

        return (this.color + card + this.emoji + Color.RESET);*/

        return this.color + this.drawing + Color.RESET;
    }
}