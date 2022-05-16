package com.jitterted.ebp.blackjack.domain;

public enum Suit {
    HEARTS("♥", true),
    CLUBS("♣", false),
    DIAMONDS("♦", true),
    SPADES("♠", false);

    private final String symbol;
    private final boolean isRed;

    Suit(String symbol, boolean isRed) {
        this.symbol = symbol;
        this.isRed = isRed;
    }

    public String symbol() {
        return symbol;
    }

    public boolean isRed() {
        return isRed;
    }
}
