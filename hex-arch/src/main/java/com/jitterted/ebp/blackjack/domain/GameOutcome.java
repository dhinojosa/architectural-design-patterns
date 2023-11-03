package com.jitterted.ebp.blackjack.domain;

public enum GameOutcome {
    PLAYER_BUSTED("You Busted, so you lose.  💸"),
    DEALER_BUSTED("Dealer went BUST, Player wins! Yay for you!! 💵"),
    PLAYER_BEATS_DEALER("You beat the Dealer! 💵"),
    PLAYER_PUSHES("Push: Nobody wins, we'll call it even."),
    PLAYER_LOSES("You lost to the Dealer. 💸"),
    PLAYER_WINS_BLACKJACK("You won with Blackjack!");

    private final String display;

    public String display() {
        return display;
    }

    GameOutcome(String display) {
        this.display = display;
    }
}