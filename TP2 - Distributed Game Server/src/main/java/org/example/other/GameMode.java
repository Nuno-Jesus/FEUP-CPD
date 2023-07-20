package org.example.other;

public enum GameMode {
    RANKED("Playing ranked"),
    SIMPLE("Playing simple");

    private final String mode;

    GameMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}


