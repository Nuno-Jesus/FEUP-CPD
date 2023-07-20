package org.example.other;

public enum ServerResponse {
    OK("OK"),
    WRONG_PASSWORD("WRONG_PASSWORD"),
    SHORT_USERNAME("SHORT_USERNAME"),
    START("START"),
    GAME_END("GAME_END"),
    BLEW_UP("BLEW_UP"),
    TIE("TIE"),
    NO_WINNERS("NO_WINNERS"),
    WAITING("WAITING"),
    ALREADY_LOGGED_IN("ALREADY_LOGGED_IN"),
    ALREADY_IN_QUEUE("ALREADY_IN_QUEUE");

    private final String message;

    ServerResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
